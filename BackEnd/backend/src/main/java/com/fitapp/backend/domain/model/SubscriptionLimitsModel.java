package com.fitapp.backend.domain.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionLimitsModel {

    private Long id;
    private String tier;

    // ── Rutinas ─────────────────────────────────────────────────────────────
    private Integer maxRoutines;           // null = ilimitado

    // ── Contenido personalizado ──────────────────────────────────────────────
    private Integer maxCustomSports;
    private Integer maxCustomParameters;
    private Integer maxCustomCategories;
    private Integer maxCustomExercises;

    // ── Historial ────────────────────────────────────────────────────────────
    private Integer historyDays;           // null = ilimitado

    // ── Features booleanas ───────────────────────────────────────────────────
    private boolean basicAnalytics;
    private boolean advancedAnalytics;
    private boolean canExportRoutines;
    private boolean canImportRoutines;
    private boolean marketplaceRead;
    private boolean marketplaceSell;
    private boolean freePacksOnly;

    // ── Helpers ──────────────────────────────────────────────────────────────

    public boolean routinesUnlimited() {
        return maxRoutines == null;
    }

    public boolean historyUnlimited() {
        return historyDays == null;
    }

    public boolean isWithinRoutineLimit(int currentCount) {
        return routinesUnlimited() || currentCount < maxRoutines;
    }

    public boolean isWithinCustomExerciseLimit(int currentCount) {
        return maxCustomExercises == null || currentCount < maxCustomExercises;
    }
}