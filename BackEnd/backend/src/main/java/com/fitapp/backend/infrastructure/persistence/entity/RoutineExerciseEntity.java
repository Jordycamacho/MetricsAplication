package com.fitapp.backend.infrastructure.persistence.entity;

import java.time.Duration;

import com.fitapp.backend.domain.service.DurationToLongConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "routine_exercises")
public class RoutineExerciseEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id", nullable = false)
    private RoutineEntity routine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    private ExerciseEntity exercise;

    @Column(nullable = false)
    private int position;

    @Convert(converter = DurationToLongConverter.class)
    @Column(name = "rest_interval")
    private Duration restInterval;
}
