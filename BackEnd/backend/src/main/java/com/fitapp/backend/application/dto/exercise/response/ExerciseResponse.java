package com.fitapp.backend.application.dto.exercise.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitapp.backend.infrastructure.persistence.entity.enums.ExerciseType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Schema(description = "Respuesta de ejercicio")
public class ExerciseResponse {
    
    @Schema(description = "ID del ejercicio")
    @JsonProperty("id")
    private Long id;
    
    @Schema(description = "Nombre del ejercicio")
    @JsonProperty("name")
    private String name;
    
    @Schema(description = "Descripción")
    @JsonProperty("description")
    private String description;
    
    @Schema(description = "Tipo de ejercicio")
    @JsonProperty("exerciseType")
    private ExerciseType exerciseType;
    
    @Schema(description = "ID del deporte")
    @JsonProperty("sportId")
    private Long sportId;
    
    @Schema(description = "Nombre del deporte")
    @JsonProperty("sportName")
    private String sportName;
    
    @Schema(description = "ID del creador")
    @JsonProperty("createdById")
    private Long createdById;
    
    @Schema(description = "Email del creador")
    @JsonProperty("createdByEmail")
    private String createdByEmail;
    
    @Schema(description = "IDs de las categorías")
    @JsonProperty("categoryIds")
    private Set<Long> categoryIds = new HashSet<>();
    
    @Schema(description = "Nombres de las categorías")
    @JsonProperty("categoryNames")
    private Set<String> categoryNames = new HashSet<>();
    
    @Schema(description = "IDs de los parámetros soportados")
    @JsonProperty("supportedParameterIds")
    private Set<Long> supportedParameterIds = new HashSet<>();
    
    @Schema(description = "Nombres de los parámetros soportados")
    @JsonProperty("supportedParameterNames")
    private Set<String> supportedParameterNames = new HashSet<>();
    
    @Schema(description = "Si está activo")
    @JsonProperty("isActive")
    private Boolean isActive;
    
    @Schema(description = "Si es público")
    @JsonProperty("isPublic")
    private Boolean isPublic;
    
    @Schema(description = "Contador de uso")
    @JsonProperty("usageCount")
    private Integer usageCount;
    
    @Schema(description = "Rating promedio")
    @JsonProperty("rating")
    private Double rating;
    
    @Schema(description = "Número de ratings")
    @JsonProperty("ratingCount")
    private Integer ratingCount;
    
    @Schema(description = "Fecha de creación")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @Schema(description = "Fecha de actualización")
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
    
    @Schema(description = "Fecha de último uso")
    @JsonProperty("lastUsedAt")
    private LocalDateTime lastUsedAt;
}