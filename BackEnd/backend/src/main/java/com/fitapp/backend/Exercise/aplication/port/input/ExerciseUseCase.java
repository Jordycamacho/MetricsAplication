package com.fitapp.backend.Exercise.aplication.port.input;

import com.fitapp.backend.Exercise.aplication.dto.request.ExerciseFilterRequest;
import com.fitapp.backend.Exercise.aplication.dto.request.ExerciseRequest;
import com.fitapp.backend.Exercise.aplication.dto.response.ExercisePageResponse;
import com.fitapp.backend.Exercise.domain.model.ExerciseModel;

import org.springframework.data.domain.Pageable;

public interface ExerciseUseCase {

    // --- Queries ---
    ExercisePageResponse getAllExercisesPaginated(ExerciseFilterRequest filterRequest);
    ExercisePageResponse getMyExercisesPaginated(String userEmail, ExerciseFilterRequest filterRequest);
    ExercisePageResponse getAvailableExercisesPaginated(String userEmail, ExerciseFilterRequest filterRequest);
    ExercisePageResponse getExercisesBySport(String userEmail, Long sportId, ExerciseFilterRequest filterRequest);
    ExerciseModel getExerciseById(Long id);
    ExercisePageResponse getRecentlyUsedExercises(String userEmail, Pageable pageable);
    ExercisePageResponse getMostPopularExercises(Pageable pageable);
    ExercisePageResponse getTopRatedExercises(Pageable pageable);
    Long getUserExerciseCount(String userEmail);

    // --- Commands ---
    ExerciseModel createExercise(ExerciseRequest request, String userEmail);
    ExerciseModel updateExercise(Long id, ExerciseRequest request, String userEmail);
    void deleteExercise(Long id, String userEmail);
    void toggleExerciseStatus(Long id, String userEmail);
    void incrementExerciseUsage(Long exerciseId);
    void rateExercise(Long exerciseId, Double rating, String userEmail);
    ExerciseModel duplicateExercise(Long exerciseId, String userEmail);
    ExerciseModel makeExercisePublic(Long exerciseId, String userEmail);
    void cleanupInactiveExercises(int daysInactive);
}