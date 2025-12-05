package com.fitapp.backend.application.dto.routine;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.fitapp.backend.infrastructure.persistence.entity.enums.DayOfWeek;

@Data
public class RoutineResponse {
    private Long id;
    private String name;
    private String description;
    private Long sportId;
    private String sportName;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<RoutineExerciseResponse> exercises;
    private Set<DayOfWeek> trainingDays;
    private String goal;
    private Integer sessionsPerWeek;
}