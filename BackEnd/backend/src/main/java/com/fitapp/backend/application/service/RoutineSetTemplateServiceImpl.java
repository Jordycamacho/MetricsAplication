package com.fitapp.backend.application.service;

import com.fitapp.backend.application.dto.RoutineSetTemplate.request.CreateSetTemplateRequest;
import com.fitapp.backend.application.dto.RoutineSetTemplate.request.UpdateSetTemplateRequest;
import com.fitapp.backend.application.logging.SetTemplateServiceLogger;
import com.fitapp.backend.application.ports.input.RoutineSetTemplateUseCase;
import com.fitapp.backend.application.ports.output.CustomParameterPersistencePort;
import com.fitapp.backend.application.ports.output.ExercisePersistencePort;
import com.fitapp.backend.application.ports.output.RoutineExercisePersistencePort;
import com.fitapp.backend.application.ports.output.RoutineSetParameterPersistencePort;
import com.fitapp.backend.application.ports.output.RoutineSetTemplatePersistencePort;
import com.fitapp.backend.application.ports.output.UserPersistencePort;
import com.fitapp.backend.domain.exception.SetTemplateNotFoundException;
import com.fitapp.backend.domain.exception.SetTemplatePositionConflictException;
import com.fitapp.backend.domain.exception.UnsupportedParameterException;
import com.fitapp.backend.domain.exception.UserNotFoundException;
import com.fitapp.backend.domain.model.*;
import com.fitapp.backend.infrastructure.metrics.SetTemplateMetrics;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SetType;

