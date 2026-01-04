package com.fitapp.backend.infrastructure.persistence.entity;

import java.util.Set;
import com.fitapp.backend.infrastructure.persistence.entity.enums.ExerciseType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "exercises")
public class ExerciseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id")
    private SportEntity sport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private UserEntity createdBy;

    // Múltiples categorías
    @ManyToMany
    @JoinTable(
        name = "exercise_category_mapping",
        joinColumns = @JoinColumn(name = "exercise_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<ExerciseCategoryEntity> categories;

    // Parámetros soportados (qué se puede medir)
    @ManyToMany
    @JoinTable(
        name = "exercise_supported_parameters",
        joinColumns = @JoinColumn(name = "exercise_id"),
        inverseJoinColumns = @JoinColumn(name = "parameter_id")
    )
    private Set<CustomParameterEntity> supportedParameters;

    // Tipo de ejercicio (para UI)
    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_type")
    private ExerciseType exerciseType; // SIMPLE, TIMED, WEIGHTED, MIXED, etc.
}
