package com.fitapp.backend.infrastructure.persistence.repository;

import com.fitapp.backend.infrastructure.persistence.entity.SubscriptionEntity;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataSubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {

    Optional<SubscriptionEntity> findByUserId(Long userId);

    List<SubscriptionEntity> findByStatusAndEndDateBefore(SubscriptionStatus status, LocalDate date);

    @Query("SELECT s FROM SubscriptionEntity s WHERE s.status = 'TRIAL' AND s.trialEndsAt < CURRENT_TIMESTAMP")
    List<SubscriptionEntity> findExpiredTrials();
}