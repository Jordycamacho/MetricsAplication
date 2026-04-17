package com.fitapp.backend.infrastructure.persistence.entity;

import com.fitapp.backend.infrastructure.persistence.entity.enums.MetricAggregation;
import com.fitapp.backend.infrastructure.persistence.entity.enums.MetricGranularity;
import com.fitapp.backend.parameter.infrastructure.persistence.entity.CustomParameterEntity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * IMPORTATE no hacer la implementación todavia mu complicado 
 * Métricas precalculadas por ejercicio + parámetro + granularidad temporal.
 *
 * Por qué precalcular:
 * Calcular en tiempo real "el volumen total de sentadilla en las últimas 4 semanas
 * agrupado por semana" requiere recorrer todos los SetExecutionParameterEntity,
 * que pueden ser miles. Estas rows se calculan de forma asíncrona tras cada sesión.
 *
 * Una row = (usuario, ejercicio, parámetro, agregación, granularidad, periodo)
 *
 * Ejemplos de rows generadas:
 *   user=1, exercise=sentadilla, param=peso, agg=MAX,  gran=WEEK,  period=2025-W03, value=120.0
 *   user=1, exercise=sentadilla, param=peso, agg=MAX,  gran=MONTH, period=2025-01,  value=120.0
 *   user=1, exercise=dominadas,  param=reps, agg=SUM,  gran=WEEK,  period=2025-W03, value=87
 *   user=1, exercise=remo500m,   param=tiempo, agg=MIN, gran=DAY,  period=2025-01-15, value=95000 (ms)
 *
 * Estas métricas se usan para los gráficos de progresión de la app.
 * Se recalculan vía job asíncrono después de cada sesión completada.
 */
@Entity
@Table(name = "exercise_metrics",
    uniqueConstraints = {
        // Una sola row por combinación. El job de recálculo hace UPSERT.
        @UniqueConstraint(
            name = "uk_metric_user_ex_param_agg_gran_period",
            columnNames = {"user_id", "exercise_id", "parameter_id", "aggregation", "granularity", "period_key"}
        )
    },
    indexes = {
        @Index(name = "idx_em_user_exercise",   columnList = "user_id, exercise_id"),
        @Index(name = "idx_em_user_param",      columnList = "user_id, parameter_id"),
        @Index(name = "idx_em_user_gran",       columnList = "user_id, granularity, period_key"),
        @Index(name = "idx_em_exercise_param",  columnList = "exercise_id, parameter_id, granularity")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseMetricEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    // ── Dimensiones ──────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    /**
     * El ejercicio medido.
     * NULL para métricas de sesión global (ej: volumen total de la sesión).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    private ExerciseEntity exercise;

    /** El parámetro que se mide. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parameter_id", nullable = false)
    private CustomParameterEntity parameter;

    /** Tipo de agregación: MAX (PR), SUM (volumen), AVG (media), MIN (velocidad). */
    @Enumerated(EnumType.STRING)
    @Column(name = "aggregation", nullable = false, length = 20)
    private MetricAggregation aggregation;

    /** Granularidad temporal: DAY, WEEK, MONTH, ALL_TIME. */
    @Enumerated(EnumType.STRING)
    @Column(name = "granularity", nullable = false, length = 20)
    private MetricGranularity granularity;

    /**
     * Clave del periodo en formato ISO:
     * DAY      → "2025-01-15"
     * WEEK     → "2025-W03"
     * MONTH    → "2025-01"
     * ALL_TIME → "ALL"
     *
     * Formato string para simplicidad. Se indexa para filtrar por rango de fechas
     * usando LIKE o comparaciones alfanuméricas (ISO es ordenable).
     */
    @Column(name = "period_key", nullable = false, length = 20)
    private String periodKey;

    /**
     * Fecha de inicio del periodo (para queries de rango con BETWEEN).
     * Redundante con period_key pero facilita queries sin parsear strings.
     */
    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    // ── Valor ────────────────────────────────────────────────────────────────

    /** El valor agregado calculado. */
    @Column(name = "value", nullable = false)
    private Double value;

    /** Número de sets/sesiones que contribuyeron a este valor. */
    @Column(name = "sample_count", nullable = false)
    @Builder.Default
    private Integer sampleCount = 0;

    // ── Auditoría ────────────────────────────────────────────────────────────

    @UpdateTimestamp
    @Column(name = "recalculated_at", nullable = false)
    private LocalDateTime recalculatedAt;
}