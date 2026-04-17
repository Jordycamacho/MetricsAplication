package com.fitapp.backend.infrastructure.persistence.entity;

import com.fitapp.backend.parameter.infrastructure.persistence.entity.CustomParameterEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "routine_exercise_parameters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoutineExerciseParameterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_exercise_id", nullable = false)
    private RoutineExerciseEntity routineExercise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parameter_id", nullable = false)
    private CustomParameterEntity parameter;

    // Valor objetivo (NO valor real)
    private Double numericValue;
    private Integer integerValue;
    private Long durationValue;
    private String stringValue;

    // Rango opcional (ej: 8–12 reps totales)
    @Column(name = "min_value", columnDefinition = "double precision")
    private Double minValue;
    
    @Column(name = "max_value", columnDefinition = "double precision")
    private Double maxValue;
    
    @Column(name = "default_value", columnDefinition = "double precision")
    private Double defaultValue;
}
