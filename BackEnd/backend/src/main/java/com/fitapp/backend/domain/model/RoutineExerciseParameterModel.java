package com.fitapp.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutineExerciseParameterModel {
    private Long id;
    private Long routineExerciseId;
    private String parameterName;
    private String parameterType;
    private String targetValue;
    private String minValue;
    private String maxValue;
    private Boolean isRequired;
}