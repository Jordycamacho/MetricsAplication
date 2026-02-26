package com.fitapp.backend.infrastructure.persistence.converter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fitapp.backend.application.dto.routine.request.AddExerciseToRoutineRequest;
import com.fitapp.backend.domain.model.RoutineExerciseModel;
import com.fitapp.backend.domain.model.RoutineExerciseParameterModel;
import com.fitapp.backend.domain.model.RoutineModel;
import com.fitapp.backend.domain.model.RoutineSetParameterModel;
import com.fitapp.backend.domain.model.RoutineSetTemplateModel;
import com.fitapp.backend.infrastructure.persistence.entity.CustomParameterEntity;
import com.fitapp.backend.infrastructure.persistence.entity.ExerciseEntity;
import com.fitapp.backend.infrastructure.persistence.entity.RoutineEntity;
import com.fitapp.backend.infrastructure.persistence.entity.RoutineExerciseEntity;
import com.fitapp.backend.infrastructure.persistence.entity.RoutineExerciseParameterEntity;
import com.fitapp.backend.infrastructure.persistence.entity.RoutineSetParameterEntity;
import com.fitapp.backend.infrastructure.persistence.entity.RoutineSetTemplateEntity;
import com.fitapp.backend.infrastructure.persistence.entity.SportEntity;
import com.fitapp.backend.infrastructure.persistence.entity.UserEntity;
import com.fitapp.backend.infrastructure.persistence.entity.enums.DayOfWeek;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SetType;
import com.fitapp.backend.infrastructure.persistence.repository.CustomParameterRepository;
import com.fitapp.backend.infrastructure.persistence.repository.ExerciseRepository;
import com.fitapp.backend.infrastructure.persistence.repository.RoutineExerciseParameterRepository;
import com.fitapp.backend.infrastructure.persistence.repository.RoutineExerciseRepository;
import com.fitapp.backend.infrastructure.persistence.repository.RoutineSetTemplateRepository;
import com.fitapp.backend.infrastructure.persistence.repository.SportRepository;
import com.fitapp.backend.infrastructure.persistence.repository.SpringDataUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoutineConverter {

    private final SpringDataUserRepository springDataUserRepository;
    private final SportRepository sportRepository;
    private final ExerciseRepository exerciseRepository;
    private final CustomParameterRepository customParameterRepository;
    private final RoutineExerciseRepository routineExerciseRepository;
    private final RoutineExerciseParameterRepository routineExerciseParameterRepository;
    private final RoutineSetTemplateRepository routineSetTemplateRepository;

    @Transactional(readOnly = true)
    public RoutineModel toDomain(RoutineEntity entity) {
        log.debug("ROUTINE_CONVERTER_TO_DOMAIN | routineId={}", entity.getId());

        RoutineModel routine = new RoutineModel();
        routine.setId(entity.getId());
        routine.setName(entity.getName());
        routine.setDescription(entity.getDescription());
        routine.setIsActive(entity.getIsActive() != null ? entity.getIsActive() : true);
        routine.setCreatedAt(entity.getCreatedAt());
        routine.setUpdatedAt(entity.getUpdatedAt());
        routine.setLastUsedAt(entity.getLastUsedAt());
        routine.setUserId(entity.getUser().getId());
        routine.setSportId(entity.getSport() != null ? entity.getSport().getId() : null);
        routine.setTrainingDays(entity.getTrainingDays() != null ? entity.getTrainingDays() : new HashSet<>());
        routine.setGoal(entity.getGoal() != null ? entity.getGoal() : "");
        routine.setSessionsPerWeek(entity.getSessionsPerWeek() != null ? entity.getSessionsPerWeek() : 3);

        if (entity.getExercises() != null) {
            log.debug("ROUTINE_CONVERTER_EXERCISES | routineId={} | count={}", entity.getId(),
                    entity.getExercises().size());
            List<RoutineExerciseModel> exerciseModels = entity.getExercises().stream()
                    .map(this::convertRoutineExercise)
                    .sorted(Comparator.comparing(RoutineExerciseModel::getPosition))
                    .collect(Collectors.toList());
            routine.setExercises(exerciseModels);
        } else {
            routine.setExercises(new ArrayList<>());
        }

        log.info("ROUTINE_CONVERTER_TO_DOMAIN_OK | routineId={} | exerciseCount={}",
                entity.getId(), routine.getExercises().size());
        return routine;
    }

    public RoutineExerciseModel convertRoutineExercise(RoutineExerciseEntity entity) {
        log.debug("ROUTINE_EXERCISE_CONVERT | exerciseId={}", entity.getId());

        RoutineExerciseModel model = new RoutineExerciseModel();
        model.setId(entity.getId());
        model.setRoutineId(entity.getRoutine().getId());
        model.setExerciseId(entity.getExercise().getId());
        model.setPosition(entity.getPosition());
        model.setSessionNumber(entity.getSessionNumber());
        model.setDayOfWeek(entity.getDayOfWeek());
        model.setSessionOrder(entity.getSessionOrder());
        model.setRestAfterExercise(entity.getRestAfterExercise());

        if (entity.getTargetParameters() != null && !entity.getTargetParameters().isEmpty()) {
            List<RoutineExerciseParameterModel> paramModels = entity.getTargetParameters().stream()
                    .map(this::convertRoutineExerciseParameter)
                    .collect(Collectors.toList());
            model.setTargetParameters(paramModels);
        } else {
            model.setTargetParameters(new ArrayList<>());
        }

        if (entity.getSets() != null && !entity.getSets().isEmpty()) {
            log.debug("ROUTINE_EXERCISE_SETS | exerciseId={} | setCount={}", entity.getId(), entity.getSets().size());
            List<RoutineSetTemplateModel> setModels = entity.getSets().stream()
                    .sorted(Comparator.comparing(RoutineSetTemplateEntity::getPosition))
                    .map(this::convertRoutineSetTemplate)
                    .collect(Collectors.toList());
            model.setSets(setModels);
        } else {
            model.setSets(new ArrayList<>());
        }

        return model;
    }

    public RoutineExerciseParameterModel convertRoutineExerciseParameter(RoutineExerciseParameterEntity entity) {
        RoutineExerciseParameterModel model = new RoutineExerciseParameterModel();
        model.setId(entity.getId());
        model.setParameterId(entity.getParameter().getId());
        model.setNumericValue(entity.getNumericValue());
        model.setIntegerValue(entity.getIntegerValue());
        model.setDurationValue(entity.getDurationValue());
        model.setStringValue(entity.getStringValue());
        model.setMinValue(entity.getMinValue());
        model.setMaxValue(entity.getMaxValue());
        model.setDefaultValue(entity.getDefaultValue());
        return model;
    }

    public RoutineSetTemplateModel convertRoutineSetTemplate(RoutineSetTemplateEntity entity) {
        log.debug("ROUTINE_SET_TEMPLATE_CONVERT | setId={} | position={}", entity.getId(), entity.getPosition());

        RoutineSetTemplateModel model = new RoutineSetTemplateModel();
        model.setId(entity.getId());
        model.setPosition(entity.getPosition());
        model.setSubSetNumber(entity.getSubSetNumber());
        model.setGroupId(entity.getGroupId());
        model.setSetType(entity.getSetType() != null ? entity.getSetType().name() : null);
        model.setRestAfterSet(entity.getRestAfterSet());

        if (entity.getParameters() != null && !entity.getParameters().isEmpty()) {
            log.debug("ROUTINE_SET_TEMPLATE_PARAMS | setId={} | paramCount={}",
                    entity.getId(), entity.getParameters().size());
            List<RoutineSetParameterModel> paramModels = entity.getParameters().stream()
                    .map(this::convertRoutineSetParameter)
                    .collect(Collectors.toList());
            model.setParameters(paramModels);
        } else {
            model.setParameters(new ArrayList<>());
        }

        return model;
    }

    // ✅ BUG 1 CORREGIDO: repetitions faltaba
    public RoutineSetParameterModel convertRoutineSetParameter(RoutineSetParameterEntity entity) {
        log.debug("ROUTINE_SET_PARAM_CONVERT | paramId={} | repetitions={} | integerValue={} | numericValue={}",
                entity.getId(), entity.getRepetitions(), entity.getIntegerValue(), entity.getNumericValue());

        RoutineSetParameterModel model = new RoutineSetParameterModel();
        model.setId(entity.getId());
        model.setParameterId(entity.getParameter().getId());
        model.setNumericValue(entity.getNumericValue());
        model.setDurationValue(entity.getDurationValue());
        model.setIntegerValue(entity.getIntegerValue());
        model.setRepetitions(entity.getRepetitions()); // ✅ CORREGIDO
        return model;
    }

    public RoutineEntity toEntity(RoutineModel domain) {
        log.debug("ROUTINE_CONVERTER_TO_ENTITY | routineId={}", domain.getId());

        RoutineEntity entity = new RoutineEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setIsActive(domain.getIsActive() != null ? domain.getIsActive() : true);
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setLastUsedAt(domain.getLastUsedAt());
        entity.setTrainingDays(domain.getTrainingDays() != null ? domain.getTrainingDays() : new HashSet<>());
        entity.setGoal(domain.getGoal() != null ? domain.getGoal() : "");
        entity.setSessionsPerWeek(domain.getSessionsPerWeek() != null ? domain.getSessionsPerWeek() : 3);

        UserEntity user = springDataUserRepository.findById(domain.getUserId())
                .orElseThrow(() -> {
                    log.error("ROUTINE_CONVERTER_USER_NOT_FOUND | userId={}", domain.getUserId());
                    return new RuntimeException("User not found with id: " + domain.getUserId());
                });
        entity.setUser(user);

        if (domain.getSportId() != null) {
            SportEntity sport = sportRepository.findById(domain.getSportId())
                    .orElseThrow(() -> {
                        log.error("ROUTINE_CONVERTER_SPORT_NOT_FOUND | sportId={}", domain.getSportId());
                        return new RuntimeException("Sport not found with id: " + domain.getSportId());
                    });
            entity.setSport(sport);
        } else {
            entity.setSport(null);
        }

        if (domain.getExercises() != null && !domain.getExercises().isEmpty()) {
            Set<RoutineExerciseEntity> exerciseEntities = domain.getExercises().stream()
                    .map(exerciseModel -> convertToRoutineExerciseEntity(exerciseModel, entity))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            entity.setExercises(exerciseEntities);
        } else {
            entity.setExercises(new HashSet<>());
        }

        log.info("ROUTINE_CONVERTER_TO_ENTITY_OK | routineId={}", domain.getId());
        return entity;
    }

    private RoutineExerciseEntity convertToRoutineExerciseEntity(RoutineExerciseModel model, RoutineEntity routine) {
        RoutineExerciseEntity entity = new RoutineExerciseEntity();
        entity.setId(model.getId());
        entity.setRoutine(routine);

        ExerciseEntity exercise = exerciseRepository.findById(model.getExerciseId())
                .orElseThrow(() -> {
                    log.error("ROUTINE_CONVERTER_EXERCISE_NOT_FOUND | exerciseId={}", model.getExerciseId());
                    return new RuntimeException("Exercise not found with id: " + model.getExerciseId());
                });
        entity.setExercise(exercise);

        entity.setPosition(model.getPosition());
        entity.setSessionNumber(model.getSessionNumber() != null ? model.getSessionNumber() : 1);
        entity.setDayOfWeek(model.getDayOfWeek());
        entity.setSessionOrder(model.getSessionOrder());
        entity.setRestAfterExercise(model.getRestAfterExercise());

        if (model.getTargetParameters() != null && !model.getTargetParameters().isEmpty()) {
            List<RoutineExerciseParameterEntity> paramEntities = model.getTargetParameters().stream()
                    .map(paramModel -> convertToRoutineExerciseParameterEntity(paramModel, entity))
                    .collect(Collectors.toList());
            entity.setTargetParameters(paramEntities);
        } else {
            entity.setTargetParameters(new ArrayList<>());
        }

        if (model.getSets() != null && !model.getSets().isEmpty()) {
            List<RoutineSetTemplateEntity> setEntities = model.getSets().stream()
                    .map(setModel -> convertToRoutineSetTemplateEntity(setModel, entity))
                    .collect(Collectors.toList());
            entity.setSets(setEntities);
        } else {
            entity.setSets(new ArrayList<>());
        }

        return entity;
    }

    private RoutineExerciseParameterEntity convertToRoutineExerciseParameterEntity(
            RoutineExerciseParameterModel model, RoutineExerciseEntity exercise) {
        RoutineExerciseParameterEntity entity = new RoutineExerciseParameterEntity();
        entity.setId(model.getId());
        entity.setRoutineExercise(exercise);

        CustomParameterEntity parameter = customParameterRepository.findById(model.getParameterId())
                .orElseThrow(() -> {
                    log.error("ROUTINE_CONVERTER_PARAM_NOT_FOUND | parameterId={}", model.getParameterId());
                    return new RuntimeException("Parameter not found with id: " + model.getParameterId());
                });
        entity.setParameter(parameter);

        entity.setNumericValue(model.getNumericValue());
        entity.setIntegerValue(model.getIntegerValue());
        entity.setDurationValue(model.getDurationValue());
        entity.setStringValue(model.getStringValue());
        entity.setMinValue(model.getMinValue());
        entity.setMaxValue(model.getMaxValue());
        entity.setDefaultValue(model.getDefaultValue());
        return entity;
    }

    private RoutineSetTemplateEntity convertToRoutineSetTemplateEntity(
            RoutineSetTemplateModel model, RoutineExerciseEntity exercise) {
        RoutineSetTemplateEntity entity = new RoutineSetTemplateEntity();
        entity.setId(model.getId());
        entity.setRoutineExercise(exercise);
        entity.setPosition(model.getPosition());
        entity.setSubSetNumber(model.getSubSetNumber() != null ? model.getSubSetNumber() : 1);
        entity.setGroupId(model.getGroupId());

        try {
            entity.setSetType(model.getSetType() != null ? SetType.valueOf(model.getSetType()) : SetType.NORMAL);
        } catch (IllegalArgumentException e) {
            log.warn("ROUTINE_CONVERTER_INVALID_SET_TYPE | setType={} | defaulting=NORMAL", model.getSetType());
            entity.setSetType(SetType.NORMAL);
        }

        entity.setRestAfterSet(model.getRestAfterSet());

        if (model.getParameters() != null && !model.getParameters().isEmpty()) {
            List<RoutineSetParameterEntity> paramEntities = model.getParameters().stream()
                    .map(paramModel -> convertToRoutineSetParameterEntity(paramModel, entity))
                    .collect(Collectors.toList());
            entity.setParameters(paramEntities);
        } else {
            entity.setParameters(new ArrayList<>());
        }

        return entity;
    }

    private RoutineSetParameterEntity convertToRoutineSetParameterEntity(
            RoutineSetParameterModel model, RoutineSetTemplateEntity setTemplate) {
        RoutineSetParameterEntity entity = new RoutineSetParameterEntity();
        entity.setId(model.getId());
        entity.setSetTemplate(setTemplate);

        CustomParameterEntity parameter = customParameterRepository.findById(model.getParameterId())
                .orElseThrow(() -> {
                    log.error("ROUTINE_CONVERTER_SET_PARAM_NOT_FOUND | parameterId={}", model.getParameterId());
                    return new RuntimeException("Set parameter not found with id: " + model.getParameterId());
                });
        entity.setParameter(parameter);

        entity.setNumericValue(model.getNumericValue());
        entity.setDurationValue(model.getDurationValue());
        entity.setIntegerValue(model.getIntegerValue());
        entity.setRepetitions(model.getRepetitions());
        return entity;
    }

    @Transactional
    public RoutineExerciseEntity addExerciseToRoutine(
            RoutineEntity routine,
            AddExerciseToRoutineRequest request,
            Long exerciseId) {

        ExerciseEntity exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new RuntimeException("Exercise not found: " + exerciseId));

        log.info("ADD_EXERCISE_TO_ROUTINE | routineId={} | exerciseId={}", routine.getId(), exercise.getId());
        int nextPosition = routine.getExercises().stream()
                .mapToInt(RoutineExerciseEntity::getPosition)
                .max().orElse(0) + 1;

        Integer sessionOrder = request.getSessionOrder();
        if (sessionOrder == null) {
            sessionOrder = routine.getExercises().stream()
                    .filter(e -> request.getSessionNumber() != null &&
                            request.getSessionNumber().equals(e.getSessionNumber()))
                    .mapToInt(RoutineExerciseEntity::getSessionOrder)
                    .max().orElse(0) + 1;
        }

        RoutineExerciseEntity routineExercise = new RoutineExerciseEntity();
        routineExercise.setRoutine(routine);
        routineExercise.setExercise(exercise);
        routineExercise.setPosition(nextPosition);
        routineExercise.setSessionNumber(request.getSessionNumber() != null ? request.getSessionNumber() : 1);
        routineExercise.setSessionOrder(sessionOrder);
        routineExercise.setRestAfterExercise(request.getRestAfterExercise());

        if (request.getDayOfWeek() != null && !request.getDayOfWeek().toString().isEmpty()) {
            try {
                routineExercise.setDayOfWeek(DayOfWeek.valueOf(request.getDayOfWeek().toString()));
            } catch (IllegalArgumentException e) {
                log.warn("ADD_EXERCISE_INVALID_DAY | dayOfWeek={}", request.getDayOfWeek());
                routineExercise.setDayOfWeek(null);
            }
        }

        if (request.getTargetParameters() != null && !request.getTargetParameters().isEmpty()) {
            log.debug("ADD_EXERCISE_TARGET_PARAMS | count={}", request.getTargetParameters().size());
            List<RoutineExerciseParameterEntity> targetParams = request.getTargetParameters().stream()
                    .map(paramRequest -> createRoutineExerciseParameter(paramRequest, routineExercise))
                    .collect(Collectors.toList());
            routineExercise.setTargetParameters(targetParams);
        } else {
            routineExercise.setTargetParameters(new ArrayList<>());
        }

        if (request.getSets() != null && !request.getSets().isEmpty()) {
            log.debug("ADD_EXERCISE_SETS | count={}", request.getSets().size());
            List<RoutineSetTemplateEntity> sets = request.getSets().stream()
                    .map(setRequest -> createRoutineSetTemplate(setRequest, routineExercise))
                    .collect(Collectors.toList());
            routineExercise.setSets(sets);
        } else {
            routineExercise.setSets(new ArrayList<>());
        }

        routine.getExercises().add(routineExercise);

        log.info("ADD_EXERCISE_TO_ROUTINE_OK | position={} | session={}", nextPosition, request.getSessionNumber());
        return routineExercise;
    }

    private RoutineExerciseParameterEntity createRoutineExerciseParameter(
            AddExerciseToRoutineRequest.ExerciseParameterRequest request,
            RoutineExerciseEntity routineExercise) {

        CustomParameterEntity parameter = customParameterRepository.findById(request.getParameterId())
                .orElseThrow(() -> {
                    log.error("CREATE_EXERCISE_PARAM_NOT_FOUND | parameterId={}", request.getParameterId());
                    return new RuntimeException("Parameter not found: " + request.getParameterId());
                });

        RoutineExerciseParameterEntity entity = new RoutineExerciseParameterEntity();
        entity.setRoutineExercise(routineExercise);
        entity.setParameter(parameter);
        entity.setNumericValue(request.getNumericValue());
        entity.setIntegerValue(request.getIntegerValue());
        entity.setDurationValue(request.getDurationValue());
        entity.setStringValue(request.getStringValue());
        entity.setMinValue(request.getMinValue());
        entity.setMaxValue(request.getMaxValue());
        entity.setDefaultValue(request.getDefaultValue());
        return entity;
    }

    private RoutineSetTemplateEntity createRoutineSetTemplate(
            AddExerciseToRoutineRequest.SetTemplateRequest request,
            RoutineExerciseEntity routineExercise) {

        RoutineSetTemplateEntity entity = new RoutineSetTemplateEntity();
        entity.setRoutineExercise(routineExercise);
        entity.setPosition(request.getPosition());
        entity.setSubSetNumber(request.getSubSetNumber() != null ? request.getSubSetNumber() : 1);

        try {
            entity.setSetType(request.getSetType() != null ? SetType.valueOf(request.getSetType()) : SetType.NORMAL);
        } catch (IllegalArgumentException e) {
            log.warn("CREATE_SET_TEMPLATE_INVALID_TYPE | setType={} | defaulting=NORMAL", request.getSetType());
            entity.setSetType(SetType.NORMAL);
        }

        entity.setRestAfterSet(request.getRestAfterSet());
        entity.setGroupId(request.getGroupId());

        if (request.getParameters() != null && !request.getParameters().isEmpty()) {
            List<RoutineSetParameterEntity> setParams = request.getParameters().stream()
                    .map(paramRequest -> createRoutineSetParameter(paramRequest, entity))
                    .collect(Collectors.toList());
            entity.setParameters(setParams);
        } else {
            entity.setParameters(new ArrayList<>());
        }

        return entity;
    }

    // ✅ BUG 3 CORREGIDO: repetitions faltaba
    private RoutineSetParameterEntity createRoutineSetParameter(
            AddExerciseToRoutineRequest.SetParameterRequest request,
            RoutineSetTemplateEntity setTemplate) {

        CustomParameterEntity parameter = customParameterRepository.findById(request.getParameterId())
                .orElseThrow(() -> {
                    log.error("CREATE_SET_PARAM_NOT_FOUND | parameterId={}", request.getParameterId());
                    return new RuntimeException("Set parameter not found: " + request.getParameterId());
                });

        RoutineSetParameterEntity entity = new RoutineSetParameterEntity();
        entity.setSetTemplate(setTemplate);
        entity.setParameter(parameter);
        entity.setNumericValue(request.getNumericValue());
        entity.setDurationValue(request.getDurationValue());
        entity.setIntegerValue(request.getIntegerValue());
        entity.setRepetitions(request.getRepetitions()); // ✅ CORREGIDO
        return entity;
    }
}