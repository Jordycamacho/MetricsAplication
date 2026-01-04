package com.fitapp.backend.application.dto.category.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.domain.Sort;

@Data
@Schema(description = "Filtros para búsqueda de categorías")
public class ExerciseCategoryFilterRequest {
    
    @Schema(description = "Texto de búsqueda")
    private String search;
    
    @Schema(description = "Si es predefinida")
    private Boolean isPredefined;
    
    @Schema(description = "Si está activa")
    private Boolean isActive;
    
    @Schema(description = "Si es pública")
    private Boolean isPublic;
    
    @Schema(description = "ID del deporte")
    private Long sportId;
    
    @Schema(description = "ID del dueño")
    private Long ownerId;
    
    @Schema(description = "Solo mis categorías")
    private Boolean onlyMine = false;
    
    @Schema(description = "Incluir predefinidas")
    private Boolean includePredefined = true;
    
    @Schema(description = "Página solicitada", example = "0")
    private Integer page = 0;
    
    @Schema(description = "Tamaño de página", example = "20")
    private Integer size = 20;
    
    @Schema(description = "Campo para ordenar", example = "name")
    private String sortBy = "name";
    
    @Schema(description = "Dirección del orden", example = "ASC")
    private Sort.Direction direction = Sort.Direction.ASC;
}