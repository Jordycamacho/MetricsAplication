package com.fitapp.backend.Exercise.domain.model;

import com.fitapp.backend.infrastructure.persistence.entity.enums.ExerciseType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
public class ExerciseModel {
    private Long id;
    private String name;
    private String description;

    private Map<Long, String> sports = new HashMap<>();

    private Long createdById;
    private String createdByEmail;

    private Set<Long> categoryIds = new HashSet<>();
    private Set<String> categoryNames = new HashSet<>();

    private Set<Long> supportedParameterIds = new HashSet<>();
    private Set<String> supportedParameterNames = new HashSet<>();

    private ExerciseType exerciseType;
    private Boolean isActive = true;
    private Boolean isPublic = false;
    private Integer usageCount = 0;
    private Double rating = 0.0;
    private Integer ratingCount = 0;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastUsedAt;
}