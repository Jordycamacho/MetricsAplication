package com.fitapp.backend.application.ports.output;


import java.util.List;
import java.util.Optional;

import com.fitapp.backend.domain.model.RoutineSetTemplateModel;

public interface RoutineSetTemplatePersistencePort {
    
    List<RoutineSetTemplateModel> findByRoutineExerciseIdAndGroupIdWithParameters(Long routineExerciseId, String groupId);
    List<RoutineSetTemplateModel> findByRoutineExerciseIdAndGroupId(Long routineExerciseId, String groupId);
    List<RoutineSetTemplateModel> findByRoutineExerciseIdWithParameters(Long routineExerciseId);
    boolean existsByRoutineExerciseIdAndPosition(Long routineExerciseId, Integer position);
    List<RoutineSetTemplateModel> findByRoutineExerciseId(Long routineExerciseId);
    List<RoutineSetTemplateModel> saveAll(List<RoutineSetTemplateModel> models);
    RoutineSetTemplateModel save(RoutineSetTemplateModel model);
    void deleteByRoutineExerciseId(Long routineExerciseId);
    long countByRoutineExerciseId(Long routineExerciseId);
    Optional<RoutineSetTemplateModel> findById(Long id);
    void deleteById(Long id);
}
