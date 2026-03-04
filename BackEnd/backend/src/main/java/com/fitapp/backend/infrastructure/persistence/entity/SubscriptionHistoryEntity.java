package com.fitapp.backend.infrastructure.persistence.entity;

import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionChangeReason;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Auditoría de todos los cambios de plan de un usuario.
 * Append-only — nunca se modifica ni elimina.
 * Útil para soporte, analytics de churn y posibles reembolsos pro-rata.
 */
@Entity
@Table(name = "subscription_history",
    indexes = {
        @Index(name = "idx_sub_hist_user",       columnList = "user_id"),
        @Index(name = "idx_sub_hist_changed_at",  columnList = "changed_at")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    /** NULL si es la primera suscripción del usuario. */
    @Enumerated(EnumType.STRING)
    @Column(name = "from_type", length = 20)
    private SubscriptionType fromType;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_type", nullable = false, length = 20)
    private SubscriptionType toType;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", length = 30)
    private SubscriptionChangeReason reason;

    /**
     * Quién ejecutó el cambio: "user", "system" (cron de expiración),
     * "admin" (override manual).
     */
    @Column(name = "performed_by", nullable = false, length = 50)
    @Builder.Default
    private String performedBy = "system";

    /** Notas adicionales (ej: ID de ticket de soporte, código promocional). */
    @Column(name = "notes", length = 500)
    private String notes;

    @CreationTimestamp
    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;
}