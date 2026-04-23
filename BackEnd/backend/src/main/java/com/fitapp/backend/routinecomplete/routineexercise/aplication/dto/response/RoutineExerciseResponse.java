package com.fitapp.backend.routinecomplete.routineexercise.aplication.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitapp.backend.application.dto.RoutineSetTemplate.response.RoutineSetTemplateResponse;
import com.fitapp.backend.infrastructure.persistence.entity.enums.DayOfWeek;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) 
public class RoutineExerciseResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("routineId")
    private Long routineId;

    @JsonProperty("exerciseId")
    private Long exerciseId;

    @JsonProperty("exerciseName")
    private String exerciseName;

    @JsonProperty("position")
    private Integer position;

    @JsonProperty("sessionNumber")
    private Integer sessionNumber;

    @JsonProperty("dayOfWeek")
    private DayOfWeek dayOfWeek;

    @JsonProperty("sessionOrder")
    private Integer sessionOrder;

    @JsonProperty("restAfterExercise")
    private Integer restAfterExercise;

    @JsonProperty("sets")
    private Integer sets;

    @JsonProperty("targetParameters")
    private List<RoutineExerciseParameterResponse> targetParameters;

    @JsonProperty("setsTemplate")
    private List<RoutineSetTemplateResponse> setsTemplate;
        
    @JsonProperty("circuitGroupId")
    private String circuitGroupId;
    
    @JsonProperty("circuitRoundCount")
    private Integer circuitRoundCount;
    
    @JsonProperty("superSetGroupId")
    private String superSetGroupId;
    
    @JsonProperty("amrapDurationSeconds")
    private Integer amrapDurationSeconds;
    
    @JsonProperty("emomIntervalSeconds")
    private Integer emomIntervalSeconds;
    
    @JsonProperty("emomTotalRounds")
    private Integer emomTotalRounds;
    
    @JsonProperty("tabataWorkSeconds")
    private Integer tabataWorkSeconds;
    
    @JsonProperty("tabataRestSeconds")
    private Integer tabataRestSeconds;
    
    @JsonProperty("tabataRounds")
    private Integer tabataRounds;
    
    @JsonProperty("notes")
    private String notes;
}