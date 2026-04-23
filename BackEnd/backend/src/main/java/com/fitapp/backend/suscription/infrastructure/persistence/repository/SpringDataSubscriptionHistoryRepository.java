package com.fitapp.backend.suscription.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitapp.backend.suscription.infrastructure.persistence.entity.SubscriptionHistoryEntity;

import java.util.List;

@Repository
public interface SpringDataSubscriptionHistoryRepository extends JpaRepository<SubscriptionHistoryEntity, Long> {

    List<SubscriptionHistoryEntity> findByUserIdOrderByChangedAtDesc(Long userId);
}