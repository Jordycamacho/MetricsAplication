package com.fitapp.backend.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "routine_exercise_parameters")
@Data
public class RoutineExerciseParameterEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_exercise_id", nullable = false)
    private RoutineExerciseEntity routineExercise;

    @Column(name = "parameter_name", nullable = false)
    private String parameterName;

    @Column(name = "parameter_type", nullable = false)
    private String parameterType;

    @Column(name = "target_value")
    private String targetValue;

    @Column(name = "min_value")
    private String minValue;

    @Column(name = "max_value")
    private String maxValue;

    @Column(name = "is_required")
    private Boolean isRequired = false;
}