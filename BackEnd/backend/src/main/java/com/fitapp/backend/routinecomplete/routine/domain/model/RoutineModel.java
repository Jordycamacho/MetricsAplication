package com.fitapp.backend.routinecomplete.routine.domain.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.fitapp.backend.domain.model.RoutineExerciseModel;
import com.fitapp.backend.infrastructure.persistence.entity.enums.DayOfWeek;

@Data
public class RoutineModel {
    private Long id;
    private String name;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastUsedAt;
    private Long userId;
    private Long sportId;
    private List<RoutineExerciseModel> exercises;
    private Set<DayOfWeek> trainingDays;
    private String goal;
    private Integer sessionsPerWeek;
    private Long originalRoutineId;
    private String version;
    private Long packageId;
    private UUID exportKey;
    private Integer timesPurchased;
}