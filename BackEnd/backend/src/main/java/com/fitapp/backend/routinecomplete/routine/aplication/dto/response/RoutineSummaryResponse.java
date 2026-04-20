package com.fitapp.backend.routinecomplete.routine.aplication.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import com.fitapp.backend.infrastructure.persistence.entity.enums.DayOfWeek;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutineSummaryResponse {
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("userId")
    private Long userId;
    
    @JsonProperty("userName")
    private String userName;
    
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
    
    @JsonProperty("trainingDays")
    private Set<DayOfWeek> trainingDays;
    
    @JsonProperty("goal")
    private String goal;
    
    @JsonProperty("sessionsPerWeek")
    private Integer sessionsPerWeek;
    
    @JsonProperty("exerciseCount")
    private Integer exerciseCount;
        
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