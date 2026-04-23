package com.fitapp.backend.suscription.aplication.port.output;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.fitapp.backend.suscription.domain.model.SubscriptionModel;

public interface SubscriptionPersistencePort {
    Optional<SubscriptionModel> findByUserId(Long userId);
    SubscriptionModel save(SubscriptionModel model, Long userId);
    List<SubscriptionModel> findExpiredActive(LocalDate date);
    List<SubscriptionModel> findExpiredTrials();
}