package com.fitapp.backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Métricas agregadas de una sesión de entrenamiento completa.
 *
 * Se calcula una vez al completar la sesión y no se modifica.
 * Sirve como snapshot inmutable del rendimiento de esa sesión.
 *
 * Por qué no calcular esto en tiempo real desde WorkoutLogEntity:
 * - WorkoutLogEntity tiene pocos campos (start/end time, performance_score).
 * - Los valores de sesión (volumen total, PRs, ejercicios completados) requieren
 *   agregar todos los SetExecutionParameterEntity de la sesión → costoso.
 * - Precalcularlo aquí permite mostrar el resumen de sesión instantáneamente.
 *
 * Esta entidad NO reemplaza WorkoutLogEntity — conviven:
 * - WorkoutLogEntity → log operacional (cuándo, con qué rutina)
 * - SessionMetricEntity → snapshot analítico (qué se consiguió)
 */
@Entity
@Table(name = "session_metrics",
    indexes = {
        @Index(name = "idx_sm_user_date",     columnList = "user_id, session_date"),
        @Index(name = "idx_sm_session",       columnList = "session_id"),
        @Index(name = "idx_sm_routine",       columnList = "routine_id"),
        @Index(name = "idx_sm_user_routine",  columnList = "user_id, routine_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionMetricEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    // ── Contexto ─────────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private WorkoutSessionEntity session;

    /**
     * Rutina usada en la sesión. NULL si fue entrenamiento libre
     * (sin rutina predefinida).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id")
    private RoutineEntity routine;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    // ── Timing ───────────────────────────────────────────────────────────────

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /** Duración total en segundos. */
    @Column(name = "duration_seconds")
    private Long durationSeconds;

    // ── Volumen y carga ───────────────────────────────────────────────────────

    /**
     * Volumen total de la sesión calculado como SUM(peso * reps) de todos los sets.
     * Solo aplica para ejercicios con parámetros de peso + reps.
     * NULL si la sesión no tiene ejercicios con peso.
     */
    @Column(name = "total_volume_kg")
    private Double totalVolumeKg;

    /** Número total de sets completados. */
    @Column(name = "sets_completed", nullable = false)
    @Builder.Default
    private Integer setsCompleted = 0;

    /** Número de sets saltados. */
    @Column(name = "sets_skipped", nullable = false)
    @Builder.Default
    private Integer setsSkipped = 0;

    /** Número de ejercicios distintos completados. */
    @Column(name = "exercises_completed", nullable = false)
    @Builder.Default
    private Integer exercisesCompleted = 0;

    // ── PRs ──────────────────────────────────────────────────────────────────

    /** Número de récords personales conseguidos en esta sesión. */
    @Column(name = "personal_records_count", nullable = false)
    @Builder.Default
    private Integer personalRecordsCount = 0;

    // ── Percepción subjetiva ──────────────────────────────────────────────────

    /**
     * RPE (Rate of Perceived Exertion) medio de la sesión, si el usuario
     * registró RPE en sus sets. Escala 1-10. NULL si no se registró.
     */
    @Column(name = "avg_rpe")
    private Double avgRpe;

    /**
     * Puntuación de rendimiento calculada por el sistema (0-100).
     * Considera: adherencia al plan, PRs, volumen vs sesión anterior.
     */
    @Column(name = "performance_score")
    private Integer performanceScore;

    // ── Adherencia al plan ────────────────────────────────────────────────────

    /**
     * Porcentaje de sets completados respecto a los planificados en la rutina.
     * NULL si fue entrenamiento libre. 100.0 = completó todo el plan.
     */
    @Column(name = "plan_adherence_pct")
    private Double planAdherencePct;

    // ── Auditoría ────────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ── Helpers ──────────────────────────────────────────────────────────────

    public boolean hasPersonalRecords() {
        return personalRecordsCount != null && personalRecordsCount > 0;
    }

    public boolean isComplete() {
        return completedAt != null;
    }
}