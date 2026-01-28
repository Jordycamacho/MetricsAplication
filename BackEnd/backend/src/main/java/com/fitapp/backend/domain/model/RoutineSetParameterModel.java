package com.fitapp.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutineSetParameterModel {
    private Long id;
    private Long setTemplateId;
    private Long parameterId;
    private Double numericValue;
    private Long durationValue;
    private Integer integerValue;
    private Double minValue;
    private Double maxValue;
    
    public void validate() {
        int valueCount = 0;
        if (numericValue != null) valueCount++;
        if (durationValue != null) valueCount++;
        if (integerValue != null) valueCount++;
        
        if (valueCount == 0) {
            throw new IllegalArgumentException("At least one value must be provided");
        }
        
        if (minValue != null && maxValue != null && minValue > maxValue) {
            throw new IllegalArgumentException("Min value cannot be greater than max value");
        }
    }
    
    public void logModelData(String operation) {
        System.out.println("RoutineSetParameterModel " + operation + ":");
        System.out.println("ID: " + id);
        System.out.println("Set Template ID: " + setTemplateId);
        System.out.println("Parameter ID: " + parameterId);
        System.out.println("Numeric Value: " + numericValue);
        System.out.println("Duration Value: " + durationValue);
        System.out.println("Integer Value: " + integerValue);
        System.out.println("Min Value: " + minValue);
        System.out.println("Max Value: " + maxValue);
    }
}