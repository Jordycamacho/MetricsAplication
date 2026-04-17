package com.fitapp.backend.parameter.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitapp.backend.infrastructure.persistence.entity.enums.MetricAggregation;
import com.fitapp.backend.parameter.infrastructure.persistence.entity.ParameterType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Respuesta de parámetro personalizado")
public class CustomParameterResponse {
    
    @Schema(description = "ID del parámetro")
    @JsonProperty("id")
    private Long id;
    
    @Schema(description = "Nombre técnico")
    @JsonProperty("name")
    private String name;
    
    @Schema(description = "Descripción")
    @JsonProperty("description")
    private String description;
    
    @Schema(description = "Tipo de parámetro")
    @JsonProperty("parameterType")
    private ParameterType parameterType;
    
    @Schema(description = "Unidad")
    @JsonProperty("unit")
    private String unit;
    
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

    @Schema(description = "Si es favorito")
    @JsonProperty("isFavorite")
    private Boolean isFavorite;

    @Schema(description = "Fecha de creación")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @Schema(description = "Fecha de actualización")
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
    
    @Schema(description = "Contador de uso")
    @JsonProperty("usageCount")
    private Integer usageCount;

    @Schema(description = "Método de agregación para métricas")
    @JsonProperty("metricAggregation")
    private MetricAggregation metricAggregation;
    
    @Schema(description = "Si el parámetro es trackeable para métricas")
    @JsonProperty("isTrackable")
    private Boolean isTrackable;
}