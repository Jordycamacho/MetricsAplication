package com.fitapp.backend.application.dto.routine.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoutineSetParameterResponse {
    private Long id;
    private Long parameterId;
    private String parameterName;
    private String parameterType;
    private Double numericValue;
    private Long durationValue;
    private Integer integerValue;
    private Double minValue;
    private Double maxValue;
}