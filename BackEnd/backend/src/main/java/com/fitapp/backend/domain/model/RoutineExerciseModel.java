package com.fitapp.backend.domain.model;

import lombok.Data;

@Data
public class RoutineExerciseModel {
    private Long id;
    private Long exerciseId;
    private int  sets;
    private int position;
    private String targetReps;
    private Double targetWeight;
    private Integer restIntervalSeconds;
}

