package com.fitapp.backend.workout.aplication.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Exercise performed in a workout session")
public class SessionExerciseResponse {
    
    @JsonProperty("id")
    @Schema(description = "Session exercise ID", example = "1")
    private Long id;
    
    @JsonProperty("exerciseId")
    @Schema(description = "ID of the exercise", example = "789")
    private Long exerciseId;
    
    @JsonProperty("exerciseName")
    @Schema(description = "Name of the exercise", example = "Bench Press")
    private String exerciseName;
    
    @JsonProperty("status")
    @Schema(description = "Completion status", example = "COMPLETED")
    private String status;
    
    @JsonProperty("startedAt")
    @Schema(description = "When this exercise started")
    private LocalDateTime startedAt;
    
    @JsonProperty("completedAt")
    @Schema(description = "When this exercise completed")
    private LocalDateTime completedAt;
    
    @JsonProperty("personalNotes")
    @Schema(description = "User notes for this exercise")
    private String personalNotes;
    
    @Builder.Default
    @JsonProperty("sets")
    @Schema(description = "Sets performed for this exercise")
    private List<SetExecutionResponse> sets = new ArrayList<>();
}