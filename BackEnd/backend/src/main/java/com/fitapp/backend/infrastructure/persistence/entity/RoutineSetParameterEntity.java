package com.fitapp.backend.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "routine_set_parameters", indexes = {
    @Index(name = "idx_set_parameter_template", columnList = "set_template_id"),
    @Index(name = "idx_set_parameter_param",    columnList = "parameter_id")
})
public class RoutineSetParameterEntity {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "set_template_id", nullable = false)
    private RoutineSetTemplateEntity setTemplate;

    @ManyToOne
    @JoinColumn(name = "parameter_id", nullable = false)
    private CustomParameterEntity parameter;

    @Column(name = "repetitions")
    private Integer repetitions;

    @Column(name = "numeric_value")
    private Double numericValue;

    @Column(name = "duration_value")
    private Long durationValue;

    @Column(name = "integer_value")
    private Integer integerValue;

}
