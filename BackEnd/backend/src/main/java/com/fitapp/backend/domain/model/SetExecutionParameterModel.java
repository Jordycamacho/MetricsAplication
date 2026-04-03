package com.fitapp.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetExecutionParameterModel {
    private Long id;
    private Long setExecutionId;
    private Long parameterId;
    private Double numericValue;
    private Integer integerValue;
    private Long durationValue;
    private String stringValue;
    
    @Builder.Default
    private boolean isPersonalRecord = false;
    
    /**
     * Devuelve el valor como Double para comparaciones uniformes.
     */
    public Double getValueAsDouble() {
        if (numericValue != null) return numericValue;
        if (integerValue != null) return integerValue.doubleValue();
        if (durationValue != null) return durationValue.doubleValue();
        return null;
    }
    
    /**
     * Verifica si tiene algún valor poblado.
     */
    public boolean hasValue() {
        return numericValue != null || integerValue != null || 
               durationValue != null || stringValue != null;
    }
}