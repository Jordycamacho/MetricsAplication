package com.fitapp.backend.domain.model;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseModel {
    private Long id;
    private String name;
    private String description;
    private Long sportId;
    private Long userId;
    private Boolean isPredefined;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, String> parameterTemplates;
}