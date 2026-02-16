package com.fitapp.backend.application.dto.routine.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class RoutineExerciseRequest {
    @NotNull(message = "Exercise ID is required")
    @JsonProperty("exerciseId")
    private Long exerciseId;
    
    @NotNull(message = "Number of sets is required")
    @PositiveOrZero(message = "Sets must be a positive number")
    @JsonProperty("sets")
    private Integer sets;
    
    @JsonProperty("targetReps")
    private String targetReps;
    
    @JsonProperty("targetWeight")
    private Double targetWeight;
    
    @Positive(message = "Rest interval must be a positive number")
    @JsonProperty("restIntervalSeconds")
    private Integer restIntervalSeconds;
}