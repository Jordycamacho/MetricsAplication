package com.fitapp.backend.application.ports.output;

import java.util.Optional;

import com.fitapp.backend.domain.model.SubscriptionModel;

public interface SubscriptionPersistencePort {
    Optional<SubscriptionModel> findByUserId(Long userId);
    SubscriptionModel save(SubscriptionModel subscription);
}