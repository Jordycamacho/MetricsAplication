package com.fitapp.backend.infrastructure.persistence.adapter.out;

import com.fitapp.backend.domain.model.RoutineSetTemplateModel;
import com.fitapp.backend.application.ports.output.RoutineSetTemplatePersistencePort;
import com.fitapp.backend.infrastructure.persistence.entity.RoutineSetTemplateEntity;
import com.fitapp.backend.infrastructure.persistence.repository.RoutineSetTemplateRepository;
import com.fitapp.backend.infrastructure.persistence.converter.SetConverter;
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
public class RoutineSetTemplatePersistenceAdapter implements RoutineSetTemplatePersistencePort {

    private final RoutineSetTemplateRepository setTemplateRepository;
    private final SetConverter setConverter;

    @Override
    @Transactional
    @CacheEvict(value = { "setTemplates", "setTemplatesByExercise" }, allEntries = true)
    public RoutineSetTemplateModel save(RoutineSetTemplateModel model) {
        log.debug("Saving set template model: {}", model.getId());
        try {
            RoutineSetTemplateEntity entity = setConverter.toSetTemplateEntity(model);
            RoutineSetTemplateEntity savedEntity = setTemplateRepository.save(entity);
            log.info("Set template saved successfully: id={}, position={}",
                    savedEntity.getId(), savedEntity.getPosition());
            return setConverter.toSetTemplateModel(savedEntity);
        } catch (DataAccessException e) {
            log.error("Data access error while saving set template: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save set template", e);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = { "setTemplates", "setTemplatesByExercise" }, allEntries = true)
    public List<RoutineSetTemplateModel> saveAll(List<RoutineSetTemplateModel> models) {
        log.debug("Saving {} set template models", models.size());
        try {
            List<RoutineSetTemplateEntity> entities = models.stream()
                    .map(setConverter::toSetTemplateEntity)
                    .collect(Collectors.toList());

            List<RoutineSetTemplateEntity> savedEntities = setTemplateRepository.saveAll(entities);

            log.info("Batch saved {} set templates successfully", savedEntities.size());
            return savedEntities.stream()
                    .map(setConverter::toSetTemplateModel)
                    .collect(Collectors.toList());
        } catch (DataAccessException e) {
            log.error("Data access error while batch saving set templates: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save set templates", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "setTemplates", key = "#id")
    public Optional<RoutineSetTemplateModel> findById(Long id) {
        log.debug("Finding set template by id: {}", id);
        return setTemplateRepository.findById(id)
                .map(setConverter::toSetTemplateModel);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "setTemplatesByExercise", key = "#routineExerciseId")
    public List<RoutineSetTemplateModel> findByRoutineExerciseId(Long routineExerciseId) {
        log.debug("Finding set templates by routine exercise id: {}", routineExerciseId);
        return setTemplateRepository.findByRoutineExerciseIdOrdered(routineExerciseId).stream()
                .map(setConverter::toSetTemplateModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "setTemplatesByExercise", key = "'withParams_' + #routineExerciseId")
    public List<RoutineSetTemplateModel> findByRoutineExerciseIdWithParameters(Long routineExerciseId) {
        log.debug("Finding set templates with parameters for routineExercise: {}", routineExerciseId);
        return setTemplateRepository.findByRoutineExerciseIdWithParameters(routineExerciseId).stream()
                .map(setConverter::toSetTemplateModelWithParameters)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoutineSetTemplateModel> findByRoutineExerciseIdAndGroupIdWithParameters(
            Long routineExerciseId, String groupId) {
        log.debug("Finding set templates with parameters for routineExercise: {} group: {}",
                routineExerciseId, groupId);
        return setTemplateRepository
                .findByRoutineExerciseIdAndGroupIdWithParameters(routineExerciseId, groupId).stream()
                .map(setConverter::toSetTemplateModelWithParameters)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoutineSetTemplateModel> findByRoutineExerciseIdAndGroupId(Long routineExerciseId, String groupId) {
        log.debug("Finding set templates by routine exercise id {} and group {}", routineExerciseId, groupId);
        return setTemplateRepository.findByRoutineExerciseIdAndGroupId(routineExerciseId, groupId).stream()
                .map(setConverter::toSetTemplateModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = { "setTemplates", "setTemplatesByExercise" }, allEntries = true)
    public void deleteById(Long id) {
        log.debug("Deleting set template by id: {}", id);
        try {
            setTemplateRepository.deleteById(id);
            log.info("Set template deleted successfully: id={}", id);
        } catch (DataAccessException e) {
            log.error("Data access error while deleting set template: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete set template", e);
        }
    }

    @Override
    @Transactional
    public void deleteByRoutineExerciseId(Long routineExerciseId) {
        log.debug("Deleting set templates by routine exercise id: {}", routineExerciseId);
        try {
            int deletedCount = setTemplateRepository.deleteByRoutineExerciseId(routineExerciseId);
            log.info("Deleted {} set templates for routine exercise id: {}", deletedCount, routineExerciseId);
        } catch (DataAccessException e) {
            log.error("Data access error while deleting set templates: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete set templates", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByRoutineExerciseIdAndPosition(Long routineExerciseId, Integer position) {
        log.debug("Checking if set template exists for routine exercise {} at position {}",
                routineExerciseId, position);
        return setTemplateRepository.existsByRoutineExerciseIdAndPosition(routineExerciseId, position);
    }
}