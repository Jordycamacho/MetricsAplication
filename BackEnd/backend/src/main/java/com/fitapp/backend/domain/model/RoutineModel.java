package com.fitapp.backend.domain.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.fitapp.backend.infrastructure.persistence.entity.enums.DayOfWeek;

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
    private Set<DayOfWeek> trainingDays;
    private String goal;
    private Integer difficultyLevel;
    private Integer weeksDuration;
    private Integer sessionsPerWeek;
    private String equipmentNeeded;
}