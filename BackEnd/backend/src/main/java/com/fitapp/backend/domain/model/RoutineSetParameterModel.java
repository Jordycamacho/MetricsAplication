package com.fitapp.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
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
    private Integer repetitions;
    
    public void validate() {
        int valueCount = 0;
        if (numericValue != null) valueCount++;
        if (durationValue != null) valueCount++;
        if (integerValue != null) valueCount++;
        
        if (valueCount == 0) {
            throw new IllegalArgumentException("At least one value must be provided");
        }
    }
    
public void logModelData(String operation) {
    log.debug("RoutineSetParameterModel {} | id={} | setTemplateId={} | parameterId={} | "
            + "repetitions={} | numericValue={} | durationValue={} | integerValue={}",
            operation, id, setTemplateId, parameterId,
            repetitions, numericValue, durationValue, integerValue);
}
}