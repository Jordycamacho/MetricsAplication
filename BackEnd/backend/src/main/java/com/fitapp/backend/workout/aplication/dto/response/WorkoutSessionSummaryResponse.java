package com.fitapp.backend.workout.aplication.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Summary of a workout session for list views")
public class WorkoutSessionSummaryResponse {
    
    @JsonProperty("id")
    @Schema(description = "Session ID", example = "1")
    private Long id;
    
    @JsonProperty("routineId")
    @Schema(description = "ID of the routine executed", example = "123")
    private Long routineId;
    
    @JsonProperty("routineName")
    @Schema(description = "Name of the routine", example = "Push Day A")
    private String routineName;
    
    @JsonProperty("startTime")
    @Schema(description = "When the workout started")
    private LocalDateTime startTime;
    
    @JsonProperty("endTime")
    @Schema(description = "When the workout finished")
    private LocalDateTime endTime;
    
    @JsonProperty("durationSeconds")
    @Schema(description = "Total workout duration in seconds", example = "5400")
    private Long durationSeconds;
    
    @JsonProperty("performanceScore")
    @Schema(description = "User's self-rated performance (1-10)", example = "8")
    private Integer performanceScore;
    
    @JsonProperty("totalVolume")
    @Schema(description = "Total volume (weight × reps)", example = "12500.5")
    private Double totalVolume;
    
    @JsonProperty("exerciseCount")
    @Schema(description = "Number of exercises performed", example = "6")
    private Integer exerciseCount;
    
    @JsonProperty("setCount")
    @Schema(description = "Total number of sets performed", example = "24")
    private Integer setCount;
}