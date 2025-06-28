package com.fitapp.backend.infrastructure.persistence.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "exercise_sets")
public class ExerciseSetEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_exercise_id")
    private SessionExerciseEntity sessionExercise;

    @Column(name = "set_number", nullable = false)
    private int setNumber;

    @Column(columnDefinition = "jsonb")
    private String parameters; // Valor como String (ej: "60kg", "3min")

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}