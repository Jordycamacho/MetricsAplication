package com.fitapp.backend.infrastructure.persistence.repository;

import com.fitapp.backend.infrastructure.persistence.entity.RoutineSetTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoutineSetTemplateRepository extends JpaRepository<RoutineSetTemplateEntity, Long> {

        @Query("SELECT COUNT(s) FROM RoutineSetTemplateEntity s WHERE s.routineExercise.id = :routineExerciseId")
        long countByRoutineExerciseId(@Param("routineExerciseId") Long routineExerciseId);

        List<RoutineSetTemplateEntity> findByRoutineExerciseId(Long routineExerciseId);

        List<RoutineSetTemplateEntity> findByRoutineExerciseIdAndGroupId(Long routineExerciseId, String groupId);

        boolean existsByRoutineExerciseIdAndPosition(Long routineExerciseId, Integer position);

        @Modifying
        @Query("DELETE FROM RoutineSetTemplateEntity s WHERE s.routineExercise.id = :routineExerciseId")
        int deleteByRoutineExerciseId(@Param("routineExerciseId") Long routineExerciseId);

        @Query("SELECT s FROM RoutineSetTemplateEntity s WHERE s.routineExercise.id = :routineExerciseId ORDER BY s.position ASC")
        List<RoutineSetTemplateEntity> findByRoutineExerciseIdOrdered(
                        @Param("routineExerciseId") Long routineExerciseId);

        @Query("""
                        SELECT DISTINCT s FROM RoutineSetTemplateEntity s
                        LEFT JOIN FETCH s.parameters p
                        LEFT JOIN FETCH p.parameter
                        WHERE s.routineExercise.id = :routineExerciseId
                        ORDER BY s.position ASC
                        """)
        List<RoutineSetTemplateEntity> findByRoutineExerciseIdWithParameters(
                        @Param("routineExerciseId") Long routineExerciseId);

        @Query("""
                        SELECT DISTINCT s FROM RoutineSetTemplateEntity s
                        LEFT JOIN FETCH s.parameters p
                        LEFT JOIN FETCH p.parameter
                        WHERE s.routineExercise.id = :routineExerciseId
                          AND s.groupId = :groupId
                        ORDER BY s.position ASC
                        """)
        List<RoutineSetTemplateEntity> findByRoutineExerciseIdAndGroupIdWithParameters(
                        @Param("routineExerciseId") Long routineExerciseId,
                        @Param("groupId") String groupId);
}
