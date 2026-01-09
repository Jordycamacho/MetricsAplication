package com.fitapp.backend.domain.model;

import lombok.Data;

@Data
public class RoutineSetParameterModel {
    private Long id;
    private Long parameterId;
    private Double numericValue;
    private Long durationValue;
    private Integer integerValue;
    private Double minValue;
    private Double maxValue;
}