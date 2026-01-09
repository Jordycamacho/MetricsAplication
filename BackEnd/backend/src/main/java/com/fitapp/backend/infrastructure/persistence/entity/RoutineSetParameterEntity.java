package com.fitapp.backend.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "routine_set_parameters")
public class RoutineSetParameterEntity {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private RoutineSetTemplateEntity set;

    @ManyToOne
    private CustomParameterEntity parameter;

    // Objetivo (no valor real)
    private Double numericValue;
    private Long durationValue;
    private Integer integerValue;

    private Double minValue;
    private Double maxValue;
}
