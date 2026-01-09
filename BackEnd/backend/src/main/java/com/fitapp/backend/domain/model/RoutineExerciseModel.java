package com.fitapp.backend.domain.model;

import lombok.Data;
import java.util.List;

@Data
public class RoutineExerciseModel {
    private Long id;
    private Long exerciseId;
    private Long routineId;
    private Integer position;
    private Integer restAfterExercise;
    private List<RoutineExerciseParameterModel> targetParameters;
    private List<RoutineSetTemplateModel> sets;
}