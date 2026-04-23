package com.fitapp.backend.suscription.infrastructure.persistence.repository;

import com.fitapp.backend.infrastructure.persistence.entity.SubscriptionHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataSubscriptionHistoryRepository extends JpaRepository<SubscriptionHistoryEntity, Long> {

    List<SubscriptionHistoryEntity> findByUserIdOrderByChangedAtDesc(Long userId);
}