package com.fitapp.backend.application.dto.routine;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RoutineResponse {
    private Long id;
    private String name;
    private String description;
    private Long sportId;
    private String sportName;
    private Boolean isActive;
    private Integer estimatedDuration;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<RoutineExerciseResponse> exercises;
}