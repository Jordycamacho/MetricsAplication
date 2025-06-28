package com.fitapp.backend.infrastructure.persistence.entity;

import com.fitapp.backend.infrastructure.persistence.entity.enums.ParameterType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "exercise_parameters")
public class ExerciseParameterEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_id", nullable = false)
    private ExerciseSetEntity set;

    @Enumerated(EnumType.STRING)
    @Column(name = "parameter_type", nullable = false)
    private ParameterType type;

    @Column(nullable = false)
    private Double value;

    @Column(name = "unit", length = 10)
    private String unit; // kg, min, km, etc.
}