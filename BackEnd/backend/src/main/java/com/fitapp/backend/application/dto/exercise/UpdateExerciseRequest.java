package com.fitapp.backend.application.dto.exercise;

import java.util.Map;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateExerciseRequest {
    @Size(max = 100, message = "Exercise name cannot exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private Long sportId;

    private Map<String, String> parameterTemplates;
}