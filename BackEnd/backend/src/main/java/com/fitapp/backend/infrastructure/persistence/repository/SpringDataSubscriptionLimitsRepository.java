package com.fitapp.backend.infrastructure.persistence.repository;

import com.fitapp.backend.infrastructure.persistence.entity.SubscriptionLimitsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataSubscriptionLimitsRepository extends JpaRepository<SubscriptionLimitsEntity, Long> {

    Optional<SubscriptionLimitsEntity> findByTier(String tier);
}