package com.fitapp.backend.application.dto.exercise;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;

@Data
public class ExerciseResponse {
    private Long id;
    private String name;
    private String description;
    private Long sportId;
    private Long userId;
    private Boolean predefined;
    private Map<String, String> parameterTemplates;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}