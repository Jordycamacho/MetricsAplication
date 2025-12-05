package com.fitapp.backend.application.dto.routine;

import lombok.Data;

@Data
public class RoutineExerciseResponse {
    private Long id;
    private Long exerciseId;
    private String exerciseName;
    private Integer sets;
    private String targetReps;
    private Double targetWeight;
    private Integer restIntervalSeconds;
}