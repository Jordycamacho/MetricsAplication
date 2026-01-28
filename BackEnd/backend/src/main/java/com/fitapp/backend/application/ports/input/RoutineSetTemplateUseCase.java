package com.fitapp.backend.application.ports.input;

import com.fitapp.backend.domain.model.RoutineSetTemplateModel;
import com.fitapp.backend.application.dto.RoutineSetTemplate.request.CreateSetTemplateRequest;
import com.fitapp.backend.application.dto.RoutineSetTemplate.request.UpdateSetTemplateRequest;

import java.util.List;

public interface RoutineSetTemplateUseCase {
    
    RoutineSetTemplateModel createSetTemplate(CreateSetTemplateRequest request, String userEmail);
    
    RoutineSetTemplateModel updateSetTemplate(Long id, UpdateSetTemplateRequest request, String userEmail);
    
    RoutineSetTemplateModel getSetTemplateById(Long id, String userEmail);
    
    List<RoutineSetTemplateModel> getSetTemplatesByRoutineExercise(Long routineExerciseId, String userEmail);
    
    void deleteSetTemplate(Long id, String userEmail);
    
    void deleteSetTemplatesByRoutineExercise(Long routineExerciseId, String userEmail);
    
    RoutineSetTemplateModel reorderSetTemplates(Long routineExerciseId, List<Long> setTemplateIds, String userEmail);
    
    List<RoutineSetTemplateModel> getSetTemplatesByGroup(Long routineExerciseId, String groupId, String userEmail);
}