package com.project.reservation.service.impl;

import com.project.reservation.dto.StoreDto;
import com.project.reservation.entity.Store;
import com.project.reservation.entity.User;
import com.project.reservation.entity.UserType;
import com.project.reservation.exception.CustomException;
import com.project.reservation.model.input.StoreForm;
import com.project.reservation.repository.StoreRepository;
import com.project.reservation.repository.UserRepository;
import com.project.reservation.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.project.reservation.exception.ErrorCode.*;

@Service
@Transactional
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    @Override
    public StoreDto addStore(Long id, StoreForm.AddStoreForm form) {

        // 해당 유저가 없으면 에러 발생
        User findUser = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 파트너가 아닌 경우 에러 발생
        if (findUser.getUserType() != UserType.PARTNER) {
            throw new CustomException(PARTNER_NOT_ENROLLED);
        }

        Store newStore = Store.fromForm(form, findUser);
        Store savedStore = storeRepository.save(newStore);

        // 점주의 가게 리스트에 추가
        findUser.getStoreList().add(savedStore);

        // dto로 변환해서 반환
        return StoreDto.fromEntity(savedStore);
    }
}