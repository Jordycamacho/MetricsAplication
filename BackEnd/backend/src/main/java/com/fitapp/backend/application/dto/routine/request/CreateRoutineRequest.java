package com.fitapp.backend.application.dto.routine.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoutineRequest {
    @NotBlank(message = "Routine name is required")
    @Size(max = 100, message = "Routine name cannot exceed 100 characters")
    private String name;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    @JsonAlias({"sportId", "sportid", "sport_id"})
    private Long sportId;
    
    @JsonProperty("sessionsPerWeek")
    @Min(value = 1, message = "La sesiones por semana no puede ser inferior a 1")
    @Max(value = 7, message = "La sesiones por semana no puede ser superior a 17")
    private Integer sessionsPerWeek;

    @NotEmpty(message = "dias de entrenamiento son requeridos")
    @JsonProperty("trainingDays") 
    private List<String> trainingDays;
    
    @NotBlank(message = "Goal is required")
    @Size(max = 200, message = "Objetivo no puede exeder 200 caracteres")
    private String goal;
}