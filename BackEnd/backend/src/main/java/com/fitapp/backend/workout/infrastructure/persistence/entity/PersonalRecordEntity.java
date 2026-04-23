package com.fitapp.backend.workout.infrastructure.persistence.entity;

import com.fitapp.backend.Exercise.infrastructure.persistence.entity.ExerciseEntity;
import com.fitapp.backend.auth.infrastructure.persistence.entity.UserEntity;
import com.fitapp.backend.infrastructure.persistence.entity.enums.MetricAggregation;
import com.fitapp.backend.parameter.infrastructure.persistence.entity.CustomParameterEntity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Récord personal de un usuario para un parámetro concreto en un ejercicio concreto.
 *
 * El sistema soporta PRs completamente personalizados:
 * - PR de "peso máximo" en sentadilla → parameter=peso, aggregation=MAX
 * - PR de "repeticiones máximas" en dominadas → parameter=reps, aggregation=MAX
 * - PR de "mejor tiempo" en 400m → parameter=tiempo, aggregation=MIN
 * - PR de "mayor distancia" en remo → parameter=distancia, aggregation=MAX
 * - PR de "potencia máxima" (parámetro custom) en cualquier ejercicio
 *
 * CAMBIOS v2:
 * - Se añade aggregation para saber qué tipo de PR es (MAX, MIN, SUM...)
 * - Se enlaza con SetExecutionEntity para tener contexto exacto
 * - Se añade previousRecord y progressPercentage para analytics de progresión
 * - exercise puede ser NULL para PRs globales (ej: mejor RPE general de una sesión)
 */
@Entity
@Table(name = "personal_records",
    uniqueConstraints = {
        // Solo puede haber un PR activo por (user, exercise, parameter, aggregation)
        @UniqueConstraint(
            name = "uk_pr_user_exercise_param_agg",
            columnNames = {"user_id", "exercise_id", "parameter_id", "aggregation"}
        )
    },
    indexes = {
        @Index(name = "idx_pr_user_exercise",   columnList = "user_id, exercise_id"),
        @Index(name = "idx_pr_user_parameter",  columnList = "user_id, parameter_id"),
        @Index(name = "idx_pr_date",            columnList = "user_id, achieved_date"),
        @Index(name = "idx_pr_exercise_param",  columnList = "exercise_id, parameter_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    // ── Contexto ─────────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    /**
     * El ejercicio en el que se consiguió el PR.
     * NULL para métricas de sesión (ej: mayor volumen total en una sesión).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    private ExerciseEntity exercise;

    /** El parámetro que se mide (peso, reps, tiempo, potencia, RPE...). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parameter_id", nullable = false)
    private CustomParameterEntity parameter;

    /**
     * Tipo de agregación de este PR.
     * Necesario porque el mismo parámetro puede tener varios PRs:
     * ej: "peso MAX en sentadilla" y no entra en conflicto con
     * "volumen SUM en sentadilla".
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "aggregation", nullable = false, length = 20)
    private MetricAggregation aggregation;

    // ── Valor ────────────────────────────────────────────────────────────────

    /** El valor del PR. Double cubre todos los tipos numéricos. */
    @Column(name = "value", nullable = false)
    private Double value;

    // ── Progresión ───────────────────────────────────────────────────────────

    /** Valor del PR anterior (antes de ser superado). NULL si es el primer PR. */
    @Column(name = "previous_record")
    private Double previousRecord;

    /** Porcentaje de mejora respecto al récord anterior. NULL si es el primero. */
    @Column(name = "progress_percentage")
    private Double progressPercentage;

    // ── Contexto de consecución ───────────────────────────────────────────────

    @Column(name = "achieved_date", nullable = false)
    private LocalDate achievedDate;

    /**
     * Set exacto en el que se consiguió el PR.
     * Permite al usuario volver a ver el contexto completo de la sesión.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_execution_id")
    private SetExecutionEntity setExecution;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ── Helpers ──────────────────────────────────────────────────────────────

    public void updateProgress(Double previousValue) {
        this.previousRecord = previousValue;
        if (previousValue != null && previousValue != 0) {
            this.progressPercentage = ((this.value - previousValue) / Math.abs(previousValue)) * 100;
        }
    }

    public boolean isBetterThan(Double other) {
        if (other == null) return true;
        return switch (aggregation) {
            case MAX, SUM, LAST -> this.value > other;
            case MIN            -> this.value < other;
            case AVG            -> this.value > other; // AVG mayor = mejor por defecto
        };
    }
}