package com.fitapp.backend.parameter.application.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitapp.backend.parameter.infrastructure.persistence.entity.ParameterType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "Filtros para búsqueda de parámetros personalizados")
public class CustomParameterFilterRequest {
    
    @Schema(description = "Texto de búsqueda en nombre o descripción")
    @JsonProperty("search")
    private String search;
    
    @Schema(description = "Tipo de parámetro")
    @JsonProperty("parameterType")
    private ParameterType parameterType;

    @Schema(description = "Favoritos")
    @JsonProperty("isFavorite")
    private Boolean isFavorite;

    @Schema(description = "Si es global")
    @JsonProperty("isGlobal")
    private Boolean isGlobal;
    
    @Schema(description = "Si está activo")
    @JsonProperty("isActive")
    private Boolean isActive;
    
    @Schema(description = "Si es trackeable para métricas")
    @JsonProperty("isTrackable")
    private Boolean isTrackable;
    
    @Schema(description = "ID del dueño")
    @JsonProperty("ownerId")
    private Long ownerId;
    
    @Schema(description = "Solo mis parámetros")
    @JsonProperty("onlyMine")
    private Boolean onlyMine = false;
    
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
    
    @Schema(description = "Múltiples campos para ordenar")
    @JsonProperty("sortFields")
    private List<SortField> sortFields = new ArrayList<>();
    
    @Data
    public static class SortField {
        private String field;
        private Sort.Direction direction = Sort.Direction.ASC;
    }
}