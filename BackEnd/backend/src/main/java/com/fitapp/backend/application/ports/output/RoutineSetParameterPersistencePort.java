package com.fitapp.backend.application.ports.output;

import java.util.List;
import java.util.Optional;

import com.fitapp.backend.domain.model.RoutineSetParameterModel;

public interface RoutineSetParameterPersistencePort {

    RoutineSetParameterModel save(RoutineSetParameterModel model);
    List<RoutineSetParameterModel> saveAll(List<RoutineSetParameterModel> models);
    Optional<RoutineSetParameterModel> findById(Long id);
    List<RoutineSetParameterModel> findBySetTemplateId(Long setTemplateId);
    void deleteById(Long id);
    void deleteBySetTemplateId(Long setTemplateId);
    void deleteAllById(List<Long> ids);
}