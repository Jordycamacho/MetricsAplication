package com.fitapp.backend.infrastructure.persistence.repository;

import com.fitapp.backend.infrastructure.persistence.entity.RoutineExerciseParameterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoutineExerciseParameterRepository extends JpaRepository<RoutineExerciseParameterEntity, Long> {
}