package com.fitapp.backend.application.dto.routine.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoutineRequest {

    @Size(max = 100, message = "Routine name cannot exceed 100 characters")
    @JsonProperty("name")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @JsonProperty("description")
    private String description;

    private Long sportId;
    private boolean sportIdExplicit = false;

    @JsonSetter(value = "sportId", nulls = Nulls.SET)
    public void setSportIdJson(Long sportId) {
        this.sportId = sportId;
        this.sportIdExplicit = true;
    }

    @JsonProperty("trainingDays")
    private List<String> trainingDays;

    @Size(max = 200, message = "Goal cannot exceed 200 characters")
    @JsonProperty("goal")
    private String goal;

    @JsonProperty("sessionsPerWeek")
    private Integer sessionsPerWeek;

    @JsonProperty("isActive")
    private Boolean isActive = true;

    /** @return true si el cliente envió "sportId" en el JSON (aunque sea null) */
    public boolean hasSportIdExplicit() {
        return sportIdExplicit;
    }
}