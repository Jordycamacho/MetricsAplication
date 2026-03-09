package com.fitapp.backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Cola de trabajos de recálculo de métricas.
 *
 * Cuando un usuario termina una sesión, se inserta una row aquí en lugar
 * de calcular las métricas síncronamente (lo que bloquearía la respuesta).
 *
 * Un job asíncrono (Spring @Scheduled o similar) procesa estas rows y:
 * 1. Recalcula ExerciseMetricEntity para los ejercicios de la sesión
 * 2. Actualiza ProgressionSnapshotEntity para los parámetros relevantes
 * 3. Evalúa y actualiza PersonalRecordEntity si hay nuevos PRs
 * 4. Crea SessionMetricEntity con el resumen de la sesión
 *
 * Status: PENDING → PROCESSING → DONE / FAILED
 */
@Entity
@Table(name = "metric_calculation_jobs", indexes = {
        @Index(name = "idx_mcj_status", columnList = "status"),
        @Index(name = "idx_mcj_user", columnList = "user_id"),
        @Index(name = "idx_mcj_session", columnList = "session_id"),
        @Index(name = "idx_mcj_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricCalculationJobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private WorkoutSessionEntity session;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING"; // PENDING | PROCESSING | DONE | FAILED

    @Column(name = "attempts", nullable = false)
    @Builder.Default
    private Integer attempts = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "processing_started_at")
    private LocalDateTime processingStartedAt;

    @Column(name = "processing_finished_at")
    private LocalDateTime processingFinishedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}