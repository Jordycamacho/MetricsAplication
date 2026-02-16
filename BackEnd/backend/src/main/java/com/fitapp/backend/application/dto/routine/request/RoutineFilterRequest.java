package com.fitapp.backend.application.dto.routine.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoutineFilterRequest {
    @JsonProperty("sportId")
    private Long sportId;
    
    @Size(max = 100, message = "Name filter cannot exceed 100 characters")
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("isActive")
    private Boolean isActive;
    
    @Pattern(regexp = "^(name|createdAt|updatedAt)$", 
             message = "Sort by must be one of: name, createdAt, updatedAt")
    @JsonProperty("sortBy")
    private String sortBy = "createdAt";
    
    @Pattern(regexp = "^(ASC|DESC)$", 
             message = "Sort direction must be ASC or DESC")
    @JsonProperty("sortDirection")
    private String sortDirection = "DESC";
}