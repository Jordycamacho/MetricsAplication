package com.fitapp.backend.suscription.infrastructure.persistence.adapter;

import com.fitapp.backend.auth.infrastructure.persistence.entity.UserEntity;
import com.fitapp.backend.auth.infrastructure.persistence.repository.SpringDataUserRepository;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionStatus;
import com.fitapp.backend.suscription.aplication.port.output.SubscriptionPersistencePort;
import com.fitapp.backend.suscription.domain.model.SubscriptionModel;
import com.fitapp.backend.suscription.infrastructure.persistence.converter.SubscriptionConverter;
import com.fitapp.backend.suscription.infrastructure.persistence.entity.SubscriptionEntity;
import com.fitapp.backend.suscription.infrastructure.persistence.entity.SubscriptionLimitsEntity;
import com.fitapp.backend.suscription.infrastructure.persistence.repository.SpringDataSubscriptionLimitsRepository;
import com.fitapp.backend.suscription.infrastructure.persistence.repository.SpringDataSubscriptionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SubscriptionPersistenceAdapter implements SubscriptionPersistencePort {

    private final SpringDataSubscriptionRepository subscriptionRepo;
    private final SpringDataSubscriptionLimitsRepository limitsRepo;
    private final SpringDataUserRepository userRepo;
    private final SubscriptionConverter subscriptionConverter;

    @Override
    @Transactional(readOnly = true)
    public Optional<SubscriptionModel> findByUserId(Long userId) {
        return subscriptionRepo.findByUserId(userId)
                .map(subscriptionConverter::toDomain);
    }

    @Override
    @Transactional
    public SubscriptionModel save(SubscriptionModel model, Long userId) {
        SubscriptionEntity entity = subscriptionRepo.findByUserId(userId)
                .orElseGet(() -> {
                    SubscriptionEntity newEntity = new SubscriptionEntity();
                    UserEntity user = userRepo.findById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + userId));
                    newEntity.setUser(user);
                    return newEntity;
                });

        SubscriptionLimitsEntity limits = limitsRepo
                .findByTier(model.getType().name())
                .orElseThrow(() -> new IllegalStateException(
                        "No hay límites configurados para el tier: " + model.getType()));

        subscriptionConverter.updateEntityFromModel(entity, model);
        entity.setLimits(limits);

        SubscriptionEntity saved = subscriptionRepo.save(entity);

        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + userId));
        user.setSubscription(saved);
        userRepo.save(user);

        return subscriptionConverter.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionModel> findExpiredActive(LocalDate date) {
        return subscriptionRepo
                .findByStatusAndEndDateBefore(SubscriptionStatus.ACTIVE, date)
                .stream()
                .map(subscriptionConverter::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionModel> findExpiredTrials() {
        return subscriptionRepo.findExpiredTrials()
                .stream()
                .map(subscriptionConverter::toDomain)
                .toList();
    }
}