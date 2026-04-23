package com.fitapp.backend.category.aplication.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.domain.Sort;

@Data
@Schema(description = "Filtros para búsqueda de categorías")
public class ExerciseCategoryFilterRequest {
    
    @Schema(description = "Texto de búsqueda")
    @JsonProperty("search")
    private String search;
    
    @Schema(description = "Si es predefinida")
    @JsonProperty("isPredefined")
    private Boolean isPredefined;
    
    @Schema(description = "Si está activa")
    @JsonProperty("isActive")
    private Boolean isActive;
    
    @Schema(description = "Si es pública")
    @JsonProperty("isPublic")
    private Boolean isPublic;
    
    @Schema(description = "ID del deporte")
    @JsonProperty("sportId")
    private Long sportId;
    
    @Schema(description = "ID del dueño")
    @JsonProperty("ownerId")
    private Long ownerId;
    
    @Schema(description = "Solo mis categorías")
    @JsonProperty("onlyMine")
    private Boolean onlyMine = false;
    
    @Schema(description = "Incluir predefinidas")
    @JsonProperty("includePredefined")
    private Boolean includePredefined = true;
    
    @Schema(description = "Página solicitada", example = "0")
    @JsonProperty("page")
    private Integer page = 0;
    
    @Schema(description = "Tamaño de página", example = "20")
    @JsonProperty("size")
    private Integer size = 20;
    
    @Schema(description = "Campo para ordenar", example = "name")
    @JsonProperty("sortBy")
    private String sortBy = "name";
    
    @Schema(description = "Dirección del orden", example = "ASC")
    @JsonProperty("direction")
    private Sort.Direction direction = Sort.Direction.ASC;
}