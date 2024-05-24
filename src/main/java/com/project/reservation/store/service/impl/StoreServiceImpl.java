package com.project.reservation.store.service.impl;

import com.project.reservation.auth.entity.User;
import com.project.reservation.auth.entity.UserType;
import com.project.reservation.auth.repository.UserRepository;
import com.project.reservation.common.exception.CustomException;
import com.project.reservation.common.util.impl.GeoUtil;
import com.project.reservation.reservation.entity.Reservation;
import com.project.reservation.reservation.repository.ReservationRepository;
import com.project.reservation.review.entity.Review;
import com.project.reservation.review.repository.ReviewRepository;
import com.project.reservation.store.dto.StoreDto;
import com.project.reservation.store.entity.Store;
import com.project.reservation.store.model.StoreForm;
import com.project.reservation.store.repository.StoreQueryRepository;
import com.project.reservation.store.repository.StoreRepository;
import com.project.reservation.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.project.reservation.common.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;
    private final StoreQueryRepository storeQueryRepository;
    private final UserRepository userRepository;

    // 상점 추가 기능
    @Override
    @Transactional
    public StoreDto addStore(Long id, StoreForm form) {

        // 해당 유저가 없으면 에러 발생
        User findUser = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 파트너가 아닌 경우 에러 발생
        validateIsPartner(findUser);

        // 위도, 경도 값 정상값인지 확인 (정상 값 아닐 경우 에러 발생)
        GeoUtil.isValidLocation(
                form.getAddress().getLatitude(), form.getAddress().getLongitude()
        );

        Store newStore = Store.fromForm(form, findUser);
        Store savedStore = storeRepository.save(newStore);

        // 점주의 가게 리스트에 추가
        findUser.getStoreList().add(savedStore);

        // dto로 변환해서 반환
        return StoreDto.fromEntity(savedStore);
    }

    // 상점 정보 수정

    @Override
    @Transactional
    public StoreDto updateStore(Long id, Long storeId, StoreForm form) {

        Store findStore = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(STORE_NOT_FOUND));

        // 올바른 소유자의 상점인지 확인
        validateStoreOwner(id, findStore);

        findStore.updateStore(form);

        return StoreDto.fromEntity(findStore);
    }


    // 상점 삭제

    @Override
    public void deleteStore(Long id, Long storeId) {

        Store findStore = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(STORE_NOT_FOUND));

        // 올바른 소유자의 상점인지 확인
        validateStoreOwner(id, findStore);

        // 관련된 예약 모두 삭제
        List<Reservation> reservations = findStore.getReservations();
        reservationRepository.deleteAll(reservations);

        // 관련된 리뷰 모두 삭제
        List<Review> reviews = reviewRepository.findByStore(findStore);
        reviewRepository.deleteAll(reviews);

        storeRepository.delete(findStore);
    }

    // 이름 순 정렬

    @Override
    @Transactional(readOnly = true)
    public Page<StoreDto> sortByName(Pageable pageable) {

        PageRequest pageRequest =
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by("name")
                );

        return toStoreDtoList(pageRequest);
    }
    // 별점 순 정렬
    @Override
    @Transactional(readOnly = true)
    public Page<StoreDto> sortByStar(Pageable pageable) {

        // 별점 높은 순서 대로 정렬
        PageRequest pageRequest = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "star")
        );

        return toStoreDtoList(pageRequest);
    }

    // 거리 순 정렬
    @Override
    @Transactional(readOnly = true)
    public Page<StoreDto> sortByDistance(Double lat, Double lon, Pageable pageable) {

        // 위도, 경도 값 정상값인지 확인 (정상 값 아닐 경우 에러 발생)
        GeoUtil.isValidLocation(lat, lon);

        // QueryDsl을 사용한 custom repository 구현
        return storeQueryRepository.findSortByDistance(pageable, lat, lon)
                .map(StoreDto::fromEntity);
    }


    // 상점 주인 아니면 에러 발생

    private void validateStoreOwner(Long ownerId, Store store) {
        if (!Objects.equals(ownerId, store.getOwner().getId())) {
            throw new CustomException(STORE_OWNER_NOT_MATCH);
        }
    }


    private void validateIsPartner(User findUser) {
        if (findUser.getUserType() != UserType.PARTNER) {
            throw new CustomException(PARTNER_NOT_ENROLLED);
        }
    }


    // StoreDto로 변환해 반환하는 코드 중복 제거
    private Page<StoreDto> toStoreDtoList(PageRequest pageRequest) {
        return storeRepository.findAll(pageRequest)
                .map(StoreDto::fromEntity);
    }
}




