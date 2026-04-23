package com.fitapp.backend.routinecomplete.routineexercise.aplication.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoutineExerciseParameterResponse {
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("parameterId")
    private Long parameterId;
    
    @JsonProperty("parameterName")
    private String parameterName;
    
    @JsonProperty("parameterType")
    private String parameterType;
    
    @JsonProperty("numericValue")
    private Double numericValue;
    
    @JsonProperty("integerValue")
    private Integer integerValue;
    
    @JsonProperty("durationValue")
    private Long durationValue;
    
    @JsonProperty("stringValue")
    private String stringValue;
    
    @JsonProperty("minValue")
    private Double minValue;
    
    @JsonProperty("maxValue")
    private Double maxValue;
    
    @JsonProperty("defaultValue")
    private Double defaultValue;
}