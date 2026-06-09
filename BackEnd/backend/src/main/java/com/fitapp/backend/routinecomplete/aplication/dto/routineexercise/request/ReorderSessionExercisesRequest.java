package com.fitapp.backend.routinecomplete.aplication.dto.routineexercise.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ReorderSessionExercisesRequest {

    @JsonProperty("dayOfWeek")
    private String dayOfWeek;

    @JsonProperty("sessionNumber")
    private Integer sessionNumber;

    @NotNull
    @NotEmpty
    @JsonProperty("exerciseIds")
    private List<Long> exerciseIds;
}
