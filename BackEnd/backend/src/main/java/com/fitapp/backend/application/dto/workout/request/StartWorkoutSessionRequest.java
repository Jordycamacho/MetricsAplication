package com.fitapp.backend.application.dto.workout.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to start a workout session from a routine")
public class StartWorkoutSessionRequest {
    
    @NotNull(message = "Routine ID is required")
    @JsonProperty("routineId")
    @Schema(description = "ID of the routine to execute", example = "123")
    private Long routineId;
}