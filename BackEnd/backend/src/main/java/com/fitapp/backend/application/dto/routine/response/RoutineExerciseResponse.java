package com.fitapp.backend.application.dto.routine.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoutineExerciseResponse {
    private Long id;
    private Long exerciseId;
    private String exerciseName;
    private Integer sets;
    private String targetReps;
    private Double targetWeight;
    private Integer restIntervalSeconds;
    private Integer position;
}