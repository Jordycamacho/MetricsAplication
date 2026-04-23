package com.fitapp.backend.suscription.domain.model;

import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionStatus;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionModel {

    private Long id;

    private Long userId;

    // ── Tipo y estado ────────────────────────────────────────────────────────
    private SubscriptionType type;

    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    // ── Límites del plan ─────────────────────────────────────────────────────
    private SubscriptionLimitsModel limits;

    // ── Periodo ──────────────────────────────────────────────────────────────
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime trialEndsAt;

    @Builder.Default
    private boolean autoRenew = false;

    // ── Pago (preparado para el futuro, sin implementar aún) ─────────────────
    private String paymentProvider;
    private String externalSubscriptionId;

    // ── Cancelación ──────────────────────────────────────────────────────────
    private LocalDateTime cancelledAt;
    private String cancelReason;

    // ── Auditoría ────────────────────────────────────────────────────────────
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Helpers de negocio ───────────────────────────────────────────────────

    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE
                || status == SubscriptionStatus.TRIAL;
    }

    public boolean hasExpired() {
        return endDate != null && endDate.isBefore(LocalDate.now());
    }

    public boolean isFree() {
        return type == SubscriptionType.FREE;
    }

    public boolean canExportRoutines() {
        return limits != null && limits.isCanExportRoutines();
    }

    public boolean canAccessMarketplace() {
        return limits != null && limits.isMarketplaceRead();
    }

    public boolean canSellOnMarketplace() {
        return limits != null && limits.isMarketplaceSell();
    }

    /** Shortcut usado en UserModel.getGrantedAuthorities() y controllers */
    public Integer getMaxRoutines() {
        if (limits == null)
            return 1;
        return limits.getMaxRoutines();
    }

    public boolean isWithinRoutineLimit(int currentCount) {
        return limits != null && limits.isWithinRoutineLimit(currentCount);
    }
}