package com.fitapp.backend.application.dto.parameter.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitapp.backend.infrastructure.persistence.entity.enums.ParameterType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Schema(description = "Respuesta de parámetro personalizado")
public class CustomParameterResponse {
    
    @Schema(description = "ID del parámetro")
    @JsonProperty("id")
    private Long id;
    
    @Schema(description = "Nombre técnico")
    @JsonProperty("name")
    private String name;
    
    @Schema(description = "Nombre para mostrar")
    @JsonProperty("displayName")
    private String displayName;
    
    @Schema(description = "Descripción")
    @JsonProperty("description")
    private String description;
    
    @Schema(description = "Tipo de parámetro")
    @JsonProperty("parameterType")
    private ParameterType parameterType;
    
    @Schema(description = "Unidad")
    @JsonProperty("unit")
    private String unit;
    
    @Schema(description = "Reglas de validación")
    @JsonProperty("validationRules")
    private Map<String, String> validationRules;
    
    @Schema(description = "Si es global")
    @JsonProperty("isGlobal")
    private Boolean isGlobal;
    
    @Schema(description = "Si está activo")
    @JsonProperty("isActive")
    private Boolean isActive;
    
    @Schema(description = "ID del dueño")
    @JsonProperty("ownerId")
    private Long ownerId;
    
    @Schema(description = "Nombre del dueño")
    @JsonProperty("ownerName")
    private String ownerName;
    
    @Schema(description = "ID del deporte")
    @JsonProperty("sportId")
    private Long sportId;
    
    @Schema(description = "Nombre del deporte")
    @JsonProperty("sportName")
    private String sportName;
    
    @Schema(description = "Categoría")
    @JsonProperty("category")
    private String category;
    
    @Schema(description = "Fecha de creación")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @Schema(description = "Fecha de actualización")
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
    
    @Schema(description = "Contador de uso")
    @JsonProperty("usageCount")
    private Integer usageCount;
}