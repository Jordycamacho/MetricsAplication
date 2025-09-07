package com.fitapp.backend.application.dto.exercise;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddExercisesToRoutineRequest {
    @NotNull
    private Long routineId;
    @Valid
    private List<ExerciseRequest> exercises;
}