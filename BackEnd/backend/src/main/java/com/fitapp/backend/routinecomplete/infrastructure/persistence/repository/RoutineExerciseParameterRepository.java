package com.fitapp.backend.routinecomplete.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fitapp.backend.routinecomplete.infrastructure.persistence.entity.RoutineExerciseParameterEntity;

@Repository
public interface RoutineExerciseParameterRepository extends JpaRepository<RoutineExerciseParameterEntity, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM RoutineExerciseParameterEntity p WHERE p.routineExercise.id = :routineExerciseId")
    int deleteByRoutineExerciseId(@Param("routineExerciseId") Long routineExerciseId);
}