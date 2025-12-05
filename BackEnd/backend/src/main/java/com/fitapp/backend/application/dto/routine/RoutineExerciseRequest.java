package com.fitapp.backend.application.dto.routine;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class RoutineExerciseRequest {
    @NotNull(message = "Exercise ID is required")
    private Long exerciseId;
    
    @NotNull(message = "Number of sets is required")
    @PositiveOrZero(message = "Sets must be a positive number")
    private Integer sets;
    
    private String targetReps;
    private Double targetWeight;
    
    @Positive(message = "Rest interval must be a positive number")
    private Integer restIntervalSeconds;
}