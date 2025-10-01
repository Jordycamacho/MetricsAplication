package com.fitapp.backend.infrastructure.persistence.repository;

import com.fitapp.backend.infrastructure.persistence.entity.ExerciseEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExerciseRepository extends JpaRepository<ExerciseEntity, Long> {
    List<ExerciseEntity> findBySportId(Long sportId);
    List<ExerciseEntity> findByUserId(Long userId);
    List<ExerciseEntity> findByIsPredefinedTrue();
    List<ExerciseEntity> findByUserIdAndSportId(Long userId, Long sportId);
}