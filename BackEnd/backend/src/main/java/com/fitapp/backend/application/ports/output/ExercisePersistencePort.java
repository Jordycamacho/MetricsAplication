package com.fitapp.backend.application.ports.output;

import com.fitapp.backend.application.dto.exercise.request.ExerciseFilterRequest;
import com.fitapp.backend.domain.model.ExerciseModel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

public interface ExercisePersistencePort {

    // --- Queries ---
    Optional<ExerciseModel> findById(Long id);
    Optional<Long> findIdByName(String name);
    String findNameById(Long exerciseId);
    Page<ExerciseModel> findAll(Pageable pageable);
    Page<ExerciseModel> findByFilters(ExerciseFilterRequest filters, Pageable pageable);
    Optional<ExerciseModel> findByNameAndCreatedById(String name, Long createdById);
    Page<ExerciseModel> findByCreatedById(Long createdById, Pageable pageable);
    Page<ExerciseModel> findAvailableForUser(Long userId, Pageable pageable);
    Page<ExerciseModel> findAvailableForUserAndSport(Long userId, Long sportId, Pageable pageable);
    Page<ExerciseModel> findRecentlyUsedByUser(Long userId, Pageable pageable);
    Page<ExerciseModel> findMostPopular(Pageable pageable);
    Page<ExerciseModel> findTopRated(Pageable pageable);
    List<ExerciseModel> findInactiveBefore(LocalDateTime cutoffDate);
    Long countByUser(Long userId);
    boolean existsById(Long id);
    boolean existsByNameAndCreatedByIdExcluding(String name, Long createdById, Long excludeId);
    boolean hasUserRated(Long exerciseId, Long userId);

    // --- Commands ---
    ExerciseModel save(ExerciseModel exerciseModel);
    void delete(Long id);
    void incrementUsageCount(Long exerciseId);
    void saveRating(Long exerciseId, Long userId, Double rating);
}