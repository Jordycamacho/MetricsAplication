package com.fitapp.backend.application.ports.input;

import com.fitapp.backend.application.dto.exercise.request.ExerciseFilterRequest;
import com.fitapp.backend.application.dto.exercise.request.ExerciseRequest;
import com.fitapp.backend.application.dto.exercise.response.ExercisePageResponse;
import com.fitapp.backend.domain.model.ExerciseModel;
import org.springframework.data.domain.Pageable;

public interface ExerciseUseCase {
    ExercisePageResponse getAllExercisesPaginated(ExerciseFilterRequest filterRequest);
    ExercisePageResponse getMyExercisesPaginated(String userEmail, ExerciseFilterRequest filterRequest);
    ExercisePageResponse getAvailableExercisesPaginated(String userEmail, ExerciseFilterRequest filterRequest);
    ExercisePageResponse getExercisesBySport(String userEmail, Long sportId, ExerciseFilterRequest filterRequest);
    ExerciseModel getExerciseById(Long id);
    ExerciseModel getExerciseByIdWithRelations(Long id);
    ExerciseModel createExercise(ExerciseRequest request, String userEmail);
    ExerciseModel updateExercise(Long id, ExerciseRequest request, String userEmail);
    void deleteExercise(Long id, String userEmail);
    void toggleExerciseStatus(Long id, String userEmail);
    void incrementExerciseUsage(Long exerciseId);
    void rateExercise(Long exerciseId, Double rating, String userEmail);
    ExerciseModel duplicateExercise(Long exerciseId, String userEmail);
    ExerciseModel makeExercisePublic(Long exerciseId, String userEmail);
    ExercisePageResponse getRecentlyUsedExercises(String userEmail, Pageable pageable);
    ExercisePageResponse getMostPopularExercises(Pageable pageable);
    ExercisePageResponse getTopRatedExercises(Pageable pageable);
    Long getUserExerciseCount(String userEmail);
    void cleanupInactiveExercises(int daysInactive);
}