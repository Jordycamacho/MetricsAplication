package com.fitapp.backend.metrics.infrastructure.persistence.entity;

import com.fitapp.backend.Exercise.infrastructure.persistence.entity.ExerciseEntity;
import com.fitapp.backend.auth.infrastructure.persistence.entity.UserEntity;
import com.fitapp.backend.infrastructure.persistence.entity.enums.MetricGranularity;
import com.fitapp.backend.parameter.infrastructure.persistence.entity.CustomParameterEntity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Snapshot de progresión para construir gráficos de tendencia.
 *
 * Diferencia con ExerciseMetricEntity:
 * - ExerciseMetricEntity = métricas puntuales (el MAX de peso de la semana X)
 * - ProgressionSnapshotEntity = tendencia acumulativa (cómo evoluciona el 1RM estimado semana a semana)
 *
 * El caso de uso principal es el gráfico de línea de progresión del usuario:
 * "muéstrame cómo ha evolucionado mi peso en sentadilla a lo largo de 3 meses"
 *
 * Cada row representa un punto en el gráfico.
 * Se calcula junto con ExerciseMetricEntity en el job asíncrono post-sesión.
 *
 * También soporta métricas custom del usuario:
 * "muéstrame cómo evoluciona mi potencia media en press banca" (si tiene el parámetro potencia)
 */
@Entity
@Table(name = "progression_snapshots",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_snap_user_ex_param_gran_period",
            columnNames = {"user_id", "exercise_id", "parameter_id", "granularity", "period_start"}
        )
    },
    indexes = {
        @Index(name = "idx_snap_user_ex",       columnList = "user_id, exercise_id"),
        @Index(name = "idx_snap_user_ex_param", columnList = "user_id, exercise_id, parameter_id"),
        @Index(name = "idx_snap_period",        columnList = "user_id, granularity, period_start"),
        @Index(name = "idx_snap_param",         columnList = "parameter_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressionSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    // ── Dimensiones ──────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    private ExerciseEntity exercise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parameter_id", nullable = false)
    private CustomParameterEntity parameter;

    @Enumerated(EnumType.STRING)
    @Column(name = "granularity", nullable = false, length = 20)
    private MetricGranularity granularity;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    // ── Valores del snapshot ──────────────────────────────────────────────────

    /**
     * Valor máximo del parámetro en el periodo (para parámetros tipo PR: peso, distancia).
     * Es el punto en el gráfico de progresión.
     */
    @Column(name = "max_value")
    private Double maxValue;

    /** Valor medio del parámetro en el periodo. */
    @Column(name = "avg_value")
    private Double avgValue;

    /** Valor mínimo del parámetro en el periodo (para tiempos de velocidad). */
    @Column(name = "min_value")
    private Double minValue;

    /** Suma total del parámetro en el periodo (volumen total). */
    @Column(name = "sum_value")
    private Double sumValue;

    /** Número de muestras (sets) que contribuyen a este snapshot. */
    @Column(name = "sample_count", nullable = false)
    @Builder.Default
    private Integer sampleCount = 0;

    /**
     * 1RM estimado para este periodo (solo para ejercicios con peso).
     * Calculado con la fórmula de Epley: 1RM = peso * (1 + reps/30)
     * NULL si el ejercicio no tiene parámetro de peso o el de reps.
     */
    @Column(name = "estimated_1rm")
    private Double estimated1rm;

    /**
     * Porcentaje de cambio respecto al periodo anterior.
     * Positivo = mejora, negativo = regresión.
     * NULL si es el primer periodo del usuario para esta combinación.
     */
    @Column(name = "change_pct")
    private Double changePct;

    // ── Auditoría ────────────────────────────────────────────────────────────

    @UpdateTimestamp
    @Column(name = "recalculated_at", nullable = false)
    private LocalDateTime recalculatedAt;
}