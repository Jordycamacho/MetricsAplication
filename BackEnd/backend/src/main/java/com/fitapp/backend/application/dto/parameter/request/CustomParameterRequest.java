package com.fitapp.backend.application.dto.parameter.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitapp.backend.infrastructure.persistence.entity.enums.ParameterType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Data
@Slf4j
@Schema(description = "Request para crear o actualizar un parámetro personalizado")
public class CustomParameterRequest {
    
    @NotBlank(message = "El nombre del parámetro es obligatorio")
    @Schema(description = "Nombre técnico del parámetro (camelCase)", 
            example = "maxWeight", required = true)
    @JsonProperty("name")
    private String name;
    
    @Schema(description = "Nombre para mostrar", example = "Peso Máximo")
    @JsonProperty("displayName")
    private String displayName;
    
    @Schema(description = "Descripción del parámetro", 
            example = "Peso máximo levantado en una repetición")
    @JsonProperty("description")
    private String description;
    
    @NotNull(message = "El tipo de parámetro es obligatorio")
    @Schema(description = "Tipo de dato del parámetro", required = true)
    @JsonProperty("parameterType")
    private ParameterType parameterType;
    
    @Schema(description = "Unidad de medida", example = "kg")
    @JsonProperty("unit")
    private String unit;
    
    @Schema(description = "Reglas de validación")
    @JsonProperty("validationRules")
    private Map<String, String> validationRules;
    
    @Schema(description = "Si es un parámetro global", example = "false")
    @JsonProperty("isGlobal")
    private Boolean isGlobal = false;
    
    @Schema(description = "ID del deporte asociado")
    @JsonProperty("sportId")
    private Long sportId;
    
    @Schema(description = "Categoría del parámetro", example = "strength")
    @JsonProperty("category")
    private String category;
    
    @Schema(description = "Icono", example = "fa-weight")
    @JsonProperty("icon")
    private String icon;
    
    public void logRequestData() {
        log.info("PARAMETER_REQUEST_RECEIVED | name={} | type={} | isGlobal={} | sportId={}", 
                name, parameterType, isGlobal, sportId);
        log.debug("PARAMETER_REQUEST_DETAILS | displayName={} | unit={} | category={}", 
                displayName, unit, category);
    }
}