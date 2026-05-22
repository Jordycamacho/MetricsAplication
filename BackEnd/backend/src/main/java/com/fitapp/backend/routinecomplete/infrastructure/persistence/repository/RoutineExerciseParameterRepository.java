package com.fitapp.backend.routinecomplete.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitapp.backend.routinecomplete.infrastructure.persistence.entity.RoutineExerciseParameterEntity;

@Repository
public interface RoutineExerciseParameterRepository extends JpaRepository<RoutineExerciseParameterEntity, Long> {
}