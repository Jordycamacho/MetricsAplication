package com.fitapp.backend.infrastructure.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionStatus;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions", indexes = {
        @Index(name = "idx_sub_user", columnList = "user_id"),
        @Index(name = "idx_sub_status", columnList = "status"),
        @Index(name = "idx_sub_end_date", columnList = "end_date, status"),
        @Index(name = "idx_sub_type_status", columnList = "subscription_type, status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    // ── Relaciones ───────────────────────────────────────────────────────────

    @OneToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    /**
     * Referencia a la configuración de límites para este tier.
     * Cuando cambia el plan, solo se actualiza este FK + subscriptionType.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "limits_id", nullable = false)
    private SubscriptionLimitsEntity limits;

    // ── Tipo y estado ────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_type", nullable = false, length = 20)
    private SubscriptionType subscriptionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    // ── Periodo ──────────────────────────────────────────────────────────────

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /** Solo poblado si status = TRIAL. Fecha en que termina la prueba gratuita. */
    @Column(name = "trial_ends_at")
    private LocalDateTime trialEndsAt;

    @Column(name = "auto_renew", nullable = false)
    @Builder.Default
    private boolean autoRenew = true;

    // ── Pago ─────────────────────────────────────────────────────────────────

    /** stripe / paypal / apple / google. NULL si es FREE o gestionado por admin. */
    @Column(name = "payment_provider", length = 50)
    private String paymentProvider;

    /** ID de la suscripción en el proveedor de pago externo. */
    @Column(name = "external_subscription_id", length = 200)
    private String externalSubscriptionId;

    // ── Cancelación ──────────────────────────────────────────────────────────

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    // ── Auditoría ────────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ── Helpers de negocio ───────────────────────────────────────────────────

    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE || status == SubscriptionStatus.TRIAL;
    }

    public boolean isTrial() {
        return status == SubscriptionStatus.TRIAL;
    }

    public boolean hasExpired() {
        return endDate != null && endDate.isBefore(LocalDate.now());
    }

    /** Shortcut para no tener que bajar por limits en cada sitio. */
    public Integer getMaxRoutines() {
        return limits != null ? limits.getMaxRoutines() : 1;
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
}