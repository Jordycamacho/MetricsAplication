package com.fitapp.backend.sport.aplication.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SportSourceType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Map;

@Data
@Schema(description = "Respuesta de deporte")
public class SportResponse {
    @Schema(description = "ID del deporte")
    @JsonProperty("id")
    private Long id;
    
    @Schema(description = "Nombre del deporte")
    @JsonProperty("name")
    private String name;
    
    @Schema(description = "Indica si es predefinido")
    @JsonProperty("isPredefined")
    private Boolean isPredefined;
    
    @Schema(description = "Tipo de origen del deporte")
    @JsonProperty("sourceType")
    private SportSourceType sourceType;
    
    @Schema(description = "Template de parámetros")
    @JsonProperty("parameterTemplate")
    private Map<String, String> parameterTemplate;
}