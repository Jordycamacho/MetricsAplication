package com.fitapp.backend.workout.aplication.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to save a completed workout session")
public class SaveWorkoutSessionRequest {
    
    @NotNull(message = "Routine ID is required")
    @JsonProperty("routineId")
    @Schema(description = "ID of the routine executed", example = "123")
    private Long routineId;
    
    @NotNull(message = "Start time is required")
    @JsonProperty("startTime")
    @Schema(description = "When the workout started", example = "2026-04-02T10:00:00")
    private LocalDateTime startTime;
    
    @NotNull(message = "End time is required")
    @JsonProperty("endTime")
    @Schema(description = "When the workout finished", example = "2026-04-02T11:30:00")
    private LocalDateTime endTime;
    
    @Min(value = 1, message = "Performance score must be at least 1")
    @Max(value = 10, message = "Performance score must be at most 10")
    @JsonProperty("performanceScore")
    @Schema(description = "User's self-rated performance (1-10)", example = "8")
    private Integer performanceScore;
    
    @Valid
    @Builder.Default
    @JsonProperty("setExecutions")
    @Schema(description = "List of executed sets with their parameters")
    private List<SetExecutionRequest> setExecutions = new ArrayList<>();
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Individual set execution data")
    public static class SetExecutionRequest {
        
        @JsonProperty("setTemplateId")
        @Schema(description = "Reference to the planned set (null if ad-hoc)", example = "456")
        private Long setTemplateId;
        
        @JsonProperty("exerciseId")
        @Schema(description = "ID of the exercise performed", example = "789")
        private Long exerciseId;
        
        @NotNull(message = "Position is required")
        @JsonProperty("position")
        @Schema(description = "Order of this set in the workout", example = "1")
        private Integer position;
        
        @JsonProperty("setType")
        @Schema(description = "Type of set", example = "NORMAL")
        private String setType;
        
        @JsonProperty("status")
        @Schema(description = "Completion status", example = "COMPLETED")
        private String status;
        
        @JsonProperty("startedAt")
        @Schema(description = "When this set started")
        private LocalDateTime startedAt;
        
        @JsonProperty("completedAt")
        @Schema(description = "When this set completed")
        private LocalDateTime completedAt;
        
        @JsonProperty("actualRestSeconds")
        @Schema(description = "Actual rest taken after this set (seconds)", example = "90")
        private Integer actualRestSeconds;
        
        @JsonProperty("notes")
        @Schema(description = "User notes for this set", example = "Felt strong")
        private String notes;
        
        @Valid
        @Builder.Default
        @JsonProperty("parameters")
        @Schema(description = "Actual values recorded for each parameter")
        private List<ParameterValueRequest> parameters = new ArrayList<>();
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Parameter value recorded during set execution")
    public static class ParameterValueRequest {
        
        @NotNull(message = "Parameter ID is required")
        @JsonProperty("parameterId")
        @Schema(description = "ID of the parameter", example = "1")
        private Long parameterId;
        
        @JsonProperty("numericValue")
        @Schema(description = "Numeric value (weight, distance, etc.)", example = "80.0")
        private Double numericValue;
        
        @JsonProperty("integerValue")
        @Schema(description = "Integer value (reps, RPE, etc.)", example = "12")
        private Integer integerValue;
        
        @JsonProperty("durationValue")
        @Schema(description = "Duration in milliseconds", example = "60000")
        private Long durationValue;
        
        @JsonProperty("stringValue")
        @Schema(description = "Text value (notes, technique cues)")
        private String stringValue;
    }
}