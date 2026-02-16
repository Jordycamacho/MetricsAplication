package com.fitapp.backend.application.dto.category.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request para crear o actualizar una categoría")
public class ExerciseCategoryRequest {
    
    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Schema(description = "Nombre de la categoría", example = "Fuerza", required = true)
    @JsonProperty("name")
    private String name;
    
    @Schema(description = "Descripción de la categoría", example = "Ejercicios de fuerza y potencia")
    @JsonProperty("description")
    private String description;
    
    @Schema(description = "Si la categoría es pública", example = "false")
    @JsonProperty("isPublic")
    private Boolean isPublic = false;
    
    @Schema(description = "ID del deporte asociado")
    @JsonProperty("sportId")
    private Long sportId;
    
    @Schema(description = "ID de la categoría padre (para jerarquías)")
    @JsonProperty("parentCategoryId")
    private Long parentCategoryId;
    
    @Schema(description = "Orden de visualización", example = "1")
    @JsonProperty("displayOrder")
    private Integer displayOrder;
}