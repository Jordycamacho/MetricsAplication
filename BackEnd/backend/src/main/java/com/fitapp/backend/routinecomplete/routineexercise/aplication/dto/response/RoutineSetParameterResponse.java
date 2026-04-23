package com.fitapp.backend.routinecomplete.routineexercise.aplication.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoutineSetParameterResponse {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("setTemplateId")
    private Long setTemplateId;
    
    @JsonProperty("parameterId")
    private Long parameterId;
    
    @JsonProperty("parameterName")
    private String parameterName;
    
    @JsonProperty("parameterType")
    private String parameterType;
    
    @JsonProperty("unit")
    private String unit;
    
    @JsonProperty("numericValue")
    private Double numericValue;
    
    @JsonProperty("durationValue")
    private Long durationValue;
    
    @JsonProperty("integerValue")
    private Integer integerValue;

    @JsonProperty("repetitions")
    private Integer repetitions;
}