package com.fitapp.backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Configuración de límites por tier de suscripción.
 * Una row por tier (FREE, STANDARD, PREMIUM).
 * NULL en campos numéricos significa "ilimitado".
 *
 * Esta entidad es inmutable en runtime — solo se modifica por migraciones
 * cuando cambia el modelo de negocio. NUNCA se crea/modifica desde código
 * de aplicación normal.
 */
@Entity
@Table(name = "subscription_limits", uniqueConstraints = @UniqueConstraint(name = "uk_limits_tier", columnNames = "tier"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionLimitsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    // El tier al que corresponde esta row. Coincide con SubscriptionType enum.
    @Column(name = "tier", nullable = false, length = 20)
    private String tier;

    // ── Rutinas ─────────────────────────────────────────────────────────────
    // NULL = ilimitado
    @Column(name = "max_routines")
    private Integer maxRoutines;

    // ── Contenido personalizado ──────────────────────────────────────────────
    @Column(name = "max_custom_sports")
    private Integer maxCustomSports;

    @Column(name = "max_custom_parameters")
    private Integer maxCustomParameters;

    @Column(name = "max_custom_categories")
    private Integer maxCustomCategories;

    @Column(name = "max_custom_exercises")
    private Integer maxCustomExercises;

    // ── Historial ────────────────────────────────────────────────────────────
    // Días de historial de sesiones accesibles. NULL = ilimitado.
    @Column(name = "history_days")
    private Integer historyDays;

    // ── Analytics ────────────────────────────────────────────────────────────
    @Column(name = "basic_analytics", nullable = false)
    @Builder.Default
    private boolean basicAnalytics = false;

    @Column(name = "advanced_analytics", nullable = false)
    @Builder.Default
    private boolean advancedAnalytics = false;

    // ── Import / Export ──────────────────────────────────────────────────────
    @Column(name = "can_export_routines", nullable = false)
    @Builder.Default
    private boolean canExportRoutines = false;

    @Column(name = "can_import_routines", nullable = false)
    @Builder.Default
    private boolean canImportRoutines = false;

    // ── Marketplace ──────────────────────────────────────────────────────────
    @Column(name = "marketplace_read", nullable = false)
    @Builder.Default
    private boolean marketplaceRead = false; // ver packs disponibles

    @Column(name = "marketplace_sell", nullable = false)
    @Builder.Default
    private boolean marketplaceSell = false; // vender rutinas/packs propios

    @Column(name = "free_packs_only", nullable = false)
    @Builder.Default
    private boolean freePacksOnly = true; // solo puede instalar packs gratuitos

    // ── Helpers ──────────────────────────────────────────────────────────────

    public boolean isUnlimited(Integer limit) {
        return limit == null;
    }

    public boolean routinesUnlimited() {
        return maxRoutines == null;
    }

    public boolean parametersUnlimited() {
        return maxCustomParameters == null;
    }

    public boolean historyUnlimited() {
        return historyDays == null;
    }
}