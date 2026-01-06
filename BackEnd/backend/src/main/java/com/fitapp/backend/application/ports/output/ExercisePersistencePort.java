package com.fitapp.backend.application.ports.output;

import com.fitapp.backend.domain.model.ExerciseModel;
import com.fitapp.backend.application.dto.exercise.request.ExerciseFilterRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ExercisePersistencePort {
    Optional<ExerciseModel> findById(Long id);
    Optional<ExerciseModel> findByIdWithRelations(Long id);
    Page<ExerciseModel> findAll(Pageable pageable);
    Page<ExerciseModel> findByFilters(ExerciseFilterRequest filters, Pageable pageable);
    Optional<ExerciseModel> findByNameAndCreatedById(String name, Long createdById);
    Page<ExerciseModel> findByCreatedById(Long createdById, Pageable pageable);
    Page<ExerciseModel> findBySportId(Long sportId, Pageable pageable);
    Page<ExerciseModel> findAvailableForUser(Long userId, Pageable pageable);
    Page<ExerciseModel> findAvailableForUserAndSport(Long userId, Long sportId, Pageable pageable);
    Page<ExerciseModel> findRecentlyUsed(Pageable pageable);
    Page<ExerciseModel> findMostPopular(Pageable pageable);
    Page<ExerciseModel> findTopRated(Pageable pageable);
    List<ExerciseModel> findInactiveBefore(java.time.LocalDateTime cutoffDate);
    List<String> findAllDistinctDifficultyLevels();
    List<String> findAllDistinctEquipment();
    Long countByUser(Long userId);
    ExerciseModel save(ExerciseModel exerciseModel);
    void delete(Long id);
    void incrementUsageCount(Long exerciseId);
    void addRating(Long exerciseId, Double rating);
    boolean existsById(Long id);
}