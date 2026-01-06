package com.fitapp.backend.domain.model;

import com.fitapp.backend.infrastructure.persistence.entity.enums.ExerciseType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Slf4j
public class ExerciseModel {
    private Long id;
    private String name;
    private String description;
    private Long sportId;
    private String sportName;
    private Long createdById;
    private String createdByEmail;
    private Set<Long> categoryIds = new HashSet<>();
    private Set<String> categoryNames = new HashSet<>();
    private Set<Long> supportedParameterIds = new HashSet<>();
    private Set<String> supportedParameterNames = new HashSet<>();
    private ExerciseType exerciseType;
    private Boolean isActive;
    private Boolean isPublic;
    private Integer usageCount;
    private Double rating;
    private Integer ratingCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastUsedAt;

    public void logModelData(String operation) {
        log.info("EXERCISE_MODEL_{} | id={} | name={} | type={} | sportId={} | isPublic={}", 
                operation.toUpperCase(), id, name, exerciseType, sportId, isPublic);
        log.debug("EXERCISE_MODEL_DETAILS | categories={} | parameters={}", 
                categoryIds.size(), supportedParameterIds.size());
    }
    
    public void validate() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Exercise name cannot be empty");
        }
        
        if (exerciseType == null) {
            throw new IllegalArgumentException("Exercise type is required");
        }
        
        if (sportId == null) {
            throw new IllegalArgumentException("Sport ID is required");
        }
        
        log.debug("EXERCISE_MODEL_VALIDATED | name={} | type={}", name, exerciseType);
    }
}