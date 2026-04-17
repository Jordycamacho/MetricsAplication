package com.fitapp.backend.parameter.domain.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import com.fitapp.backend.infrastructure.persistence.entity.enums.MetricAggregation;
import com.fitapp.backend.parameter.domain.exception.InvalidParameterException;
import com.fitapp.backend.parameter.infrastructure.persistence.entity.ParameterType;

@Data
@Slf4j
public class CustomParameterModel {
    private Long id;
    private String name;
    private String description;
    private ParameterType parameterType;
    private String unit;
    private Boolean isGlobal;
    private Boolean isActive;
    private Long ownerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer usageCount;
    private boolean isFavorite;
    private MetricAggregation metricAggregation;
    private boolean isTrackable;

    public void logModelData(String operation) {
        log.info("PARAMETER_MODEL_{} | id={} | name={} | type={} | isGlobal={} | trackable={} | aggregation={}", 
                operation.toUpperCase(), id, name, parameterType, isGlobal, isTrackable, metricAggregation);
        log.debug("PARAMETER_MODEL_DETAILS | unit={} | usageCount={} | isFavorite={}", 
                unit, usageCount, isFavorite);
    }
    
    /**
     * Valida el formato del nombre y la coherencia de los campos
     */
    public void validateFormat() {
        validateName();
        validateTypeUnitConsistency();
        validateTrackableConsistency();
        validatePercentageRange();
    }
    
    private void validateName() {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidParameterException("El nombre del parámetro no puede estar vacío");
        }
        if (name.length() > 100) {
            throw new InvalidParameterException("El nombre del parámetro no puede exceder 100 caracteres");
        }
        if (!name.matches("^[a-z]+([A-Z][a-z]*)*$")) {
            log.warn("PARAMETER_NAME_FORMAT_WARNING | name={} | format not camelCase, may cause frontend issues", name);
        }
    }
    
    /**
     * Valida que el tipo de parámetro sea coherente con la unidad
     */
    private void validateTypeUnitConsistency() {
        if (parameterType == null) {
            throw new InvalidParameterException("El tipo de parámetro es obligatorio");
        }
        
        // Tipos que requieren unidad
        if ((parameterType == ParameterType.DISTANCE || parameterType == ParameterType.DURATION) 
            && (unit == null || unit.trim().isEmpty())) {
            throw new InvalidParameterException(
                String.format("El tipo %s requiere especificar una unidad", parameterType.getTypeName())
            );
        }
        
        // PERCENTAGE no debe tener unidad (siempre es %)
        if (parameterType == ParameterType.PERCENTAGE && unit != null && !unit.trim().isEmpty() && !unit.equals("%")) {
            log.warn("PARAMETER_PERCENTAGE_UNIT_OVERRIDE | name={} | unit={} | forcing to %", name, unit);
            unit = "%";
        }
        
        // BOOLEAN no debe tener unidad
        if (parameterType == ParameterType.BOOLEAN && unit != null && !unit.trim().isEmpty()) {
            log.warn("PARAMETER_BOOLEAN_UNIT_IGNORED | name={} | unit={}", name, unit);
            unit = null;
        }
    }
    
    /**
     * Valida que isTrackable sea coherente con metricAggregation
     */
    private void validateTrackableConsistency() {
        if (isTrackable && metricAggregation == null) {
            log.warn("PARAMETER_TRACKABLE_WITHOUT_AGGREGATION | name={} | defaulting to MAX", name);
            // Por defecto, si es trackable pero no tiene agregación, usar MAX
            metricAggregation = MetricAggregation.MAX;
        }
        
        if (!isTrackable && metricAggregation != null) {
            log.warn("PARAMETER_NOT_TRACKABLE_WITH_AGGREGATION | name={} | aggregation will be ignored", name);
        }
    }
    
    /**
     * Validación adicional para tipo PERCENTAGE
     */
    private void validatePercentageRange() {
        if (parameterType == ParameterType.PERCENTAGE) {
            // Solo advertencia, la validación real se hará en runtime cuando se guarden valores
            log.debug("PARAMETER_PERCENTAGE_TYPE | name={} | values should be validated in range 0-100", name);
        }
    }
    
    /**
     * Determina si este parámetro debería ser visible para cálculo de métricas
     */
    public boolean isMetricCalculable() {
        return isTrackable && metricAggregation != null && 
               parameterType != ParameterType.TEXT && parameterType != ParameterType.BOOLEAN;
    }
}