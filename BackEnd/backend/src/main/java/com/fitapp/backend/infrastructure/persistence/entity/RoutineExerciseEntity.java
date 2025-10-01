package com.fitapp.backend.infrastructure.persistence.entity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Id;

import com.fitapp.backend.application.service.DurationToLongConverter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "routine_exercises")
public class RoutineExerciseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id", nullable = false)
    private RoutineEntity routine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    private ExerciseEntity exercise;

    @Column(nullable = false)
    private int sets;

    @Column(name = "rest_interval_seconds")
    private Integer restIntervalSeconds;

    @Column(name = "notes")
    private String notes;

    @Column(nullable = false)
    private int position;

    @OneToMany(mappedBy = "routineExercise", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoutineExerciseParameterEntity> parameters = new ArrayList<>();

    @Convert(converter = DurationToLongConverter.class)
    @Column(name = "rest_interval")
    private Duration restInterval;

    @Column(name = "target_reps")
    private String targetReps;

    @Column(name = "target_weight")
    private Double targetWeight;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
