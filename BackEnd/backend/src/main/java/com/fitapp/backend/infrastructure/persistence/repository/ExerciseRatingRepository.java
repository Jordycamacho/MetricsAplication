package com.fitapp.backend.infrastructure.persistence.repository;

import com.fitapp.backend.infrastructure.persistence.entity.ExerciseRatingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExerciseRatingRepository extends JpaRepository<ExerciseRatingEntity, Long> {

    boolean existsByExerciseIdAndUserId(Long exerciseId, Long userId);

    Optional<ExerciseRatingEntity> findByExerciseIdAndUserId(Long exerciseId, Long userId);

    @Query("SELECT COUNT(r) FROM ExerciseRatingEntity r WHERE r.exercise.id = :exerciseId")
    Long countByExerciseId(@Param("exerciseId") Long exerciseId);
}