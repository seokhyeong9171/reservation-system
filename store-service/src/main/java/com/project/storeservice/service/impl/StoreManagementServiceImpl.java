package com.project.storeservice.service.impl;

import com.project.common.util.GeoUtil;
import com.project.domain.dto.StoreDto;
import com.project.domain.entity.Store;
import com.project.domain.entity.User;
import com.project.domain.model.StoreDomainForm;
import com.project.domain.repository.ReservationRepository;
import com.project.domain.repository.ReviewRepository;
import com.project.domain.repository.StoreRepository;
import com.project.storeservice.service.StoreManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@RequiredArgsConstructor
public class StoreManagementServiceImpl implements StoreManagementService {

    private final StoreRepository storeRepository;
    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;

    private final KioskService kioskService;


    // 상점 추가 기능
    @Override
    @Transactional
    public StoreDto addStore(User partner, StoreDomainForm form) {

        // 위도, 경도 값 정상값인지 확인 (정상 값 아닐 경우 에러 발생)
        GeoUtil.isValidLocation(
                form.getAddress().getLatitude(), form.getAddress().getLongitude()
        );

        Store newStore = Store.fromForm(form, partner);
        Store savedStore = storeRepository.save(newStore);

        // 점주의 가게 리스트에 추가
        partner.getStoreList().add(savedStore);

        // 키오스크 등록
        kioskService.addKiosk(savedStore);

        // dto로 변환해서 반환
        return StoreDto.fromEntity(savedStore);
    }

    // 상점 정보 수정
    @Override
    @Transactional
    public StoreDto updateStore(Store store, StoreDomainForm form) {

        store.updateStore(form);

        return StoreDto.fromEntity(store);
    }


    // 상점 삭제

    @Override
    public void deleteStore(Store store) {

        // 관련된 예약 모두 삭제
        reservationRepository.deleteAllByStore(store);

        // 관련된 리뷰 모두 삭제
        reviewRepository.deleteAllByStore(store);

        // 해당 상점 삭제
        storeRepository.delete(store);
    }


}