package com.fitapp.backend.infrastructure.persistence.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.fitapp.backend.infrastructure.persistence.entity.enums.ExerciseStatus;

import jakarta.persistence.Id;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

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

    // Sets realizados en esta sesión
    @OneToMany(mappedBy = "sessionExercise", cascade = CascadeType.ALL)
    @OrderBy("setNumber ASC, subSetNumber ASC")
    private List<ExerciseSetEntity> sets;

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