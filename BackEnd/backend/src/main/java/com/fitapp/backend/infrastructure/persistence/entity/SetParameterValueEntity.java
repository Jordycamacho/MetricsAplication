package com.fitapp.backend.infrastructure.persistence.entity;

import com.fitapp.backend.infrastructure.persistence.entity.enums.ValueType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "set_parameter_values")
public class SetParameterValueEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_id", nullable = false)
    private ExerciseSetEntity exerciseSet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parameter_id", nullable = false)
    private CustomParameterEntity parameter;

    // Valor según el tipo de parámetro
    @Column(name = "string_value")
    private String stringValue;

    @Column(name = "numeric_value")
    private Double numericValue;

    @Column(name = "integer_value")
    private Integer integerValue;

    @Column(name = "boolean_value")
    private Boolean booleanValue;

    @Column(name = "duration_value") // en segundos
    private Long durationValue;

    // Unidad personalizada (puede sobrescribir la del parámetro)
    @Column(name = "unit_override")
    private String unitOverride;

    // Rango para valores (ej: 8-12 repeticiones)
    @Column(name = "min_value")
    private Double minValue;

    @Column(name = "max_value")
    private Double maxValue;

    // Estado del valor (objetivo vs real)
    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", nullable = false)
    private ValueType valueType; // TARGET, ACTUAL

    @Column(name = "is_pr")
    private Boolean isPR = false;
}