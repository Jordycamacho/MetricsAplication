package com.fitapp.backend.application.dto.routine;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateRoutineRequest {
    @NotBlank(message = "Routine name is required")
    @Size(max = 100, message = "Routine name cannot exceed 100 characters")
    private String name;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    private Long sportId;
    
    @NotNull(message = "Routine must contain at least one exercise")
    private List<RoutineExerciseRequest> exercises;
}