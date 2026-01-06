package com.fitapp.backend.application.dto.exercise.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitapp.backend.infrastructure.persistence.entity.enums.ExerciseType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
@Schema(description = "Request para crear o actualizar un ejercicio")
public class ExerciseRequest {

    private static final Logger log = LoggerFactory.getLogger(ExerciseRequest.class);
    
    @NotBlank(message = "El nombre del ejercicio es obligatorio")
    @Schema(description = "Nombre del ejercicio", example = "Sentadilla", required = true)
    @JsonProperty("name")
    private String name;
    
    @Schema(description = "Descripción del ejercicio", example = "Ejercicio para piernas")
    @JsonProperty("description")
    private String description;
    
    @NotNull(message = "El tipo de ejercicio es obligatorio")
    @Schema(description = "Tipo de ejercicio", required = true)
    @JsonProperty("exerciseType")
    private ExerciseType exerciseType;
    
    @NotNull(message = "El deporte es obligatorio")
    @Schema(description = "ID del deporte", required = true)
    @JsonProperty("sportId")
    private Long sportId;
    
    @Schema(description = "IDs de las categorías")
    @JsonProperty("categoryIds")
    private Set<Long> categoryIds = new HashSet<>();
    
    @Schema(description = "IDs de los parámetros soportados")
    @JsonProperty("supportedParameterIds")
    private Set<Long> supportedParameterIds = new HashSet<>();
    
    @Schema(description = "Si el ejercicio es público", example = "false")
    @JsonProperty("isPublic")
    private Boolean isPublic = false;
    
    @Schema(description = "Equipo necesario", example = "Barra, discos")
    @JsonProperty("equipmentNeeded")
    private String equipmentNeeded;
    
    public void logRequestData() {
        log.info("EXERCISE_REQUEST_RECEIVED | name={} | type={} | sportId={} | isPublic={}", 
                name, exerciseType, sportId, isPublic);
        log.debug("EXERCISE_REQUEST_DETAILS | categories={} | parameters={}", 
                categoryIds.size(), supportedParameterIds.size());
    }
}