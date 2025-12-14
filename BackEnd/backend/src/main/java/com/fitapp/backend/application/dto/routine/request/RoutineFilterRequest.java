package com.fitapp.backend.application.dto.routine.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoutineFilterRequest {
    private Long sportId;
    
    @Size(max = 100, message = "Name filter cannot exceed 100 characters")
    private String name;
    
    private Boolean isActive;
    
    @Pattern(regexp = "^(name|createdAt|updatedAt)$", 
             message = "Sort by must be one of: name, createdAt, updatedAt")
    private String sortBy = "createdAt";
    
    @Pattern(regexp = "^(ASC|DESC)$", 
             message = "Sort direction must be ASC or DESC")
    private String sortDirection = "DESC";
}