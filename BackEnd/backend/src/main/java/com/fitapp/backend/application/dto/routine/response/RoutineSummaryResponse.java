package com.fitapp.backend.application.dto.routine.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

import com.fitapp.backend.infrastructure.persistence.entity.enums.DayOfWeek;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutineSummaryResponse {
    private Long id;
    private String name;
    private Long userId;
    private String userName;
    private String description;
    private Long sportId;
    private String sportName;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastUsedAt;
    private Set<DayOfWeek> trainingDays;
    private String goal;
    private Integer sessionsPerWeek;
    private Integer exerciseCount;
}