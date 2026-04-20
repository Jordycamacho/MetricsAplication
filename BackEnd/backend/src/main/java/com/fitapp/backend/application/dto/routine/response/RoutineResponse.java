package com.fitapp.backend.application.dto.routine.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.ArrayList;

import com.fitapp.backend.infrastructure.persistence.entity.enums.DayOfWeek;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoutineResponse {
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("sportId")
    private Long sportId;
    
    @JsonProperty("sportName")
    private String sportName;
    
    @JsonProperty("isActive")
    private Boolean isActive;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
    
    @JsonProperty("lastUsedAt")
    private LocalDateTime lastUsedAt;
    
    @Builder.Default
    @JsonProperty("exercises")
    private List<RoutineExerciseResponse> exercises = new ArrayList<>();
    
    @JsonProperty("trainingDays")
    private Set<DayOfWeek> trainingDays;
    
    @JsonProperty("goal")
    private String goal;
    
    @JsonProperty("sessionsPerWeek")
    private Integer sessionsPerWeek;
    
    @JsonProperty("originalRoutineId")
    private Long originalRoutineId;
    
    @JsonProperty("version")
    private String version;
    
    @JsonProperty("packageId")
    private Long packageId;
    
    @JsonProperty("exportKey")
    private UUID exportKey;
    
    @JsonProperty("timesPurchased")
    private Integer timesPurchased;
}