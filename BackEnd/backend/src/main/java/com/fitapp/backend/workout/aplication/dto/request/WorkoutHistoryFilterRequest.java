package com.fitapp.backend.workout.aplication.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Filters for workout history queries")
public class WorkoutHistoryFilterRequest {
    
    @JsonProperty("routineId")
    @Schema(description = "Filter by specific routine", example = "123")
    private Long routineId;
    
    @JsonProperty("fromDate")
    @Schema(description = "Start date (inclusive)", example = "2026-01-01")
    private LocalDate fromDate;
    
    @JsonProperty("toDate")
    @Schema(description = "End date (inclusive)", example = "2026-04-02")
    private LocalDate toDate;
    
    @JsonProperty("minPerformanceScore")
    @Schema(description = "Minimum performance score", example = "7")
    private Integer minPerformanceScore;
    
    @JsonProperty("maxPerformanceScore")
    @Schema(description = "Maximum performance score", example = "10")
    private Integer maxPerformanceScore;
    
    @Builder.Default
    @JsonProperty("sortBy")
    @Schema(description = "Sort field: startTime, performanceScore, totalVolume, durationSeconds")
    private String sortBy = "startTime";
    
    @Builder.Default
    @JsonProperty("sortDirection")
    @Schema(description = "Sort direction: ASC, DESC")
    private String sortDirection = "DESC";
}