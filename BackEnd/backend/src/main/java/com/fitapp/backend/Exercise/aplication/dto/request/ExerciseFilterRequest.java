package com.fitapp.backend.Exercise.aplication.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitapp.backend.infrastructure.persistence.entity.enums.ExerciseType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "Filtros para búsqueda de ejercicios")
public class ExerciseFilterRequest {
    
    @Schema(description = "Texto de búsqueda en nombre o descripción")
    @JsonProperty("search")
    private String search;
    
    @Schema(description = "Tipo de ejercicio")
    @JsonProperty("exerciseType")
    private ExerciseType exerciseType;
    
    @Schema(description = "ID del deporte")
    @JsonProperty("sportId")
    private Long sportId;
    
    @Schema(description = "ID de la categoría")
    @JsonProperty("categoryId")
    private Long categoryId;
    
    @Schema(description = "ID del parámetro soportado")
    @JsonProperty("parameterId")
    private Long parameterId;
    
    @Schema(description = "Si está activo")
    @JsonProperty("isActive")
    private Boolean isActive;
    
    @Schema(description = "Si es público")
    @JsonProperty("isPublic")
    private Boolean isPublic;
    
    @Schema(description = "ID del creador")
    @JsonProperty("createdBy")
    private Long createdBy;
    
    @Schema(description = "Incluir ejercicios públicos")
    @JsonProperty("includePublic")
    private Boolean includePublic = true;
    
    @Schema(description = "Mínimo rating")
    @JsonProperty("minRating")
    private Double minRating;
    
    @Schema(description = "Página solicitada (0-index)", example = "0")
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
    
    @Schema(description = "Ordenar por popularidad (uso)")
    @JsonProperty("sortByPopularity")
    private Boolean sortByPopularity = false;
    
    @Schema(description = "Ordenar por rating")
    @JsonProperty("sortByRating")
    private Boolean sortByRating = false;
    
    @Schema(description = "Múltiples campos para ordenar")
    @JsonProperty("sortFields")
    private List<SortField> sortFields = new ArrayList<>();
    
    @Data
    public static class SortField {
        private String field;
        private Sort.Direction direction = Sort.Direction.ASC;
    }
}