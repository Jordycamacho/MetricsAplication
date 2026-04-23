package com.fitapp.backend.routinecomplete.package_.aplication.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitapp.backend.sport.aplication.dto.response.SportResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Item dentro de un package")
public class PackageItemResponse {

    @Schema(description = "Item ID")
    @JsonProperty("id")
    private Long id;

    @Schema(description = "Tipo: SPORT, PARAMETER, ROUTINE, EXERCISE, CATEGORY")
    @JsonProperty("itemType")
    private String itemType;

    @Schema(description = "Orden de display")
    @JsonProperty("displayOrder")
    private Integer displayOrder;

    @Schema(description = "Notas del creador")
    @JsonProperty("notes")
    private String notes;

    @Schema(description = "Contenido si es SPORT")
    @JsonProperty("sport")
    private SportResponse sport;

    @Schema(description = "Contenido si es PARAMETER")
    @JsonProperty("parameter")
    private com.fitapp.backend.parameter.application.dto.response.CustomParameterResponse parameter;

    @Schema(description = "Contenido si es ROUTINE")
    @JsonProperty("routine")
    private com.fitapp.backend.routinecomplete.routine.aplication.dto.response.RoutineResponse routine;

    @Schema(description = "Contenido si es EXERCISE")
    @JsonProperty("exercise")
    private com.fitapp.backend.Exercise.aplication.dto.response.ExerciseResponse exercise;
}