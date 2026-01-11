package com.fitapp.backend.infrastructure.persistence.converter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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
        log.debug("Convirtiendo RoutineEntity a RoutineModel: {}", entity.getId());
        
        RoutineModel routine = new RoutineModel();
        routine.setId(entity.getId());
        routine.setName(entity.getName());
        routine.setDescription(entity.getDescription());
        routine.setIsActive(entity.getIsActive() != null ? entity.getIsActive() : true);
        routine.setCreatedAt(entity.getCreatedAt());
        routine.setUpdatedAt(entity.getUpdatedAt());
        routine.setLastUsedAt(entity.getLastUsedAt());
        routine.setUserId(entity.getUser().getId());

        if (entity.getSport() != null) {
            routine.setSportId(entity.getSport().getId());
        } else {
            routine.setSportId(null);
        }

        routine.setTrainingDays(entity.getTrainingDays() != null ? entity.getTrainingDays() : new HashSet<>());
        routine.setGoal(entity.getGoal() != null ? entity.getGoal() : "");
        routine.setSessionsPerWeek(entity.getSessionsPerWeek() != null ? entity.getSessionsPerWeek() : 3);

        // Convertir ejercicios con logs
        if (entity.getExercises() != null) {
            log.debug("Convirtiendo {} ejercicios para rutina {}", entity.getExercises().size(), entity.getId());
            List<RoutineExerciseModel> exerciseModels = entity.getExercises().stream()
                    .map(this::convertRoutineExercise)
                    .sorted(Comparator.comparing(RoutineExerciseModel::getPosition))
                    .collect(Collectors.toList());
            routine.setExercises(exerciseModels);
        } else {
            routine.setExercises(new ArrayList<>());
        }

        log.info("Conversión completada para rutina {}", entity.getId());
        return routine;
    }

    // Método público para usar en otros lugares
    public RoutineExerciseModel convertRoutineExercise(RoutineExerciseEntity entity) {
        log.debug("Convirtiendo RoutineExerciseEntity: {}", entity.getId());
        
        RoutineExerciseModel model = new RoutineExerciseModel();
        model.setId(entity.getId());
        model.setRoutineId(entity.getRoutine().getId());
        model.setExerciseId(entity.getExercise().getId());
        model.setPosition(entity.getPosition());
        model.setSessionNumber(entity.getSessionNumber());
        model.setDayOfWeek(entity.getDayOfWeek());
        model.setSessionOrder(entity.getSessionOrder());
        model.setRestAfterExercise(entity.getRestAfterExercise());

        // Convertir parámetros objetivo
        if (entity.getTargetParameters() != null && !entity.getTargetParameters().isEmpty()) {
            log.debug("Convirtiendo {} parámetros para ejercicio {}", entity.getTargetParameters().size(), entity.getId());
            List<RoutineExerciseParameterModel> paramModels = entity.getTargetParameters().stream()
                    .map(this::convertRoutineExerciseParameter)
                    .collect(Collectors.toList());
            model.setTargetParameters(paramModels);
        } else {
            model.setTargetParameters(new ArrayList<>());
        }

        // Convertir sets
        if (entity.getSets() != null && !entity.getSets().isEmpty()) {
            log.debug("Convirtiendo {} sets para ejercicio {}", entity.getSets().size(), entity.getId());
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

    // Método para convertir parámetros de ejercicio
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

    // Método para convertir sets template
    public RoutineSetTemplateModel convertRoutineSetTemplate(RoutineSetTemplateEntity entity) {
        RoutineSetTemplateModel model = new RoutineSetTemplateModel();
        model.setId(entity.getId());
        model.setPosition(entity.getPosition());
        model.setSubSetNumber(entity.getSubSetNumber());
        model.setGroupId(entity.getGroupId());
        model.setSetType(entity.getSetType() != null ? entity.getSetType().name() : null);
        model.setRestAfterSet(entity.getRestAfterSet());

        // Convertir parámetros del set
        if (entity.getParameters() != null && !entity.getParameters().isEmpty()) {
            List<RoutineSetParameterModel> paramModels = entity.getParameters().stream()
                    .map(this::convertRoutineSetParameter)
                    .collect(Collectors.toList());
            model.setParameters(paramModels);
        } else {
            model.setParameters(new ArrayList<>());
        }

        return model;
    }

    // Método para convertir parámetros de set
    public RoutineSetParameterModel convertRoutineSetParameter(RoutineSetParameterEntity entity) {
        RoutineSetParameterModel model = new RoutineSetParameterModel();
        model.setId(entity.getId());
        model.setParameterId(entity.getParameter().getId());
        model.setNumericValue(entity.getNumericValue());
        model.setDurationValue(entity.getDurationValue());
        model.setIntegerValue(entity.getIntegerValue());
        model.setMinValue(entity.getMinValue());
        model.setMaxValue(entity.getMaxValue());
        return model;
    }

    @Transactional
    public RoutineEntity toEntity(RoutineModel domain) {
        log.debug("Convirtiendo RoutineModel a RoutineEntity: {}", domain.getId());
        
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
                    log.error("Usuario no encontrado con id: {}", domain.getUserId());
                    return new RuntimeException("User not found with id: " + domain.getUserId());
                });
        entity.setUser(user);

        if (domain.getSportId() != null) {
            SportEntity sport = sportRepository.findById(domain.getSportId())
                    .orElseThrow(() -> {
                        log.error("Deporte no encontrado con id: {}", domain.getSportId());
                        return new RuntimeException("Sport not found with id: " + domain.getSportId());
                    });
            entity.setSport(sport);
        } else {
            entity.setSport(null);
        }

        // Convertir ejercicios si existen
        if (domain.getExercises() != null && !domain.getExercises().isEmpty()) {
            log.debug("Convirtiendo {} ejercicios del modelo", domain.getExercises().size());
            List<RoutineExerciseEntity> exerciseEntities = domain.getExercises().stream()
                    .map(exerciseModel -> convertToRoutineExerciseEntity(exerciseModel, entity))
                    .collect(Collectors.toList());
            entity.setExercises(exerciseEntities);
        } else {
            entity.setExercises(new ArrayList<>());
        }

        log.info("Conversión a entidad completada para rutina {}", domain.getId());
        return entity;
    }

    private RoutineExerciseEntity convertToRoutineExerciseEntity(RoutineExerciseModel model, RoutineEntity routine) {
        RoutineExerciseEntity entity = new RoutineExerciseEntity();
        entity.setId(model.getId());
        entity.setRoutine(routine);
        
        ExerciseEntity exercise = exerciseRepository.findById(model.getExerciseId())
                .orElseThrow(() -> {
                    log.error("Ejercicio no encontrado con id: {}", model.getExerciseId());
                    return new RuntimeException("Exercise not found with id: " + model.getExerciseId());
                });
        entity.setExercise(exercise);
        
        entity.setPosition(model.getPosition());
        entity.setSessionNumber(model.getSessionNumber() != null ? model.getSessionNumber() : 1);
        entity.setDayOfWeek(model.getDayOfWeek());
        entity.setSessionOrder(model.getSessionOrder());
        entity.setRestAfterExercise(model.getRestAfterExercise());

        // Convertir parámetros objetivo
        if (model.getTargetParameters() != null && !model.getTargetParameters().isEmpty()) {
            log.debug("Convirtiendo {} parámetros para ejercicio", model.getTargetParameters().size());
            List<RoutineExerciseParameterEntity> paramEntities = model.getTargetParameters().stream()
                    .map(paramModel -> convertToRoutineExerciseParameterEntity(paramModel, entity))
                    .collect(Collectors.toList());
            entity.setTargetParameters(paramEntities);
        } else {
            entity.setTargetParameters(new ArrayList<>());
        }

        // Convertir sets
        if (model.getSets() != null && !model.getSets().isEmpty()) {
            log.debug("Convirtiendo {} sets para ejercicio", model.getSets().size());
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
                    log.error("Parámetro no encontrado con id: {}", model.getParameterId());
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
            log.warn("Tipo de set inválido: {}. Usando NORMAL por defecto.", model.getSetType());
            entity.setSetType(SetType.NORMAL);
        }
        
        entity.setRestAfterSet(model.getRestAfterSet());

        // Convertir parámetros del set
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
                    log.error("Parámetro de set no encontrado con id: {}", model.getParameterId());
                    return new RuntimeException("Set parameter not found with id: " + model.getParameterId());
                });
        entity.setParameter(parameter);
        
        entity.setNumericValue(model.getNumericValue());
        entity.setDurationValue(model.getDurationValue());
        entity.setIntegerValue(model.getIntegerValue());
        entity.setMinValue(model.getMinValue());
        entity.setMaxValue(model.getMaxValue());
        
        return entity;
    }

    // Método para agregar ejercicio a rutina existente
    @Transactional
    public RoutineExerciseEntity addExerciseToRoutine(
            RoutineEntity routine, 
            AddExerciseToRoutineRequest request,
            ExerciseEntity exercise) {
        
        log.info("Agregando ejercicio {} a rutina {}", exercise.getId(), routine.getId());
        
        // Determinar la siguiente posición disponible
        int nextPosition = routine.getExercises().stream()
                .mapToInt(RoutineExerciseEntity::getPosition)
                .max()
                .orElse(0) + 1;

        // Determinar el sessionOrder si no se proporciona
        Integer sessionOrder = request.getSessionOrder();
        if (sessionOrder == null) {
            sessionOrder = routine.getExercises().stream()
                    .filter(e -> request.getSessionNumber() != null && 
                            request.getSessionNumber().equals(e.getSessionNumber()))
                    .mapToInt(RoutineExerciseEntity::getSessionOrder)
                    .max()
                    .orElse(0) + 1;
        }

        RoutineExerciseEntity routineExercise = new RoutineExerciseEntity();
        routineExercise.setRoutine(routine);
        routineExercise.setExercise(exercise);
        routineExercise.setPosition(nextPosition);
        routineExercise.setSessionNumber(request.getSessionNumber() != null ? request.getSessionNumber() : 1);
        routineExercise.setSessionOrder(sessionOrder);
        routineExercise.setRestAfterExercise(request.getRestAfterExercise());

        // Configurar dayOfWeek si se proporciona
        if (request.getDayOfWeek() != null && !request.getDayOfWeek().isEmpty()) {
            try {
                DayOfWeek day = DayOfWeek.valueOf(request.getDayOfWeek().toUpperCase());
                routineExercise.setDayOfWeek(day);
            } catch (IllegalArgumentException e) {
                log.warn("Día de la semana inválido: {}", request.getDayOfWeek());
                routineExercise.setDayOfWeek(null);
            }
        }

        // Crear parámetros objetivo
        if (request.getTargetParameters() != null && !request.getTargetParameters().isEmpty()) {
            log.debug("Creando {} parámetros objetivo", request.getTargetParameters().size());
            List<RoutineExerciseParameterEntity> targetParams = request.getTargetParameters().stream()
                    .map(paramRequest -> createRoutineExerciseParameter(paramRequest, routineExercise))
                    .collect(Collectors.toList());
            routineExercise.setTargetParameters(targetParams);
        } else {
            routineExercise.setTargetParameters(new ArrayList<>());
        }

        // Crear sets
        if (request.getSets() != null && !request.getSets().isEmpty()) {
            log.debug("Creando {} sets", request.getSets().size());
            List<RoutineSetTemplateEntity> sets = request.getSets().stream()
                    .map(setRequest -> createRoutineSetTemplate(setRequest, routineExercise))
                    .collect(Collectors.toList());
            routineExercise.setSets(sets);
        } else {
            routineExercise.setSets(new ArrayList<>());
        }

        // Agregar a la rutina
        routine.getExercises().add(routineExercise);
        
        log.info("Ejercicio agregado con éxito. Posición: {}, Sesión: {}", nextPosition, request.getSessionNumber());
        return routineExercise;
    }

    private RoutineExerciseParameterEntity createRoutineExerciseParameter(
            AddExerciseToRoutineRequest.ExerciseParameterRequest request,
            RoutineExerciseEntity routineExercise) {
        
        CustomParameterEntity parameter = customParameterRepository.findById(request.getParameterId())
                .orElseThrow(() -> {
                    log.error("Parámetro no encontrado: {}", request.getParameterId());
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
            log.warn("Tipo de set inválido: {}. Usando NORMAL por defecto.", request.getSetType());
            entity.setSetType(SetType.NORMAL);
        }
        
        entity.setRestAfterSet(request.getRestAfterSet());
        entity.setGroupId(request.getGroupId());

        // Crear parámetros del set
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

    private RoutineSetParameterEntity createRoutineSetParameter(
            AddExerciseToRoutineRequest.SetParameterRequest request,
            RoutineSetTemplateEntity setTemplate) {
        
        CustomParameterEntity parameter = customParameterRepository.findById(request.getParameterId())
                .orElseThrow(() -> {
                    log.error("Parámetro de set no encontrado: {}", request.getParameterId());
                    return new RuntimeException("Set parameter not found: " + request.getParameterId());
                });

        RoutineSetParameterEntity entity = new RoutineSetParameterEntity();
        entity.setSetTemplate(setTemplate);
        entity.setParameter(parameter);
        entity.setNumericValue(request.getNumericValue());
        entity.setDurationValue(request.getDurationValue());
        entity.setIntegerValue(request.getIntegerValue());
        entity.setMinValue(request.getMinValue());
        entity.setMaxValue(request.getMaxValue());
        
        return entity;
    }
}