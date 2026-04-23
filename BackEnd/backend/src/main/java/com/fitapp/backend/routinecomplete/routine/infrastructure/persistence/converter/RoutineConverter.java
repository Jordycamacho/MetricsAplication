package com.fitapp.backend.routinecomplete.routine.infrastructure.persistence.converter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.fitapp.backend.Exercise.infrastructure.persistence.entity.ExerciseEntity;
import com.fitapp.backend.Exercise.infrastructure.persistence.repository.ExerciseRepository;
import com.fitapp.backend.domain.model.RoutineExerciseParameterModel;
import com.fitapp.backend.domain.model.RoutineSetParameterModel;
import com.fitapp.backend.infrastructure.persistence.entity.RoutineExerciseParameterEntity;
import com.fitapp.backend.infrastructure.persistence.entity.RoutineSetParameterEntity;
import com.fitapp.backend.infrastructure.persistence.entity.RoutineSetTemplateEntity;
import com.fitapp.backend.infrastructure.persistence.entity.enums.DayOfWeek;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SetType;
import com.fitapp.backend.infrastructure.persistence.repository.RoutineExerciseParameterRepository;
import com.fitapp.backend.infrastructure.persistence.repository.RoutineSetTemplateRepository;
import com.fitapp.backend.parameter.infrastructure.persistence.entity.CustomParameterEntity;
import com.fitapp.backend.parameter.infrastructure.persistence.repository.CustomParameterRepository;
import com.fitapp.backend.routinecomplete.package_.infrastructure.persistence.entity.PackageEntity;
import com.fitapp.backend.routinecomplete.package_.infrastructure.persistence.repository.PackageRepository;
import com.fitapp.backend.routinecomplete.routine.domain.model.RoutineModel;
import com.fitapp.backend.routinecomplete.routine.infrastructure.persistence.entity.RoutineEntity;
import com.fitapp.backend.routinecomplete.routine.infrastructure.persistence.repository.RoutineRepository;
import com.fitapp.backend.routinecomplete.routineexercise.aplication.dto.request.AddExerciseToRoutineRequest;
import com.fitapp.backend.routinecomplete.routineexercise.domain.model.RoutineExerciseModel;
import com.fitapp.backend.routinecomplete.routineexercise.infrastructure.persistence.entity.RoutineExerciseEntity;
import com.fitapp.backend.routinecomplete.routineexercise.infrastructure.persistence.repository.RoutineExerciseRepository;
import com.fitapp.backend.routinecomplete.routinesetemplate.domain.model.RoutineSetTemplateModel;
import com.fitapp.backend.sport.infrastructure.persistence.entity.SportEntity;
import com.fitapp.backend.sport.infrastructure.persistence.repository.SportRepository;
import com.fitapp.backend.user.infrastructure.persistence.entity.UserEntity;
import com.fitapp.backend.user.infrastructure.persistence.repository.SpringDataUserRepository;

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
    private final PackageRepository packageRepository;
    private final RoutineRepository routineRepository;

    // ── toDomain ──────────────────────────────────────────────────────────────

    public RoutineModel toDomain(RoutineEntity entity) {
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
        
        // V2 fields
        routine.setOriginalRoutineId(entity.getOriginalRoutine() != null ? entity.getOriginalRoutine().getId() : null);
        routine.setVersion(entity.getVersion());
        routine.setPackageId(entity.getPack() != null ? entity.getPack().getId() : null);
        routine.setExportKey(entity.getExportKey());
        routine.setTimesPurchased(entity.getTimesPurchased() != null ? entity.getTimesPurchased() : 0);

        if (entity.getExercises() != null) {
            List<RoutineExerciseModel> exerciseModels = entity.getExercises().stream()
                    .map(this::convertRoutineExercise)
                    .sorted(Comparator.comparing(RoutineExerciseModel::getPosition))
                    .collect(Collectors.toList());
            routine.setExercises(exerciseModels);
        } else {
            routine.setExercises(new ArrayList<>());
        }

        return routine;
    }

    public RoutineExerciseModel convertRoutineExercise(RoutineExerciseEntity entity) {
        RoutineExerciseModel model = new RoutineExerciseModel();
        model.setId(entity.getId());
        model.setRoutineId(entity.getRoutine().getId());
        model.setExerciseId(entity.getExercise().getId());
        model.setPosition(entity.getPosition());
        model.setSessionNumber(entity.getSessionNumber());
        model.setDayOfWeek(entity.getDayOfWeek());
        model.setSessionOrder(entity.getSessionOrder());
        model.setRestAfterExercise(entity.getRestAfterExercise());
        
        // V2 Fields
        model.setCircuitGroupId(entity.getCircuitGroupId());
        model.setCircuitRoundCount(entity.getCircuitRoundCount());
        model.setSuperSetGroupId(entity.getSuperSetGroupId());
        model.setAmrapDurationSeconds(entity.getAmrapDurationSeconds());
        model.setEmomIntervalSeconds(entity.getEmomIntervalSeconds());
        model.setEmomTotalRounds(entity.getEmomTotalRounds());
        model.setTabataWorkSeconds(entity.getTabataWorkSeconds());
        model.setTabataRestSeconds(entity.getTabataRestSeconds());
        model.setTabataRounds(entity.getTabataRounds());
        model.setNotes(entity.getNotes());

        model.setTargetParameters(entity.getTargetParameters() != null && !entity.getTargetParameters().isEmpty()
                ? entity.getTargetParameters().stream()
                        .map(this::convertRoutineExerciseParameter)
                        .collect(Collectors.toList())
                : new ArrayList<>());

        model.setSets(entity.getSets() != null && !entity.getSets().isEmpty()
                ? entity.getSets().stream()
                        .sorted(Comparator.comparing(RoutineSetTemplateEntity::getPosition))
                        .map(this::convertRoutineSetTemplate)
                        .collect(Collectors.toList())
                : new ArrayList<>());

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
        RoutineSetTemplateModel model = new RoutineSetTemplateModel();
        model.setId(entity.getId());
        model.setPosition(entity.getPosition());
        model.setSubSetNumber(entity.getSubSetNumber());
        model.setGroupId(entity.getGroupId());
        model.setSetType(entity.getSetType() != null ? entity.getSetType().name() : null);
        model.setRestAfterSet(entity.getRestAfterSet());

        model.setParameters(entity.getParameters() != null && !entity.getParameters().isEmpty()
                ? entity.getParameters().stream()
                        .map(this::convertRoutineSetParameter)
                        .collect(Collectors.toList())
                : new ArrayList<>());

        return model;
    }

    public RoutineSetParameterModel convertRoutineSetParameter(RoutineSetParameterEntity entity) {
        RoutineSetParameterModel model = new RoutineSetParameterModel();
        model.setId(entity.getId());
        model.setParameterId(entity.getParameter().getId());
        model.setNumericValue(entity.getNumericValue());
        model.setDurationValue(entity.getDurationValue());
        model.setIntegerValue(entity.getIntegerValue());
        model.setRepetitions(entity.getRepetitions());
        return model;
    }

    // ── toEntity ──────────────────────────────────────────────────────────────

    public RoutineEntity toEntity(RoutineModel domain) {
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
        entity.setVersion(domain.getVersion());
        entity.setExportKey(domain.getExportKey());
        entity.setTimesPurchased(domain.getTimesPurchased() != null ? domain.getTimesPurchased() : 0);

        UserEntity user = springDataUserRepository.findById(domain.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found: " + domain.getUserId()));
        entity.setUser(user);

        if (domain.getSportId() != null) {
            SportEntity sport = sportRepository.findById(domain.getSportId())
                    .orElseThrow(() -> new RuntimeException("Sport not found: " + domain.getSportId()));
            entity.setSport(sport);
        } else {
            entity.setSport(null);
        }
        
        if (domain.getOriginalRoutineId() != null) {
            RoutineEntity originalRoutine = routineRepository.findById(domain.getOriginalRoutineId())
                    .orElseThrow(() -> new RuntimeException("Original routine not found: " + domain.getOriginalRoutineId()));
            entity.setOriginalRoutine(originalRoutine);
        }
        
        if (domain.getPackageId() != null) {
            PackageEntity pack = packageRepository.findById(domain.getPackageId())
                    .orElseThrow(() -> new RuntimeException("Package not found: " + domain.getPackageId()));
            entity.setPack(pack);
        }

        if (domain.getExercises() != null && !domain.getExercises().isEmpty()) {
            Set<RoutineExerciseEntity> exerciseEntities = domain.getExercises().stream()
                    .map(m -> convertToRoutineExerciseEntity(m, entity))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            entity.setExercises(exerciseEntities);
        } else {
            entity.setExercises(new HashSet<>());
        }

        return entity;
    }

    private RoutineExerciseEntity convertToRoutineExerciseEntity(RoutineExerciseModel model, RoutineEntity routine) {
        RoutineExerciseEntity entity = new RoutineExerciseEntity();
        entity.setId(model.getId());
        entity.setRoutine(routine);

        ExerciseEntity exercise = exerciseRepository.findById(model.getExerciseId())
                .orElseThrow(() -> new RuntimeException("Exercise not found: " + model.getExerciseId()));
        entity.setExercise(exercise);

        entity.setPosition(model.getPosition());
        entity.setSessionNumber(model.getSessionNumber() != null ? model.getSessionNumber() : 1);
        entity.setDayOfWeek(model.getDayOfWeek());
        entity.setSessionOrder(model.getSessionOrder());
        entity.setRestAfterExercise(model.getRestAfterExercise());
        entity.setCircuitGroupId(model.getCircuitGroupId());
        entity.setCircuitRoundCount(model.getCircuitRoundCount());
        entity.setSuperSetGroupId(model.getSuperSetGroupId());
        entity.setAmrapDurationSeconds(model.getAmrapDurationSeconds());
        entity.setEmomIntervalSeconds(model.getEmomIntervalSeconds());
        entity.setEmomTotalRounds(model.getEmomTotalRounds());
        entity.setTabataWorkSeconds(model.getTabataWorkSeconds());
        entity.setTabataRestSeconds(model.getTabataRestSeconds());
        entity.setTabataRounds(model.getTabataRounds());
        entity.setNotes(model.getNotes());

        entity.setTargetParameters(model.getTargetParameters() != null && !model.getTargetParameters().isEmpty()
                ? model.getTargetParameters().stream()
                        .map(pm -> convertToRoutineExerciseParameterEntity(pm, entity))
                        .collect(Collectors.toList())
                : new ArrayList<>());

        entity.setSets(model.getSets() != null && !model.getSets().isEmpty()
                ? model.getSets().stream()
                        .map(sm -> convertToRoutineSetTemplateEntity(sm, entity))
                        .collect(Collectors.toList())
                : new ArrayList<>());

        return entity;
    }

    private RoutineExerciseParameterEntity convertToRoutineExerciseParameterEntity(
            RoutineExerciseParameterModel model, RoutineExerciseEntity exercise) {
        CustomParameterEntity parameter = customParameterRepository.findById(model.getParameterId())
                .orElseThrow(() -> new RuntimeException("Parameter not found: " + model.getParameterId()));

        RoutineExerciseParameterEntity entity = new RoutineExerciseParameterEntity();
        entity.setId(model.getId());
        entity.setRoutineExercise(exercise);
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
        entity.setRestAfterSet(model.getRestAfterSet());

        try {
            entity.setSetType(model.getSetType() != null ? SetType.valueOf(model.getSetType()) : SetType.NORMAL);
        } catch (IllegalArgumentException e) {
            log.warn("INVALID_SET_TYPE | value={} | defaulting=NORMAL", model.getSetType());
            entity.setSetType(SetType.NORMAL);
        }

        entity.setParameters(model.getParameters() != null && !model.getParameters().isEmpty()
                ? model.getParameters().stream()
                        .map(pm -> convertToRoutineSetParameterEntity(pm, entity))
                        .collect(Collectors.toList())
                : new ArrayList<>());

        return entity;
    }

    private RoutineSetParameterEntity convertToRoutineSetParameterEntity(
            RoutineSetParameterModel model, RoutineSetTemplateEntity setTemplate) {
        CustomParameterEntity parameter = customParameterRepository.findById(model.getParameterId())
                .orElseThrow(() -> new RuntimeException("Set parameter not found: " + model.getParameterId()));

        RoutineSetParameterEntity entity = new RoutineSetParameterEntity();
        entity.setId(model.getId());
        entity.setSetTemplate(setTemplate);
        entity.setParameter(parameter);
        entity.setNumericValue(model.getNumericValue());
        entity.setDurationValue(model.getDurationValue());
        entity.setIntegerValue(model.getIntegerValue());
        entity.setRepetitions(model.getRepetitions());
        return entity;
    }

    // ── addExerciseToRoutine (operación de negocio) ──────────────────────────

    public RoutineExerciseEntity addExerciseToRoutine(
            RoutineEntity routine, AddExerciseToRoutineRequest request, Long exerciseId) {

        ExerciseEntity exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new RuntimeException("Exercise not found: " + exerciseId));

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
        routineExercise.setCircuitGroupId(request.getCircuitGroupId());
        routineExercise.setCircuitRoundCount(request.getCircuitRoundCount());
        routineExercise.setSuperSetGroupId(request.getSuperSetGroupId());
        routineExercise.setAmrapDurationSeconds(request.getAmrapDurationSeconds());
        routineExercise.setEmomIntervalSeconds(request.getEmomIntervalSeconds());
        routineExercise.setEmomTotalRounds(request.getEmomTotalRounds());
        routineExercise.setTabataWorkSeconds(request.getTabataWorkSeconds());
        routineExercise.setTabataRestSeconds(request.getTabataRestSeconds());
        routineExercise.setTabataRounds(request.getTabataRounds());
        routineExercise.setNotes(request.getNotes());

        routineExercise.setTargetParameters(
                request.getTargetParameters() != null && !request.getTargetParameters().isEmpty()
                        ? request.getTargetParameters().stream()
                                .map(pr -> createRoutineExerciseParameter(pr, routineExercise))
                                .collect(Collectors.toList())
                        : new ArrayList<>());

        routineExercise.setSets(
                request.getSets() != null && !request.getSets().isEmpty()
                        ? request.getSets().stream()
                                .map(sr -> createRoutineSetTemplate(sr, routineExercise))
                                .collect(Collectors.toList())
                        : new ArrayList<>());

        routine.getExercises().add(routineExercise);

        log.info("ADD_EXERCISE_OK | routineId={} | exerciseId={} | position={} | session={} | circuit={} | superset={} | amrap={} | emom={} | tabata={}",
                routine.getId(), exercise.getId(), nextPosition, request.getSessionNumber(),
                request.getCircuitGroupId(), request.getSuperSetGroupId(), request.getAmrapDurationSeconds(),
                request.getEmomTotalRounds(), request.getTabataRounds());
        return routineExercise;
    }

    private RoutineExerciseParameterEntity createRoutineExerciseParameter(
            AddExerciseToRoutineRequest.ExerciseParameterRequest request, RoutineExerciseEntity routineExercise) {

        CustomParameterEntity parameter = customParameterRepository.findById(request.getParameterId())
                .orElseThrow(() -> new RuntimeException("Parameter not found: " + request.getParameterId()));

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
            AddExerciseToRoutineRequest.SetTemplateRequest request, RoutineExerciseEntity routineExercise) {

        RoutineSetTemplateEntity entity = new RoutineSetTemplateEntity();
        entity.setRoutineExercise(routineExercise);
        entity.setPosition(request.getPosition());
        entity.setSubSetNumber(request.getSubSetNumber() != null ? request.getSubSetNumber() : 1);
        entity.setGroupId(request.getGroupId());
        entity.setRestAfterSet(request.getRestAfterSet());

        try {
            entity.setSetType(request.getSetType() != null ? SetType.valueOf(request.getSetType()) : SetType.NORMAL);
        } catch (IllegalArgumentException e) {
            log.warn("CREATE_SET_TEMPLATE_INVALID_TYPE | value={} | defaulting=NORMAL", request.getSetType());
            entity.setSetType(SetType.NORMAL);
        }

        entity.setParameters(request.getParameters() != null && !request.getParameters().isEmpty()
                ? request.getParameters().stream()
                        .map(pr -> createRoutineSetParameter(pr, entity))
                        .collect(Collectors.toList())
                : new ArrayList<>());

        return entity;
    }

    private RoutineSetParameterEntity createRoutineSetParameter(
            AddExerciseToRoutineRequest.SetParameterRequest request, RoutineSetTemplateEntity setTemplate) {

        CustomParameterEntity parameter = customParameterRepository.findById(request.getParameterId())
                .orElseThrow(() -> new RuntimeException("Set parameter not found: " + request.getParameterId()));

        RoutineSetParameterEntity entity = new RoutineSetParameterEntity();
        entity.setSetTemplate(setTemplate);
        entity.setParameter(parameter);
        entity.setNumericValue(request.getNumericValue());
        entity.setDurationValue(request.getDurationValue());
        entity.setIntegerValue(request.getIntegerValue());
        entity.setRepetitions(request.getRepetitions());
        return entity;
    }
}