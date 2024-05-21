package com.project.reservation.store.repository;

import com.project.reservation.store.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long>, StoreQueryRepository{

    Page<Store> findAll(Pageable pageable);

}