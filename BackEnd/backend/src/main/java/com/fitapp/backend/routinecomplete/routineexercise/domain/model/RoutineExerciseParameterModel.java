package com.fitapp.backend.routinecomplete.routineexercise.domain.model;

import lombok.Data;

@Data
public class RoutineExerciseParameterModel {
    private Long id;
    private Long parameterId;
    private Double numericValue;
    private Integer integerValue;
    private Long durationValue;
    private String stringValue;
    private Double minValue;
    private Double maxValue;
    private Double defaultValue;
}