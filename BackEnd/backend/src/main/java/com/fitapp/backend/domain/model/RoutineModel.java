package com.fitapp.backend.domain.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RoutineModel {
    private Long id;
    private String name;
    private String description;
    private Boolean isActive;
    private Integer estimatedDuration;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long userId;
    private Long sportId;
    private List<RoutineExerciseModel> exercises;
}