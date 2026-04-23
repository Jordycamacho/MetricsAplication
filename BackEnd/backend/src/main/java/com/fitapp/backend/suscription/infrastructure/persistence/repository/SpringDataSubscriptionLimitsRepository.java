package com.fitapp.backend.suscription.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitapp.backend.suscription.infrastructure.persistence.entity.SubscriptionLimitsEntity;

import java.util.Optional;

@Repository
public interface SpringDataSubscriptionLimitsRepository extends JpaRepository<SubscriptionLimitsEntity, Long> {

    Optional<SubscriptionLimitsEntity> findByTier(String tier);
}