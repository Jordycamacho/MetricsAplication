package com.fitapp.backend.application.dto.routine.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class AddExerciseToRoutineRequest {
    
    @NotNull(message = "Exercise ID is required")
    @JsonProperty("exerciseId")
    private Long exerciseId;
    
    @JsonProperty("sessionNumber")
    private Integer sessionNumber = 1;
    
    @JsonProperty("dayOfWeek")
    private String dayOfWeek;
    
    @JsonProperty("sessionOrder")
    private Integer sessionOrder;
    
    @JsonProperty("restAfterExercise")
    private Integer restAfterExercise;
    
    @JsonProperty("targetParameters")
    private List<ExerciseParameterRequest> targetParameters;
    
    @JsonProperty("sets")
    private List<SetTemplateRequest> sets;
        
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
    
    // ─────────────────────────────────────────────────────────────────────────
    
    @Data
    public static class ExerciseParameterRequest {
        @NotNull @JsonProperty("parameterId") private Long parameterId;
        @JsonProperty("numericValue") private Double numericValue;
        @JsonProperty("integerValue") private Integer integerValue;
        @JsonProperty("durationValue") private Long durationValue;
        @JsonProperty("stringValue") private String stringValue;
        @JsonProperty("minValue") private Double minValue;
        @JsonProperty("maxValue") private Double maxValue;
        @JsonProperty("defaultValue") private Double defaultValue;
    }
    
    @Data
    public static class SetTemplateRequest {
        @NotNull @JsonProperty("position") private Integer position;
        @JsonProperty("setType") private String setType = "NORMAL";
        @JsonProperty("restAfterSet") private Integer restAfterSet;
        @JsonProperty("subSetNumber") private Integer subSetNumber;
        @JsonProperty("groupId") private String groupId;
        @JsonProperty("parameters") private List<SetParameterRequest> parameters;
    }
    
    @Data
    public static class SetParameterRequest {
        @NotNull @JsonProperty("parameterId") private Long parameterId;
        @JsonProperty("numericValue") private Double numericValue;
        @JsonProperty("durationValue") private Long durationValue;
        @JsonProperty("integerValue") private Integer integerValue;
        @JsonProperty("repetitions") private Integer repetitions;
    }
}