package com.fitapp.backend.infrastructure.persistence.entity;

import com.fitapp.backend.infrastructure.persistence.entity.enums.ExerciseStatus;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.EnumType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import jakarta.persistence.Id;

@Entity
@Table(name = "session_exercises")
public class SessionExerciseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private WorkoutSessionEntity session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    private ExerciseEntity exercise;

    // Estado del ejercicio en la sesión
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ExerciseStatus status; // PENDING, IN_PROGRESS, COMPLETED, SKIPPED

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "personal_notes", length = 2000)
    private String personalNotes;
}