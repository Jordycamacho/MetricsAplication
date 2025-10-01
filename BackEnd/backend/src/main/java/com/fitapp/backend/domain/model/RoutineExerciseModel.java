package com.fitapp.backend.domain.model;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutineExerciseModel {
    private Long id;
    private Long exerciseId;
    private Long routineId;
    private int  sets;
    private int position;
    private String notes;
    private String targetReps;
    private Double targetWeight;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer restIntervalSeconds;
    private List<RoutineExerciseParameterModel> parameters;
}

