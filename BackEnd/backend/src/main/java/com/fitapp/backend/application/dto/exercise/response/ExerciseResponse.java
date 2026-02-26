package com.fitapp.backend.application.dto.exercise.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitapp.backend.infrastructure.persistence.entity.enums.ExerciseType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Respuesta de ejercicio")
public class ExerciseResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("exerciseType")
    private ExerciseType exerciseType;

    @JsonProperty("sports")
    private Map<Long, String> sports = new HashMap<>();

    @JsonProperty("createdById")
    private Long createdById;

    @JsonProperty("categoryIds")
    private Set<Long> categoryIds = new HashSet<>();

    @JsonProperty("categoryNames")
    private Set<String> categoryNames = new HashSet<>();

    @JsonProperty("supportedParameterIds")
    private Set<Long> supportedParameterIds = new HashSet<>();

    @JsonProperty("supportedParameterNames")
    private Set<String> supportedParameterNames = new HashSet<>();

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("isPublic")
    private Boolean isPublic;

    @JsonProperty("usageCount")
    private Integer usageCount;

    @JsonProperty("rating")
    private Double rating;

    @JsonProperty("ratingCount")
    private Integer ratingCount;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    @JsonProperty("lastUsedAt")
    private LocalDateTime lastUsedAt;
}