import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoutineSetTemplateServiceImpl implements RoutineSetTemplateUseCase {

        private final RoutineSetTemplatePersistencePort setTemplatePersistencePort;
        private final RoutineSetParameterPersistencePort setParameterPersistencePort;
        private final RoutineExercisePersistencePort routineExercisePersistencePort;
        private final CustomParameterPersistencePort customParameterPersistencePort;
        private final UserPersistencePort userPersistencePort;
        private final ExercisePersistencePort exercisePersistencePort;
        private final SetTemplateServiceLogger serviceLogger;
        private final SetTemplateMetrics metrics;

        // ─────────────────────────────────────────────────────────────────────────
        // CREATE
        // ─────────────────────────────────────────────────────────────────────────

        @Override
        @Transactional
        public RoutineSetTemplateModel createSetTemplate(CreateSetTemplateRequest request, String userEmail) {
                log.info("SET_TEMPLATE_CREATION_START | user={} | routineExercise={} | position={}",
                                userEmail, request.getRoutineExerciseId(), request.getPosition());
                serviceLogger.logSetTemplateCreationStart(userEmail, request.getRoutineExerciseId());

                Timer.Sample timer = metrics.startCreationTimer();

                try {
                        request.validate();
                        request.logRequestData();

                        UserModel user = userPersistencePort.findByEmail(userEmail)
                                        .orElseThrow(() -> {
                                                log.error("USER_NOT_FOUND | email={}", userEmail);
                                                return new UserNotFoundException("User not found: " + userEmail);
                                        });

                        RoutineExerciseModel routineExercise = routineExercisePersistencePort
                                        .findById(request.getRoutineExerciseId())
                                        .orElseThrow(() -> {
                                                log.error("ROUTINE_EXERCISE_NOT_FOUND | id={}",
                                                                request.getRoutineExerciseId());
                                                return new RuntimeException("Routine exercise not found: "
                                                                + request.getRoutineExerciseId());
                                        });

                        ExerciseModel exercise = exercisePersistencePort.findById(routineExercise.getExerciseId())
                                        .orElseThrow(() -> new RuntimeException("Exercise not found"));

                        Set<Long> supportedParamIds = exercise.getSupportedParameterIds();

                        if (request.getParameters() != null) {
                                for (var paramRequest : request.getParameters()) {
                                        if (!supportedParamIds.contains(paramRequest.getParameterId())) {
                                                throw new UnsupportedParameterException(paramRequest.getParameterId(),
                                                                exercise.getId());
                                        }
                                }
                        }

                        validateUserOwnsRoutineExercise(user.getId(), routineExercise);
                        validateUniquePosition(request.getRoutineExerciseId(), request.getPosition());

                        RoutineSetTemplateModel model = new RoutineSetTemplateModel();
                        model.setRoutineExerciseId(request.getRoutineExerciseId());
                        model.setPosition(request.getPosition());
                        model.setSubSetNumber(request.getSubSetNumber() != null ? request.getSubSetNumber() : 1);
                        model.setGroupId(request.getGroupId());
                        model.setSetType(request.getSetType() != null ? request.getSetType() : SetType.NORMAL.name());
                        model.setRestAfterSet(request.getRestAfterSet());

                        model.validate();
                        model.logModelData("CREATING");

                        RoutineSetTemplateModel savedSetTemplate = setTemplatePersistencePort.save(model);
                        log.info("SET_TEMPLATE_CREATED | id={} | position={} | type={}",
                                        savedSetTemplate.getId(), savedSetTemplate.getPosition(),
                                        savedSetTemplate.getSetType());

                        if (request.getParameters() != null && !request.getParameters().isEmpty()) {
                                log.debug("Creating {} parameters for set template id={}",
                                                request.getParameters().size(), savedSetTemplate.getId());

                                List<RoutineSetParameterModel> parameterModels = request.getParameters().stream()
                                                .map(paramRequest -> createSetParameterModel(paramRequest,
                                                                savedSetTemplate.getId()))
                                                .collect(Collectors.toList());

                                List<RoutineSetParameterModel> savedParameters = setParameterPersistencePort
                                                .saveAll(parameterModels);
                                savedSetTemplate.setParameters(savedParameters);

                                savedParameters.forEach(param -> customParameterPersistencePort
                                                .incrementUsageCount(param.getParameterId()));

                                metrics.recordParametersCreated(savedParameters.size());
                                log.info("SET_PARAMETERS_CREATED | count={} | setTemplateId={}",
                                                savedParameters.size(), savedSetTemplate.getId());
                        }

                        metrics.recordCreated();
                        serviceLogger.logSetTemplateCreationSuccess(savedSetTemplate.getId(), userEmail);
                        log.info("SET_TEMPLATE_CREATION_COMPLETE | id={} | user={}", savedSetTemplate.getId(),
                                        userEmail);

                        return savedSetTemplate;

                } catch (Exception e) {
                        serviceLogger.logSetTemplateCreationError(userEmail, request.getRoutineExerciseId(), e);
                        log.error("SET_TEMPLATE_CREATION_ERROR | user={} | error={}", userEmail, e.getMessage(), e);
                        throw e;
                } finally {
                        metrics.stopCreationTimer(timer);
                }
        }

        // ─────────────────────────────────────────────────────────────────────────
        // UPDATE
        // ─────────────────────────────────────────────────────────────────────────

        @Override
        @Transactional
        public RoutineSetTemplateModel updateSetTemplate(Long id, UpdateSetTemplateRequest request, String userEmail) {
                log.info("SET_TEMPLATE_UPDATE_START | id={} | user={}", id, userEmail);
                serviceLogger.logSetTemplateUpdateStart(id, userEmail);

                try {
                        request.validate();

                        UserModel user = userPersistencePort.findByEmail(userEmail)
                                        .orElseThrow(() -> new UserNotFoundException("User not found: " + userEmail));

                        RoutineSetTemplateModel existingSet = setTemplatePersistencePort.findById(id)
                                        .orElseThrow(() -> {
                                                log.error("SET_TEMPLATE_NOT_FOUND | id={}", id);
                                                return new SetTemplateNotFoundException(id);
                                        });

                        validateUserOwnsSetTemplate(user.getId(), existingSet);

                        if (request.getPosition() != null) {
                                validateUniquePosition(existingSet.getRoutineExerciseId(), request.getPosition(), id);
                                existingSet.setPosition(request.getPosition());
                        }
                        if (request.getSubSetNumber() != null) {
                                existingSet.setSubSetNumber(request.getSubSetNumber());
                        }
                        if (request.getGroupId() != null) {
                                existingSet.setGroupId(request.getGroupId());
                        }
                        if (request.getSetType() != null) {
                                existingSet.setSetType(request.getSetType());
                        }
                        if (request.getRestAfterSet() != null) {
                                existingSet.setRestAfterSet(request.getRestAfterSet());
                        }

                        existingSet.validate();
                        existingSet.logModelData("UPDATING");

                        if (request.getParameters() != null) {
                                updateSetParameters(existingSet, request.getParameters());
                        }

                        RoutineSetTemplateModel updatedSet = setTemplatePersistencePort.save(existingSet);

                        metrics.recordUpdated();
                        serviceLogger.logSetTemplateUpdateSuccess(id, userEmail);
                        log.info("SET_TEMPLATE_UPDATE_COMPLETE | id={} | user={}", id, userEmail);

                        return updatedSet;

                } catch (Exception e) {
                        serviceLogger.logSetTemplateUpdateError(id, userEmail, e);
                        log.error("SET_TEMPLATE_UPDATE_ERROR | id={} | error={}", id, e.getMessage(), e);
                        throw e;
                }
        }

        // ─────────────────────────────────────────────────────────────────────────
        // READ
        // ─────────────────────────────────────────────────────────────────────────

        @Override
        @Transactional(readOnly = true)
        public RoutineSetTemplateModel getSetTemplateById(Long id, String userEmail) {
                log.debug("GET_SET_TEMPLATE_BY_ID | id={} | user={}", id, userEmail);
                serviceLogger.logSetTemplateRetrievalStart(id, userEmail);

                try {
                        UserModel user = userPersistencePort.findByEmail(userEmail)
                                        .orElseThrow(() -> new UserNotFoundException("User not found: " + userEmail));

                        RoutineSetTemplateModel setTemplate = setTemplatePersistencePort.findById(id)
                                        .orElseThrow(() -> new SetTemplateNotFoundException(id));

                        validateUserOwnsSetTemplate(user.getId(), setTemplate);

                        List<RoutineSetParameterModel> parameters = setParameterPersistencePort.findBySetTemplateId(id);
                        setTemplate.setParameters(parameters);

                        serviceLogger.logSetTemplateRetrievalSuccess(id, userEmail);
                        return setTemplate;

                } catch (Exception e) {
                        serviceLogger.logSetTemplateRetrievalError(id, userEmail, e);
                        log.error("SET_TEMPLATE_RETRIEVAL_ERROR | id={} | error={}", id, e.getMessage(), e);
                        throw e;
                }
        }

        @Override
        @Transactional(readOnly = true)
        public List<RoutineSetTemplateModel> getSetTemplatesByRoutineExercise(Long routineExerciseId,
                        String userEmail) {
                log.debug("GET_SET_TEMPLATES_BY_ROUTINE_EXERCISE | routineExercise={} | user={}",
                                routineExerciseId, userEmail);
                serviceLogger.logSetTemplatesRetrievalStart(routineExerciseId, userEmail);

                Timer.Sample queryTimer = metrics.startQueryTimer();

                try {
                        UserModel user = userPersistencePort.findByEmail(userEmail)
                                        .orElseThrow(() -> new UserNotFoundException("User not found: " + userEmail));

                        RoutineExerciseModel routineExercise = routineExercisePersistencePort
                                        .findById(routineExerciseId)
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Routine exercise not found: " + routineExerciseId));

                        validateUserOwnsRoutineExercise(user.getId(), routineExercise);

                        // Usa fetch join — evita N+1 queries cargando sets + parámetros en una sola
                        // consulta
                        List<RoutineSetTemplateModel> setTemplates = setTemplatePersistencePort
                                        .findByRoutineExerciseIdWithParameters(routineExerciseId);

                        log.info("RETRIEVED_SET_TEMPLATES | count={} | routineExercise={}",
                                        setTemplates.size(), routineExerciseId);
                        serviceLogger.logSetTemplatesRetrievalSuccess(routineExerciseId, setTemplates.size(),
                                        userEmail);

                        return setTemplates;

                } catch (Exception e) {
                        serviceLogger.logSetTemplatesRetrievalError(routineExerciseId, userEmail, e);
                        log.error("SET_TEMPLATES_RETRIEVAL_ERROR | routineExercise={} | error={}",
                                        routineExerciseId, e.getMessage(), e);
                        throw e;
                } finally {
                        metrics.stopQueryTimer(queryTimer);
                }
        }

        @Override
        @Transactional(readOnly = true)
        public List<RoutineSetTemplateModel> getSetTemplatesByGroup(Long routineExerciseId, String groupId,
                        String userEmail) {
                log.debug("GET_SET_TEMPLATES_BY_GROUP | routineExercise={} | group={} | user={}",
                                routineExerciseId, groupId, userEmail);
                serviceLogger.logSetTemplatesByGroupRetrievalStart(routineExerciseId, groupId, userEmail);

                try {
                        UserModel user = userPersistencePort.findByEmail(userEmail)
                                        .orElseThrow(() -> new UserNotFoundException("User not found: " + userEmail));

                        RoutineExerciseModel routineExercise = routineExercisePersistencePort
                                        .findById(routineExerciseId)
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Routine exercise not found: " + routineExerciseId));

                        validateUserOwnsRoutineExercise(user.getId(), routineExercise);

                        // Fetch join — sets + parámetros del grupo en una sola consulta
                        List<RoutineSetTemplateModel> setTemplates = setTemplatePersistencePort
                                        .findByRoutineExerciseIdAndGroupIdWithParameters(routineExerciseId, groupId);

                        serviceLogger.logSetTemplatesByGroupRetrievalSuccess(routineExerciseId, groupId,
                                        setTemplates.size(), userEmail);
                        log.info("RETRIEVED_SET_TEMPLATES_BY_GROUP | count={} | routineExercise={} | group={}",
                                        setTemplates.size(), routineExerciseId, groupId);

                        return setTemplates;

                } catch (Exception e) {
                        serviceLogger.logSetTemplatesByGroupRetrievalError(routineExerciseId, groupId, userEmail, e);
                        log.error("SET_TEMPLATES_BY_GROUP_RETRIEVAL_ERROR | routineExercise={} | group={} | error={}",
                                        routineExerciseId, groupId, e.getMessage(), e);
                        throw e;
                }
        }

        // ─────────────────────────────────────────────────────────────────────────
        // DELETE
        // ─────────────────────────────────────────────────────────────────────────

        @Override
        @Transactional
        public void deleteSetTemplate(Long id, String userEmail) {
                log.info("DELETE_SET_TEMPLATE_START | id={} | user={}", id, userEmail);
                serviceLogger.logSetTemplateDeletionStart(id, userEmail);

                try {
                        UserModel user = userPersistencePort.findByEmail(userEmail)
                                        .orElseThrow(() -> new UserNotFoundException("User not found: " + userEmail));

                        RoutineSetTemplateModel setTemplate = setTemplatePersistencePort.findById(id)
                                        .orElseThrow(() -> new SetTemplateNotFoundException(id));

                        validateUserOwnsSetTemplate(user.getId(), setTemplate);

                        setTemplatePersistencePort.deleteById(id);

                        metrics.recordDeleted();
                        serviceLogger.logSetTemplateDeletionSuccess(id, userEmail);
                        log.info("SET_TEMPLATE_DELETED | id={} | user={}", id, userEmail);

                } catch (Exception e) {
                        serviceLogger.logSetTemplateDeletionError(id, userEmail, e);
                        log.error("SET_TEMPLATE_DELETION_ERROR | id={} | error={}", id, e.getMessage(), e);
                        throw e;
                }
        }

        @Override
        @Transactional
        public void deleteSetTemplatesByRoutineExercise(Long routineExerciseId, String userEmail) {
                log.info("DELETE_SET_TEMPLATES_BY_ROUTINE_EXERCISE | routineExercise={} | user={}",
                                routineExerciseId, userEmail);
                serviceLogger.logSetTemplatesBulkDeletionStart(routineExerciseId, userEmail);

                try {
                        UserModel user = userPersistencePort.findByEmail(userEmail)
                                        .orElseThrow(() -> new UserNotFoundException("User not found: " + userEmail));

                        RoutineExerciseModel routineExercise = routineExercisePersistencePort
                                        .findById(routineExerciseId)
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Routine exercise not found: " + routineExerciseId));

                        validateUserOwnsRoutineExercise(user.getId(), routineExercise);

                        List<RoutineSetTemplateModel> setTemplates = setTemplatePersistencePort
                                        .findByRoutineExerciseId(routineExerciseId);

                        setTemplates.forEach(setTemplate -> setParameterPersistencePort
                                        .deleteBySetTemplateId(setTemplate.getId()));

                        setTemplatePersistencePort.deleteByRoutineExerciseId(routineExerciseId);

                        serviceLogger.logSetTemplatesBulkDeletionSuccess(routineExerciseId, setTemplates.size(),
                                        userEmail);
                        log.info("SET_TEMPLATES_BULK_DELETED | count={} | routineExercise={}",
                                        setTemplates.size(), routineExerciseId);

                } catch (Exception e) {
                        serviceLogger.logSetTemplatesBulkDeletionError(routineExerciseId, userEmail, e);
                        log.error("SET_TEMPLATES_BULK_DELETION_ERROR | routineExercise={} | error={}",
                                        routineExerciseId, e.getMessage(), e);
                        throw e;
                }
        }

        // ─────────────────────────────────────────────────────────────────────────
        // REORDER
        // ─────────────────────────────────────────────────────────────────────────

        @Override
        @Transactional
        public RoutineSetTemplateModel reorderSetTemplates(Long routineExerciseId, List<Long> setTemplateIds,
                        String userEmail) {
                log.info("REORDER_SET_TEMPLATES | routineExercise={} | count={} | user={}",
                                routineExerciseId, setTemplateIds.size(), userEmail);
                serviceLogger.logSetTemplatesReorderStart(routineExerciseId, setTemplateIds.size(), userEmail);

                try {
                        UserModel user = userPersistencePort.findByEmail(userEmail)
                                        .orElseThrow(() -> new UserNotFoundException("User not found: " + userEmail));

                        RoutineExerciseModel routineExercise = routineExercisePersistencePort
                                        .findById(routineExerciseId)
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Routine exercise not found: " + routineExerciseId));

                        validateUserOwnsRoutineExercise(user.getId(), routineExercise);

                        List<RoutineSetTemplateModel> allSetTemplates = setTemplatePersistencePort
                                        .findByRoutineExerciseId(routineExerciseId);

                        Set<Long> existingIds = allSetTemplates.stream()
                                        .map(RoutineSetTemplateModel::getId)
                                        .collect(Collectors.toSet());

                        if (!existingIds.containsAll(setTemplateIds)) {
                                throw new IllegalArgumentException(
                                                "Some set template IDs do not belong to this routine exercise");
                        }

                        Map<Long, RoutineSetTemplateModel> setTemplateMap = allSetTemplates.stream()
                                        .collect(Collectors.toMap(RoutineSetTemplateModel::getId, Function.identity()));

                        List<RoutineSetTemplateModel> reorderedSets = new ArrayList<>();
                        for (int i = 0; i < setTemplateIds.size(); i++) {
                                RoutineSetTemplateModel setTemplate = setTemplateMap.get(setTemplateIds.get(i));
                                setTemplate.setPosition(i + 1);
                                reorderedSets.add(setTemplate);
                        }

                        for (RoutineSetTemplateModel setTemplate : allSetTemplates) {
                                if (!setTemplateIds.contains(setTemplate.getId())) {
                                        reorderedSets.add(setTemplate);
                                }
                        }

                        List<RoutineSetTemplateModel> updatedSets = setTemplatePersistencePort.saveAll(reorderedSets);

                        serviceLogger.logSetTemplatesReorderSuccess(routineExerciseId, updatedSets.size(), userEmail);
                        log.info("SET_TEMPLATES_REORDERED | count={} | routineExercise={}",
                                        updatedSets.size(), routineExerciseId);

                        return updatedSets.stream()
                                        .findFirst()
                                        .orElseThrow(() -> new RuntimeException(
                                                        "No set templates found after reorder"));

                } catch (Exception e) {
                        serviceLogger.logSetTemplatesReorderError(routineExerciseId, userEmail, e);
                        log.error("SET_TEMPLATES_REORDER_ERROR | routineExercise={} | error={}",
                                        routineExerciseId, e.getMessage(), e);
                        throw e;
                }
        }

        // ─────────────────────────────────────────────────────────────────────────
        // PRIVATE HELPERS
        // ─────────────────────────────────────────────────────────────────────────

        private RoutineSetParameterModel createSetParameterModel(
                        CreateSetTemplateRequest.SetParameterRequest request, Long setTemplateId) {

                RoutineSetParameterModel model = new RoutineSetParameterModel();
                model.setSetTemplateId(setTemplateId);
                model.setParameterId(request.getParameterId());
                model.setNumericValue(request.getNumericValue());
                model.setDurationValue(request.getDurationValue());
                model.setIntegerValue(request.getIntegerValue());
                model.setRepetitions(request.getRepetitions());

                model.validate();
                model.logModelData("CREATING");

                return model;
        }

        private void updateSetParameters(RoutineSetTemplateModel setTemplate,
                        List<UpdateSetTemplateRequest.SetParameterRequest> parameterRequests) {

                List<RoutineSetParameterModel> existingParameters = setParameterPersistencePort
                                .findBySetTemplateId(setTemplate.getId());

                Map<Long, RoutineSetParameterModel> existingParamsMap = existingParameters.stream()
                                .filter(p -> p.getId() != null)
                                .collect(Collectors.toMap(RoutineSetParameterModel::getId, Function.identity()));

                List<RoutineSetParameterModel> updatedParameters = new ArrayList<>();

                for (UpdateSetTemplateRequest.SetParameterRequest paramRequest : parameterRequests) {
                        if (paramRequest.getId() != null && existingParamsMap.containsKey(paramRequest.getId())) {
                                RoutineSetParameterModel existingParam = existingParamsMap.get(paramRequest.getId());
                                updateExistingParameter(existingParam, paramRequest);
                                updatedParameters.add(existingParam);
                                existingParamsMap.remove(paramRequest.getId());
                        } else {
                                RoutineSetParameterModel newParam = createSetParameterModelFromUpdateRequest(
                                                paramRequest, setTemplate.getId());
                                updatedParameters.add(newParam);
                        }
                }

                List<Long> paramsToDelete = new ArrayList<>(existingParamsMap.keySet());
                if (!paramsToDelete.isEmpty()) {
                        setParameterPersistencePort.deleteAllById(paramsToDelete);
                        log.debug("DELETED_SET_PARAMETERS | count={} | setTemplateId={}",
                                        paramsToDelete.size(), setTemplate.getId());
                }

                List<RoutineSetParameterModel> savedParameters = setParameterPersistencePort.saveAll(updatedParameters);
                setTemplate.setParameters(savedParameters);

                log.info("SET_PARAMETERS_UPDATED | count={} | setTemplateId={}",
                                savedParameters.size(), setTemplate.getId());
        }

        private RoutineSetParameterModel createSetParameterModelFromUpdateRequest(
                        UpdateSetTemplateRequest.SetParameterRequest request, Long setTemplateId) {

                RoutineSetParameterModel model = new RoutineSetParameterModel();
                model.setSetTemplateId(setTemplateId);
                model.setParameterId(request.getParameterId());
                model.setNumericValue(request.getNumericValue());
                model.setDurationValue(request.getDurationValue());
                model.setIntegerValue(request.getIntegerValue());
                model.setRepetitions(request.getRepetitions());
                model.validate();
                model.logModelData("CREATING_FROM_UPDATE");

                return model;
        }

        private void updateExistingParameter(RoutineSetParameterModel existingParam,
                        UpdateSetTemplateRequest.SetParameterRequest request) {

                existingParam.setParameterId(request.getParameterId());
                existingParam.setNumericValue(request.getNumericValue());
                existingParam.setDurationValue(request.getDurationValue());
                existingParam.setIntegerValue(request.getIntegerValue());
                existingParam.setRepetitions(request.getRepetitions());

                existingParam.validate();
                existingParam.logModelData("UPDATING");
        }

        private void validateUserOwnsRoutineExercise(Long userId, RoutineExerciseModel routineExercise) {
                log.debug("Validating user ownership for routine exercise: user={}, routineExercise={}",
                                userId, routineExercise.getId());
        }

        private void validateUserOwnsSetTemplate(Long userId, RoutineSetTemplateModel setTemplate) {
                log.debug("Validating user ownership for set template: user={}, setTemplate={}",
                                userId, setTemplate.getId());
        }

        private void validateUniquePosition(Long routineExerciseId, Integer position) {
                validateUniquePosition(routineExerciseId, position, null);
        }

        private void validateUniquePosition(Long routineExerciseId, Integer position, Long excludeId) {
                List<RoutineSetTemplateModel> existingSets = setTemplatePersistencePort
                                .findByRoutineExerciseId(routineExerciseId);

                boolean positionTaken = existingSets.stream()
                                .filter(set -> excludeId == null || !set.getId().equals(excludeId))
                                .anyMatch(set -> set.getPosition().equals(position));

                if (positionTaken) {
                        throw new SetTemplatePositionConflictException(position, routineExerciseId);
                }
        }
}