package com.fitapp.backend.infrastructure.persistence.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.fitapp.backend.infrastructure.persistence.entity.SubscriptionEntity;

@Repository
public interface SpringDataSubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {

     Optional<SubscriptionEntity> findByUserId(Long userId);
}