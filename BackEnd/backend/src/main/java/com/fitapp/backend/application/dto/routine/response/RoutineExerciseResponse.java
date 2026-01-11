package com.fitapp.backend.application.dto.routine.response;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

import com.fitapp.backend.infrastructure.persistence.entity.enums.DayOfWeek;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoutineExerciseResponse {
    private Long id;
    private Long exerciseId;
    private String exerciseName;
    private Integer position;
    private Integer sessionNumber;
    private DayOfWeek dayOfWeek;
    private Integer sessionOrder;
    private Integer restAfterExercise;
    private Integer sets;
    private String targetReps;
    private Double targetWeight;
    private List<RoutineExerciseParameterResponse> targetParameters;
    private List<RoutineSetTemplateResponse> setsTemplate;
}