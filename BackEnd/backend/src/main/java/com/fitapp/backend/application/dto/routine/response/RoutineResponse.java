package com.fitapp.backend.application.dto.routine.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitapp.backend.infrastructure.persistence.entity.enums.DayOfWeek;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoutineResponse {
    private Long id;
    private String name;
    private String description;
    @JsonProperty("sportId")
    private Long sportId;
    @JsonProperty("sportName")
    private String sportName;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Builder.Default
    private List<RoutineExerciseResponse> exercises= new ArrayList<>();
    
    private Set<DayOfWeek> trainingDays;
    private String goal;
    private Integer sessionsPerWeek;
}