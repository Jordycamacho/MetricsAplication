package com.fitapp.backend.application.dto.routine;

import java.util.Map;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ExerciseInRoutineRequest {
    @NotNull
    private Long exerciseId;

    @Min(1)
    private int sets;

    private String targetReps;

    private Double targetWeight;

    @Min(0)
    private Integer restIntervalSeconds;

    private Map<String, Object> customParameters;

    // Getters and setters
}