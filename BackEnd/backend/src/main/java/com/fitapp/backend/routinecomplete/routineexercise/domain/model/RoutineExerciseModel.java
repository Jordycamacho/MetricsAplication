package com.fitapp.backend.routinecomplete.routineexercise.domain.model;

import com.fitapp.backend.infrastructure.persistence.entity.enums.DayOfWeek;
import com.fitapp.backend.routinecomplete.routinesetemplate.domain.model.RoutineSetTemplateModel;

import lombok.Data;
import java.util.List;

@Data
public class RoutineExerciseModel {
    private Long id;
    private Long routineId;
    private Long exerciseId;
    private Integer position;
    private Integer sessionNumber;
    private DayOfWeek dayOfWeek;
    private Integer sessionOrder;
    private Integer restAfterExercise;
    private List<RoutineExerciseParameterModel> targetParameters;
    //v2 
    private List<RoutineSetTemplateModel> sets;    
    private String circuitGroupId;
    private Integer circuitRoundCount;
    private String superSetGroupId;
    private Integer amrapDurationSeconds;
    private Integer emomIntervalSeconds;
    private Integer emomTotalRounds;
    private Integer tabataWorkSeconds;
    private Integer tabataRestSeconds;
    private Integer tabataRounds;
    private String notes;
}