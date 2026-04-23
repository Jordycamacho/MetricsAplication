package com.fitapp.backend.suscription.aplication.service;

import com.fitapp.backend.auth.domain.exception.UserNotFoundException;
import com.fitapp.backend.auth.infrastructure.persistence.entity.UserEntity;
import com.fitapp.backend.auth.infrastructure.persistence.repository.SpringDataUserRepository;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionChangeReason;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionStatus;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionType;
import com.fitapp.backend.suscription.aplication.port.input.SubscriptionUseCase;
import com.fitapp.backend.suscription.aplication.port.output.SubscriptionPersistencePort;
import com.fitapp.backend.suscription.domain.model.SubscriptionModel;
import com.fitapp.backend.suscription.infrastructure.persistence.entity.SubscriptionHistoryEntity;
import com.fitapp.backend.suscription.infrastructure.persistence.repository.SpringDataSubscriptionHistoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionUseCase {

    private final SubscriptionPersistencePort subscriptionPersistence;
    private final SpringDataSubscriptionHistoryRepository historyRepo;
    private final SpringDataUserRepository userRepo;

    @Override
    @Transactional(readOnly = true)
    public SubscriptionModel getMySubscription(Long userId) {
        return subscriptionPersistence.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("El usuario no tiene suscripción"));
    }

    @Override
    @Transactional
    public SubscriptionModel createFreeSubscription(Long userId) {
        SubscriptionModel free = SubscriptionModel.builder()
                .type(SubscriptionType.FREE)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(10)) // FREE no expira en la práctica
                .autoRenew(false)
                .build();

        SubscriptionModel saved = subscriptionPersistence.save(free, userId);
        recordHistory(userId, null, SubscriptionType.FREE, SubscriptionChangeReason.REACTIVATION, "system", "Suscripción inicial FREE");
        return saved;
    }

    @Override
    @Transactional
    public SubscriptionModel changePlan(Long userId, SubscriptionType newType, String performedBy, String notes) {
        SubscriptionModel current = subscriptionPersistence.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("El usuario no tiene suscripción"));

        SubscriptionType oldType = current.getType();

        SubscriptionChangeReason reason = resolveReason(oldType, newType);

        current.setType(newType);
        current.setStatus(SubscriptionStatus.ACTIVE);
        current.setStartDate(LocalDate.now());
        current.setEndDate(newType == SubscriptionType.FREE
                ? LocalDate.now().plusYears(10)
                : LocalDate.now().plusMonths(1));
        current.setCancelledAt(null);
        current.setCancelReason(null);

        SubscriptionModel saved = subscriptionPersistence.save(current, userId);
        recordHistory(userId, oldType, newType, reason, performedBy, notes);

        log.info("Plan cambiado: usuario={} {} → {} por={}", userId, oldType, newType, performedBy);
        return saved;
    }

    @Override
    @Transactional
    public SubscriptionModel cancelSubscription(Long userId, String reason) {
        SubscriptionModel current = subscriptionPersistence.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("El usuario no tiene suscripción"));

        if (current.isFree()) {
            throw new IllegalStateException("No se puede cancelar una suscripción FREE");
        }

        current.setStatus(SubscriptionStatus.CANCELLED);
        current.setCancelledAt(java.time.LocalDateTime.now());
        current.setCancelReason(reason);
        current.setAutoRenew(false);

        SubscriptionModel saved = subscriptionPersistence.save(current, userId);
        recordHistory(userId, current.getType(), current.getType(),
                SubscriptionChangeReason.CANCELLATION, "user", reason);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canCreateRoutine(Long userId, int currentRoutineCount) {
        return subscriptionPersistence.findByUserId(userId)
                .map(s -> s.isWithinRoutineLimit(currentRoutineCount))
                .orElse(false);
    }

    // ── Cron: expira suscripciones vencidas ─────────────────────────────────

    @Override
    @Scheduled(cron = "0 0 2 * * *") // cada día a las 2 AM
    @Transactional
    public void expireSubscriptions() {
        List<SubscriptionModel> expired = subscriptionPersistence.findExpiredActive(LocalDate.now());

        for (SubscriptionModel sub : expired) {
            Long userId = getUserIdFromSubscription(sub);
            log.info("Expirando suscripción usuario={} tipo={}", userId, sub.getType());
            changePlan(userId, SubscriptionType.FREE, "system", "Suscripción expirada automáticamente");
        }

        List<SubscriptionModel> expiredTrials = subscriptionPersistence.findExpiredTrials();
        for (SubscriptionModel trial : expiredTrials) {
            Long userId = getUserIdFromSubscription(trial);
            log.info("Trial expirado usuario={}", userId);
            changePlan(userId, SubscriptionType.FREE, "system", "Trial expirado");
        }
    }

    // ── Helpers privados ─────────────────────────────────────────────────────

    private void recordHistory(Long userId, SubscriptionType from, SubscriptionType to,
                                SubscriptionChangeReason reason, String performedBy, String notes) {
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        SubscriptionHistoryEntity history = SubscriptionHistoryEntity.builder()
                .user(user)
                .fromType(from)
                .toType(to)
                .reason(reason)
                .performedBy(performedBy)
                .notes(notes)
                .build();

        historyRepo.save(history);
    }

    private SubscriptionChangeReason resolveReason(SubscriptionType from, SubscriptionType to) {
        if (from == null) return SubscriptionChangeReason.REACTIVATION;
        int fromOrd = from.ordinal();
        int toOrd = to.ordinal();
        if (toOrd > fromOrd) return SubscriptionChangeReason.UPGRADE;
        if (toOrd < fromOrd) return SubscriptionChangeReason.DOWNGRADE;
        return SubscriptionChangeReason.REACTIVATION;
    }

    private Long getUserIdFromSubscription(SubscriptionModel sub) {
        return sub.getUserId();
    }
}