package com.fitapp.backend.application.ports.input;

import java.util.List;

import com.fitapp.backend.application.dto.exercise.CreateExerciseRequest;
import com.fitapp.backend.application.dto.exercise.ExerciseResponse;
import com.fitapp.backend.application.dto.exercise.UpdateExerciseRequest;

public interface ExerciseUseCase {
    ExerciseResponse getExerciseById(Long id, String userEmail);
    List<ExerciseResponse> getExercisesBySport(Long sportId, String userEmail);
    List<ExerciseResponse> getUserExercises(String userEmail);
    List<ExerciseResponse> getPredefinedExercises();
    ExerciseResponse createExercise(CreateExerciseRequest request, String userEmail);
    ExerciseResponse updateExercise(Long id, UpdateExerciseRequest request, String userEmail);
    void deleteExercise(Long id, String userEmail);
}