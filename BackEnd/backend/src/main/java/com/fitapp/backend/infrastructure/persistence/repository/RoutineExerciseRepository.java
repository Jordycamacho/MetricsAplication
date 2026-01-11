package com.fitapp.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fitapp.backend.infrastructure.persistence.entity.ExerciseEntity;
import com.fitapp.backend.infrastructure.persistence.entity.RoutineEntity;
import com.fitapp.backend.infrastructure.persistence.entity.RoutineExerciseEntity;
import com.fitapp.backend.infrastructure.persistence.entity.enums.DayOfWeek;

@Repository
public interface RoutineExerciseRepository extends JpaRepository<RoutineExerciseEntity, Long> {
    
    @Query("SELECT re FROM RoutineExerciseEntity re WHERE re.id = :id AND re.routine.id = :routineId")
    Optional<RoutineExerciseEntity> findByIdAndRoutineId(@Param("id") Long id, @Param("routineId") Long routineId);
    
    @Query("SELECT re FROM RoutineExerciseEntity re WHERE re.routine.id = :routineId ORDER BY re.position ASC")
    List<RoutineExerciseEntity> findByRoutineId(@Param("routineId") Long routineId);
    
    @Modifying
    @Query("DELETE FROM RoutineExerciseEntity re WHERE re.id = :id AND re.routine.id = :routineId")
    int deleteByIdAndRoutineId(@Param("id") Long id, @Param("routineId") Long routineId);
    
    @Modifying
    @Query("DELETE FROM RoutineExerciseEntity re WHERE re.routine.id = :routineId")
    int deleteByRoutineId(@Param("routineId") Long routineId);
    
    @Query("SELECT re FROM RoutineExerciseEntity re WHERE re.routine.id = :routineId AND re.sessionNumber = :sessionNumber ORDER BY re.sessionOrder ASC")
    List<RoutineExerciseEntity> findByRoutineIdAndSessionNumber(@Param("routineId") Long routineId, 
                                                               @Param("sessionNumber") Integer sessionNumber);
    
    @Query("SELECT re FROM RoutineExerciseEntity re WHERE re.routine.id = :routineId AND re.dayOfWeek = :dayOfWeek ORDER BY re.sessionOrder ASC")
    List<RoutineExerciseEntity> findByRoutineIdAndDayOfWeek(@Param("routineId") Long routineId, 
                                                           @Param("dayOfWeek") DayOfWeek dayOfWeek);
    
    @Query("SELECT r FROM RoutineEntity r WHERE r.id = :routineId")
    Optional<RoutineEntity> findRoutineById(@Param("routineId") Long routineId);
    
    @Query("SELECT e FROM ExerciseEntity e WHERE e.id = :exerciseId")
    Optional<ExerciseEntity> findExerciseById(@Param("exerciseId") Long exerciseId);
}