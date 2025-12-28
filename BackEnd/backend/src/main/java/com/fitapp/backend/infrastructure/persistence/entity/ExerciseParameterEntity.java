package com.fitapp.backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "exercise_parameters",
       indexes = {
           @Index(name = "idx_exercise_param_set", columnList = "set_id"),
           @Index(name = "idx_exercise_param_custom", columnList = "custom_parameter_id")
       })
@Slf4j
public class ExerciseParameterEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_id", nullable = false)
    private ExerciseSetEntity set;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custom_parameter_id", nullable = false)
    private CustomParameterEntity customParameter;

    @Column(name = "value_text", length = 500)
    private String valueText;
    
    @Column(name = "value_number")
    private Double valueNumber;
    
    @Column(name = "value_integer")
    private Integer valueInteger;
    
    @Column(name = "value_boolean")
    private Boolean valueBoolean;
    
    @Column(name = "unit", length = 20)
    private String unit; // Puede sobrescribir la unidad del parámetro
    
    @Column(name = "notes", length = 1000)
    private String notes;
    
    @PrePersist
    @PreUpdate
    public void validateValues() {
        log.debug("EXERCISE_PARAMETER_VALIDATING | id={} | paramId={}", 
                 id, customParameter != null ? customParameter.getId() : null);
        
        // Validar que solo un valor esté establecido según el tipo
        if (customParameter != null) {
            switch (customParameter.getParameterType()) {
                case NUMBER:
                    if (valueNumber == null) {
                        log.warn("EXERCISE_PARAMETER_INVALID_VALUE | type=NUMBER | valueNumber is null");
                    }
                    break;
                case INTEGER:
                    if (valueInteger == null) {
                        log.warn("EXERCISE_PARAMETER_INVALID_VALUE | type=INTEGER | valueInteger is null");
                    }
                    break;
                case TEXT:
                    if (valueText == null || valueText.trim().isEmpty()) {
                        log.warn("EXERCISE_PARAMETER_INVALID_VALUE | type=TEXT | valueText is empty");
                    }
                    break;
                case BOOLEAN:
                    if (valueBoolean == null) {
                        log.warn("EXERCISE_PARAMETER_INVALID_VALUE | type=BOOLEAN | valueBoolean is null");
                    }
                    break;
            }
        }
    }
    
    public Object getValue() {
        if (customParameter == null) return null;
        
        switch (customParameter.getParameterType()) {
            case NUMBER: return valueNumber;
            case INTEGER: return valueInteger;
            case TEXT: return valueText;
            case BOOLEAN: return valueBoolean;
            default: return null;
        }
    }
    
    public void setValue(Object value) {
        if (customParameter == null) return;
        
        switch (customParameter.getParameterType()) {
            case NUMBER:
                if (value instanceof Number) {
                    this.valueNumber = ((Number) value).doubleValue();
                } else if (value instanceof String) {
                    try {
                        this.valueNumber = Double.parseDouble((String) value);
                    } catch (NumberFormatException e) {
                        log.error("Failed to parse number value: {}", value, e);
                    }
                }
                break;
            case INTEGER:
                if (value instanceof Number) {
                    this.valueInteger = ((Number) value).intValue();
                } else if (value instanceof String) {
                    try {
                        this.valueInteger = Integer.parseInt((String) value);
                    } catch (NumberFormatException e) {
                        log.error("Failed to parse integer value: {}", value, e);
                    }
                }
                break;
            case TEXT:
                this.valueText = value != null ? value.toString() : null;
                break;
            case BOOLEAN:
                if (value instanceof Boolean) {
                    this.valueBoolean = (Boolean) value;
                } else if (value instanceof String) {
                    this.valueBoolean = Boolean.parseBoolean((String) value);
                } else if (value instanceof Number) {
                    this.valueBoolean = ((Number) value).intValue() != 0;
                }
                break;
        }
    }
}