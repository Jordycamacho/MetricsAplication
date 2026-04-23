package com.fitapp.backend.routinecomplete.routinesetemplate.infrastructure.persistence.adapter;

import com.fitapp.backend.routinecomplete.routinesetemplate.aplication.port.output.RoutineSetParameterPersistencePort;
import com.fitapp.backend.routinecomplete.routinesetemplate.domain.model.RoutineSetParameterModel;
import com.fitapp.backend.routinecomplete.routinesetemplate.infrastructure.persistence.converter.SetConverter;
import com.fitapp.backend.routinecomplete.routinesetemplate.infrastructure.persistence.entity.RoutineSetParameterEntity;
import com.fitapp.backend.routinecomplete.routinesetemplate.infrastructure.persistence.repository.RoutineSetParameterRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoutineSetParameterPersistenceAdapter implements RoutineSetParameterPersistencePort {
    
    private final RoutineSetParameterRepository setParameterRepository;
    private final SetConverter setConverter;
    
    @Override
    @Transactional
    public RoutineSetParameterModel save(RoutineSetParameterModel model) {
        log.debug("Saving set parameter model: setTemplate={}, parameter={}", 
                model.getSetTemplateId(), model.getParameterId());
        try {
            RoutineSetParameterEntity entity = setConverter.toSetParameterEntity(model);
            RoutineSetParameterEntity savedEntity = setParameterRepository.save(entity);
            log.info("Set parameter saved successfully: id={}", savedEntity.getId());
            return setConverter.toSetParameterModel(savedEntity);
        } catch (DataAccessException e) {
            log.error("Data access error while saving set parameter: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save set parameter", e);
        }
    }
    
    @Override
    @Transactional
    public List<RoutineSetParameterModel> saveAll(List<RoutineSetParameterModel> models) {
        log.debug("Saving {} set parameter models", models.size());
        try {
            List<RoutineSetParameterEntity> entities = models.stream()
                    .map(setConverter::toSetParameterEntity)
                    .collect(Collectors.toList());
            
            List<RoutineSetParameterEntity> savedEntities = setParameterRepository.saveAll(entities);
            
            log.info("Batch saved {} set parameters successfully", savedEntities.size());
            return savedEntities.stream()
                    .map(setConverter::toSetParameterModel)
                    .collect(Collectors.toList());
        } catch (DataAccessException e) {
            log.error("Data access error while batch saving set parameters: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save set parameters", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "setParameters", key = "#id")
    public Optional<RoutineSetParameterModel> findById(Long id) {
        log.debug("Finding set parameter by id: {}", id);
        return setParameterRepository.findById(id)
                .map(setConverter::toSetParameterModel);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<RoutineSetParameterModel> findBySetTemplateId(Long setTemplateId) {
        log.debug("Finding set parameters by set template id: {}", setTemplateId);
        return setParameterRepository.findBySetTemplateId(setTemplateId).stream()
                .map(setConverter::toSetParameterModel)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "setParameters", allEntries = true)
    public void deleteById(Long id) {
        log.debug("Deleting set parameter by id: {}", id);
        try {
            setParameterRepository.deleteById(id);
            log.info("Set parameter deleted successfully: id={}", id);
        } catch (DataAccessException e) {
            log.error("Data access error while deleting set parameter: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete set parameter", e);
        }
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "setParameters", allEntries = true)
    public void deleteBySetTemplateId(Long setTemplateId) {
        log.debug("Deleting set parameters by set template id: {}", setTemplateId);
        try {
            int deletedCount = setParameterRepository.deleteBySetTemplateId(setTemplateId);
            log.info("Deleted {} set parameters for set template id: {}", deletedCount, setTemplateId);
        } catch (DataAccessException e) {
            log.error("Data access error while deleting set parameters: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete set parameters", e);
        }
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "setParameters", allEntries = true)
    public void deleteAllById(List<Long> ids) {
        log.debug("Deleting {} set parameters by ids", ids.size());
        try {
            setParameterRepository.deleteAllById(ids);
            log.info("Deleted {} set parameters successfully", ids.size());
        } catch (DataAccessException e) {
            log.error("Data access error while deleting set parameters: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete set parameters", e);
        }
    }
}