package com.fitapp.backend.domain.model;

import lombok.Data;
import java.util.List;
import com.fitapp.backend.infrastructure.persistence.entity.enums.DayOfWeek;

@Data
public class RoutineExerciseModel {
    private Long id;
    private Long exerciseId;
    private Long routineId;
    private Integer position;
    private Integer sessionNumber = 1;
    private DayOfWeek dayOfWeek;
    private Integer sessionOrder;
    private Integer restAfterExercise;
    private List<RoutineExerciseParameterModel> targetParameters;
    private List<RoutineSetTemplateModel> sets;
}