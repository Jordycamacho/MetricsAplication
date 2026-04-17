package com.fitapp.backend.parameter.application.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitapp.backend.infrastructure.persistence.entity.enums.MetricAggregation;
import com.fitapp.backend.parameter.infrastructure.persistence.entity.ParameterType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Schema(description = "Request para crear o actualizar un parámetro personalizado")
public class CustomParameterRequest {
    
    @NotBlank(message = "El nombre del parámetro es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Schema(description = "Nombre técnico del parámetro (camelCase)", 
            example = "maxWeight", required = true)
    @JsonProperty("name")
    private String name;
    
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @Schema(description = "Descripción del parámetro", 
            example = "Peso máximo levantado en una repetición")
    @JsonProperty("description")
    private String description;
    
    @NotNull(message = "El tipo de parámetro es obligatorio")
    @Schema(description = "Tipo de dato del parámetro", required = true)
    @JsonProperty("parameterType")
    private ParameterType parameterType;
    
    @Size(max = 20, message = "La unidad no puede exceder 20 caracteres")
    @Schema(description = "Unidad de medida", example = "kg")
    @JsonProperty("unit")
    private String unit;
    
    @Schema(description = "Si es un parámetro global (solo administradores)", example = "false")
    @JsonProperty("isGlobal")
    private Boolean isGlobal = false;

    @Schema(description = "Si es favorito", example = "false")
    @JsonProperty("isFavorite")
    private Boolean isFavorite = false;
    
    @Schema(description = "Cómo se agrega este parámetro para métricas (MAX, AVG, SUM, MIN, LAST)", 
            example = "MAX")
    @JsonProperty("metricAggregation")
    private MetricAggregation metricAggregation;
    
    @Schema(description = "Si el sistema debe calcular métricas automáticas para este parámetro", 
            example = "true")
    @JsonProperty("isTrackable")
    private Boolean isTrackable = true;

    public void logRequestData() {
        log.info("PARAMETER_REQUEST_RECEIVED | name={} | type={} | isGlobal={} | trackable={} | aggregation={}", 
                name, parameterType, isGlobal, isTrackable, metricAggregation);
        log.debug("PARAMETER_REQUEST_DETAILS | unit={} | isFavorite={}", 
                unit, isFavorite);
    }
}