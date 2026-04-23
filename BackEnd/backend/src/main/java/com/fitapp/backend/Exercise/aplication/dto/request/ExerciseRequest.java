package com.fitapp.backend.Exercise.aplication.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitapp.backend.infrastructure.persistence.entity.enums.ExerciseType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.validator.constraints.NotEmpty;

@Data
@Schema(description = "Request para crear o actualizar un ejercicio")
public class ExerciseRequest {

    @NotBlank(message = "El nombre del ejercicio es obligatorio")
    @Size(min = 2, max = 200, message = "El nombre debe tener entre 2 y 200 caracteres")
    @JsonProperty("name")
    private String name;

    @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
    @JsonProperty("description")
    private String description;

    @NotNull(message = "El tipo de ejercicio es obligatorio")
    @JsonProperty("exerciseType")
    private ExerciseType exerciseType;

    @NotEmpty(message = "Se requiere al menos un deporte")
    @Size(max = 10, message = "Un ejercicio puede pertenecer a máximo 10 deportes")
    @JsonProperty("sportIds")
    private Set<Long> sportIds = new HashSet<>();

    @JsonProperty("categoryIds")
    private Set<Long> categoryIds = new HashSet<>();

    @JsonProperty("supportedParameterIds")
    private Set<Long> supportedParameterIds = new HashSet<>();

    @JsonProperty("isPublic")
    private Boolean isPublic = false;
}