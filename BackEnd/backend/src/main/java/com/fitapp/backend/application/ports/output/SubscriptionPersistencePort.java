package com.fitapp.backend.application.ports.output;

import com.fitapp.backend.domain.model.SubscriptionModel;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscriptionPersistencePort {
    Optional<SubscriptionModel> findByUserId(Long userId);
    SubscriptionModel save(SubscriptionModel model, Long userId);
    List<SubscriptionModel> findExpiredActive(LocalDate date);
    List<SubscriptionModel> findExpiredTrials();
}