package com.fitapp.backend.application.dto.routine.response;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoutineExerciseParameterResponse {
    private Long id;
    private Long parameterId;
    private String parameterName;
    private String parameterType;
    private Double numericValue;
    private Integer integerValue;
    private Long durationValue;
    private String stringValue;
    private Double minValue;
    private Double maxValue;
    private Double defaultValue;
}