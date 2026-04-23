package com.fitapp.backend.workout.aplication.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Parameter value recorded during set execution")
public class SetExecutionParameterResponse {
    
    @JsonProperty("id")
    @Schema(description = "Parameter execution ID", example = "1")
    private Long id;
    
    @JsonProperty("parameterId")
    @Schema(description = "ID of the parameter", example = "1")
    private Long parameterId;
    
    @JsonProperty("parameterName")
    @Schema(description = "Name of the parameter", example = "Weight")
    private String parameterName;
    
    @JsonProperty("parameterType")
    @Schema(description = "Type of parameter", example = "NUMBER")
    private String parameterType;
    
    @JsonProperty("unit")
    @Schema(description = "Unit of measurement", example = "kg")
    private String unit;
    
    @JsonProperty("numericValue")
    @Schema(description = "Numeric value", example = "80.0")
    private Double numericValue;
    
    @JsonProperty("integerValue")
    @Schema(description = "Integer value", example = "12")
    private Integer integerValue;
    
    @JsonProperty("durationValue")
    @Schema(description = "Duration in milliseconds", example = "60000")
    private Long durationValue;
    
    @JsonProperty("stringValue")
    @Schema(description = "Text value")
    private String stringValue;
    
    @JsonProperty("isPersonalRecord")
    @Schema(description = "Whether this is a new personal record", example = "false")
    private Boolean isPersonalRecord;
}