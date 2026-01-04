package com.fitapp.backend.application.dto.category.response;

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
    private Long id;
    
    @Schema(description = "Nombre de la categoría")
    private String name;
    
    @Schema(description = "Descripción")
    private String description;
    
    @Schema(description = "Si es predefinida")
    private Boolean isPredefined;
    
    @Schema(description = "Si está activa")
    private Boolean isActive;
    
    @Schema(description = "Si es pública")
    private Boolean isPublic;
    
    @Schema(description = "ID del dueño")
    private Long ownerId;
    
    @Schema(description = "Nombre del dueño")
    private String ownerName;
    
    @Schema(description = "ID del deporte")
    private Long sportId;
    
    @Schema(description = "Nombre del deporte")
    private String sportName;
    
    @Schema(description = "ID de la categoría padre")
    private Long parentCategoryId;
    
    @Schema(description = "Contador de uso")
    private Integer usageCount;
    
    @Schema(description = "Fecha de creación")
    private LocalDateTime createdAt;
    
    @Schema(description = "Fecha de actualización")
    private LocalDateTime updatedAt;
}