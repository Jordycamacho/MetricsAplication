package com.fitapp.backend.application.dto.workout.response;

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
@Schema(description = "Set execution details")
public class SetExecutionResponse {
    
    @JsonProperty("id")
    @Schema(description = "Set execution ID", example = "1")
    private Long id;
    
    @JsonProperty("setTemplateId")
    @Schema(description = "Reference to planned set (null if ad-hoc)", example = "456")
    private Long setTemplateId;
    
    @JsonProperty("position")
    @Schema(description = "Order of this set", example = "1")
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
    
    @JsonProperty("durationSeconds")
    @Schema(description = "Set duration in seconds", example = "45")
    private Long durationSeconds;
    
    @JsonProperty("actualRestSeconds")
    @Schema(description = "Actual rest taken after this set", example = "90")
    private Integer actualRestSeconds;
    
    @JsonProperty("notes")
    @Schema(description = "User notes for this set")
    private String notes;
    
    @JsonProperty("volume")
    @Schema(description = "Volume for this set (weight × reps)", example = "960.0")
    private Double volume;
    
    @Builder.Default
    @JsonProperty("parameters")
    @Schema(description = "Parameter values recorded")
    private List<SetExecutionParameterResponse> parameters = new ArrayList<>();
}