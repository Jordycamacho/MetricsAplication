package com.fitapp.backend.infrastructure.persistence.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fitapp.backend.infrastructure.config.HashMapConverter;
import com.fitapp.backend.infrastructure.persistence.entity.enums.ParameterType;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "sports")
public class SportEntity extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String name;

    @Convert(converter = HashMapConverter.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> parameterTemplate = new HashMap<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "sport_parameter_constraints", joinColumns = @JoinColumn(name = "sport_id"))
    @Column(name = "parameter_type")
    @Enumerated(EnumType.STRING)
    private Set<ParameterType> allowedParameters = new HashSet<>();

    // En Sport: Permitir parámetros personalizados además de los enum
    @ElementCollection
    @CollectionTable(name = "sport_custom_parameters")
    private List<String> customAllowedParameters; // Ej: "altura_muro", "dificultad"
}