package com.fitapp.backend.application.dto.exercise;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExerciseRequest {
    @NotNull
    private Long exerciseId;
    @NotNull
    private Integer sets;
    private Integer targetReps;
    private Double targetWeight;
    private Integer restIntervalSeconds;
}