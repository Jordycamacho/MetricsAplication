package com.fitapp.backend.workout.aplication.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Filters for workout history queries")
public class WorkoutHistoryFilterRequest {

    /** Allowed sort fields — prevents arbitrary field injection into ORDER BY. */
    public static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("startTime", "performanceScore", "totalVolume", "durationSeconds");

    @JsonProperty("routineId")
    @Schema(description = "Filter by specific routine", example = "123")
    private Long routineId;

    @JsonProperty("fromDate")
    @Schema(description = "Start date inclusive (ISO-8601)", example = "2026-01-01")
    private LocalDate fromDate;

    @JsonProperty("toDate")
    @Schema(description = "End date inclusive (ISO-8601)", example = "2026-04-02")
    private LocalDate toDate;

    @JsonProperty("minPerformanceScore")
    @Schema(description = "Minimum performance score (1-10)", example = "7")
    private Integer minPerformanceScore;

    @JsonProperty("maxPerformanceScore")
    @Schema(description = "Maximum performance score (1-10)", example = "10")
    private Integer maxPerformanceScore;

    @Builder.Default
    @Pattern(regexp = "startTime|performanceScore|totalVolume|durationSeconds",
             message = "sortBy must be one of: startTime, performanceScore, totalVolume, durationSeconds")
    @JsonProperty("sortBy")
    @Schema(description = "Sort field: startTime | performanceScore | totalVolume | durationSeconds",
            example = "startTime")
    private String sortBy = "startTime";

    @Builder.Default
    @Pattern(regexp = "ASC|DESC", flags = Pattern.Flag.CASE_INSENSITIVE, message = "sortDirection must be ASC or DESC")
    @JsonProperty("sortDirection")
    @Schema(description = "Sort direction: ASC | DESC", example = "DESC")
    private String sortDirection = "DESC";

    /**
     * Returns a safe sortBy value, falling back to "startTime" if the requested
     * field is not in the allowlist. This is a secondary guard on top of @Pattern.
     */
    public String getSafeSortBy() {
        return ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "startTime";
    }
}