package com.fitapp.backend.category.domain.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Data
@Slf4j
public class ExerciseCategoryModel {
    private Long id;
    private String name;
    private String description;
    private Boolean isPredefined;
    private Boolean isActive;
    private Boolean isPublic;
    private Long ownerId;
    private Long sportId;
    private Long parentCategoryId;
    private Integer usageCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void validate() {
        if (isPredefined && ownerId != null) {
            log.error("CATEGORY_VALIDATION | Predefined category cannot have owner");
            throw new IllegalArgumentException("Predefined category cannot have owner");
        }
        
        if (!isPredefined && ownerId == null) {
            log.error("CATEGORY_VALIDATION | Personal category must have owner");
            throw new IllegalArgumentException("Personal category must have owner");
        }
        
        log.debug("CATEGORY_MODEL_VALIDATED | name={} | type={}", 
                 name, isPredefined ? "PREDEFINED" : "PERSONAL");
    }
    
    public void incrementUsage() {
        this.usageCount++;
        log.debug("CATEGORY_USAGE_INCREMENTED_MODEL | id={} | count={}", id, usageCount);
    }
}