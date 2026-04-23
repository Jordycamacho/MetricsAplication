package com.fitapp.backend.sport.aplication.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SportSourceType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "Filtros para búsqueda de deportes")
public class SportFilterRequest {
    
    @Schema(description = "Texto de búsqueda en nombre", example = "fut")
    @JsonProperty("search")
    private String search;
    
    @Schema(description = "Si es predefinido o no")
    @JsonProperty("isPredefined")
    private Boolean isPredefined;
    
    @Schema(description = "Tipo de origen", example = "OFFICIAL")
    @JsonProperty("sourceType")
    private SportSourceType sourceType;
    
    @Schema(description = "Creado por usuario específico")
    @JsonProperty("createdBy")
    private Long createdBy;
    
    @Schema(description = "Página solicitada (0-index)", example = "0")
    @JsonProperty("page")
    private Integer page = 0;
    
    @Schema(description = "Tamaño de página", example = "10")
    @JsonProperty("size")
    private Integer size = 10;
    
    @Schema(description = "Campo para ordenar", example = "name")
    @JsonProperty("sortBy")
    private String sortBy = "name";
    
    @Schema(description = "Dirección del orden", example = "ASC")
    @JsonProperty("direction")
    private Sort.Direction direction = Sort.Direction.ASC;
    
    @Schema(description = "Múltiples campos para ordenar")
    @JsonProperty("sortFields")
    private List<SortField> sortFields = new ArrayList<>();
    
    @Data
    public static class SortField {
        @JsonProperty("field")
        private String field;
        
        @JsonProperty("direction")
        private Sort.Direction direction = Sort.Direction.ASC;
    }
}