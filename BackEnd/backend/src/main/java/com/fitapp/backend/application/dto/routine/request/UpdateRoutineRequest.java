package com.fitapp.backend.application.dto.routine.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoutineRequest {
    @Size(max = 100, message = "Routine name cannot exceed 100 characters")
    private String name;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    @JsonProperty("sportId")
    private Long sportId;
    
    @JsonProperty("trainingDays")  
    private List<String> trainingDays;
    
    @Size(max = 200, message = "Goal cannot exceed 200 characters")
    private String goal;

    @JsonProperty("sessionsPerWeek")
    private Integer sessionsPerWeek;
    
    @JsonProperty("isActive")
    private Boolean isActive = true;
}
