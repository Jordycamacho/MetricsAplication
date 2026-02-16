package com.fitapp.backend.application.dto.category.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta de categoría de ejercicio")
public class ExerciseCategoryResponse {
    
    @Schema(description = "ID de la categoría")
    @JsonProperty("id")
    private Long id;
    
    @Schema(description = "Nombre de la categoría")
    @JsonProperty("name")
    private String name;
    
    @Schema(description = "Descripción")
    @JsonProperty("description")
    private String description;
    
    @Schema(description = "Si es predefinida")
    @JsonProperty("isPredefined")
    private Boolean isPredefined;
    
    @Schema(description = "Si está activa")
    @JsonProperty("isActive")
    private Boolean isActive;
    
    @Schema(description = "Si es pública")
    @JsonProperty("isPublic")
    private Boolean isPublic;
    
    @Schema(description = "ID del dueño")
    @JsonProperty("ownerId")
    private Long ownerId;
    
    @Schema(description = "Nombre del dueño")
    @JsonProperty("ownerName")
    private String ownerName;
    
    @Schema(description = "ID del deporte")
    @JsonProperty("sportId")
    private Long sportId;
    
    @Schema(description = "Nombre del deporte")
    @JsonProperty("sportName")
    private String sportName;
    
    @Schema(description = "ID de la categoría padre")
    @JsonProperty("parentCategoryId")
    private Long parentCategoryId;
    
    @Schema(description = "Contador de uso")
    @JsonProperty("usageCount")
    private Integer usageCount;
    
    @Schema(description = "Fecha de creación")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @Schema(description = "Fecha de actualización")
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
}