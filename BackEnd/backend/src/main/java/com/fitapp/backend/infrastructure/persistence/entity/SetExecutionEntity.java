package com.fitapp.backend.infrastructure.persistence.entity;

import com.fitapp.backend.infrastructure.persistence.entity.enums.SetExecutionStatus;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SetType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Set realmente ejecutado durante una sesión de entrenamiento.
 *
 * La diferencia con RoutineSetTemplateEntity (valores OBJETIVO)
 * es que aquí se almacenan los valores REALES del usuario.
 *
 * setTemplate puede ser NULL si el set fue añadido ad-hoc durante la sesión
 * (el usuario añade un set extra que no estaba en la rutina).
 */
@Entity
@Table(name = "set_executions",
    indexes = {
        @Index(name = "idx_se_session_ex",  columnList = "session_exercise_id"),
        @Index(name = "idx_se_position",    columnList = "session_exercise_id, position"),
        @Index(name = "idx_se_status",      columnList = "status")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SetExecutionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_exercise_id", nullable = false)
    private SessionExerciseEntity sessionExercise;

    /**
     * Template de referencia. NULL si el set fue añadido libremente
     * sin seguir la rutina planificada.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_template_id")
    private RoutineSetTemplateEntity setTemplate;

    // ── Posición y tipo ──────────────────────────────────────────────────────

    @Column(name = "position", nullable = false)
    private Integer position;

    @Enumerated(EnumType.STRING)
    @Column(name = "set_type", length = 30)
    private SetType setType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private SetExecutionStatus status = SetExecutionStatus.COMPLETED;

    // ── Timing ───────────────────────────────────────────────────────────────

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /** Descanso REAL tomado tras este set (puede diferir del planificado). */
    @Column(name = "actual_rest_seconds")
    private Integer actualRestSeconds;

    // ── Notas ────────────────────────────────────────────────────────────────

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // ── Valores reales por parámetro ─────────────────────────────────────────

    @OneToMany(mappedBy = "setExecution", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SetExecutionParameterEntity> parameters = new ArrayList<>();

    // ── Helpers ──────────────────────────────────────────────────────────────

    public boolean isCompleted() {
        return status == SetExecutionStatus.COMPLETED;
    }

    public Long getDurationSeconds() {
        if (startedAt == null || completedAt == null) return null;
        return java.time.Duration.between(startedAt, completedAt).getSeconds();
    }
}