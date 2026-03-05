package com.fitapp.backend.infrastructure.persistence.entity;

import com.fitapp.backend.infrastructure.persistence.converter.DaysOfWeekConverter;
import com.fitapp.backend.infrastructure.persistence.entity.enums.DayOfWeek;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Rutina de entrenamiento.
 *
 * CAMBIOS v2:
 * - price, currency → venta en marketplace
 * - originalRoutineId → trazabilidad de rutinas copiadas/compradas
 * - version → versionado de templates
 * - packageId → si pertenece a un pack
 * - exportKey (UUID) → share link sin necesidad de cuenta
 * - timesPurchased → stats de ventas
 */
@Entity
@Table(name = "routines", indexes = {
        @Index(name = "idx_routine_user", columnList = "user_id"),
        @Index(name = "idx_routine_sport", columnList = "sport_id"),
        @Index(name = "idx_routine_template_public", columnList = "is_template, is_public"),
        @Index(name = "idx_routine_package", columnList = "package_id"),
        @Index(name = "idx_routine_export_key", columnList = "export_key"),
        @Index(name = "idx_routine_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // ── Relaciones core ──────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id")
    private SportEntity sport;

    // ── Configuración ────────────────────────────────────────────────────────

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Convert(converter = DaysOfWeekConverter.class)
    @Column(name = "training_days", columnDefinition = "TEXT")
    @Builder.Default
    private Set<DayOfWeek> trainingDays = new HashSet<>();

    @Column(name = "goal", length = 500)
    private String goal;

    @Min(1)
    @Max(7)
    @Column(name = "sessions_per_week")
    private Integer sessionsPerWeek;

    // ── Exercises ────────────────────────────────────────────────────────────

    @OneToMany(mappedBy = "routine", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sessionNumber ASC, position ASC")
    @Builder.Default
    private Set<RoutineExerciseEntity> exercises = new HashSet<>();

    // ── Marketplace / Template ────────────────────────────────────────────────

    /**
     * Si es plantilla, otros usuarios pueden importarla/comprarla
     * y crear su propia copia editable.
     */
    @Column(name = "is_template", nullable = false)
    @Builder.Default
    private Boolean isTemplate = false;

    /** Visible en el marketplace público. Requiere isTemplate = true. */
    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = false;

    /** NULL = gratis. Solo aplica si isPublic = true. */
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "USD";

    /** Número de veces que se ha comprado o importado. */
    @Column(name = "times_purchased", nullable = false)
    @Builder.Default
    private Integer timesPurchased = 0;

    /**
     * Si esta rutina es copia de otra (template comprada o importada),
     * apunta a la rutina original. NULL si es original.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_routine_id")
    private RoutineEntity originalRoutine;

    /**
     * Versión del template. Ej: "1.2.0".
     * NULL si no es template o no se versiona.
     */
    @Column(name = "version", length = 20)
    private String version;

    /** Si pertenece a un pack distribuible. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id")
    private PackageEntity pack;

    /**
     * UUID para compartir la rutina via link sin necesidad de cuenta.
     * Generado automáticamente en @PrePersist.
     * Ej: fitapp.com/import/550e8400-e29b-41d4-a716-446655440000
     */
    @Column(name = "export_key", unique = true, updatable = false)
    private UUID exportKey;

    // ── Timestamps ───────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    // ── Lifecycle ────────────────────────────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        if (exportKey == null) {
            exportKey = UUID.randomUUID();
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    public boolean isFree() {
        return price == null || price.compareTo(BigDecimal.ZERO) == 0;
    }

    public void registerPurchase() {
        this.timesPurchased++;
    }

    public void markAsUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }
}