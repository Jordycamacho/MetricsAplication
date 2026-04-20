package com.fitapp.backend.routinecomplete.routine.aplication.service;

import com.fitapp.backend.application.dto.RoutineSetParameter.response.RoutineSetParameterResponse;
import com.fitapp.backend.application.dto.RoutineSetTemplate.response.RoutineSetTemplateResponse;
import com.fitapp.backend.application.dto.page.PageResponse;
import com.fitapp.backend.application.dto.routine.response.RoutineExerciseParameterResponse;
import com.fitapp.backend.application.dto.routine.response.RoutineExerciseResponse;
import com.fitapp.backend.application.logging.RoutineServiceLogger;
import com.fitapp.backend.application.ports.output.ExercisePersistencePort;
import com.fitapp.backend.application.ports.output.SportPersistencePort;
import com.fitapp.backend.application.ports.output.UserPersistencePort;
import com.fitapp.backend.application.service.SubscriptionLimitChecker;
import com.fitapp.backend.domain.exception.RoutineNotFoundException;
import com.fitapp.backend.domain.model.ExerciseModel;
import com.fitapp.backend.domain.model.RoutineExerciseModel;
import com.fitapp.backend.domain.model.RoutineExerciseParameterModel;
import com.fitapp.backend.domain.model.RoutineSetParameterModel;
import com.fitapp.backend.domain.model.RoutineSetTemplateModel;
import com.fitapp.backend.domain.model.SportModel;
import com.fitapp.backend.domain.model.UserModel;
import com.fitapp.backend.infrastructure.persistence.entity.enums.DayOfWeek;
import com.fitapp.backend.parameter.domain.model.CustomParameterModel;
import com.fitapp.backend.parameter.infrastructure.persistence.adapter.CustomParameterPersistencePort;
import com.fitapp.backend.routinecomplete.routine.aplication.dto.request.CreateRoutineRequest;
import com.fitapp.backend.routinecomplete.routine.aplication.dto.request.RoutineFilterRequest;
import com.fitapp.backend.routinecomplete.routine.aplication.dto.request.UpdateRoutineRequest;
import com.fitapp.backend.routinecomplete.routine.aplication.dto.response.RoutineResponse;
import com.fitapp.backend.routinecomplete.routine.aplication.dto.response.RoutineStatisticsResponse;
import com.fitapp.backend.routinecomplete.routine.aplication.dto.response.RoutineSummaryResponse;
import com.fitapp.backend.routinecomplete.routine.aplication.port.input.RoutineUseCase;
import com.fitapp.backend.routinecomplete.routine.aplication.port.output.RoutinePersistencePort;
import com.fitapp.backend.routinecomplete.routine.domain.model.RoutineModel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoutineServiceImpl implements RoutineUseCase {

    private final RoutinePersistencePort routinePersistencePort;
    private final UserPersistencePort userPersistencePort;
    private final ExercisePersistencePort exercisePersistencePort;
    private final SportPersistencePort sportPersistencePort;
    private final CustomParameterPersistencePort customParameterPersistencePort;
    private final RoutineServiceLogger serviceLogger;
    private final SubscriptionLimitChecker limitChecker;

    @Override
    @Caching(evict = {
            @CacheEvict(value = "routines", allEntries = true),
            @CacheEvict(value = "userRoutines", allEntries = true),
            @CacheEvict(value = "recentRoutines", allEntries = true),
            @CacheEvict(value = "routineStats", allEntries = true)
    })
    public RoutineResponse createRoutine(CreateRoutineRequest request, String userEmail) {
        log.info("CREATE_ROUTINE | user={} | name={}", userEmail, request.getName());
        serviceLogger.logRoutineCreationStart(userEmail, request.getName());

        if (request.getTrainingDays() == null || request.getTrainingDays().isEmpty()) {
            throw new IllegalArgumentException("Training days are required");
        }

        Set<DayOfWeek> trainingDays = request.getTrainingDays().stream()
                .map(String::toUpperCase)
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toSet());

        UserModel user = findUser(userEmail);

        long currentCount = routinePersistencePort.countByUserId(user.getId());
        limitChecker.checkRoutineLimit(userEmail, currentCount);

        RoutineModel routine = new RoutineModel();
        routine.setName(request.getName());
        routine.setDescription(request.getDescription());
        routine.setUserId(user.getId());
        routine.setSportId(request.getSportId());
        routine.setIsActive(true);
        routine.setExercises(new ArrayList<>());
        routine.setTrainingDays(trainingDays);
        routine.setGoal(request.getGoal());
        routine.setSessionsPerWeek(request.getSessionsPerWeek());
        routine.setLastUsedAt(null);

        // V2 fields
        routine.setOriginalRoutineId(request.getOriginalRoutineId());
        routine.setVersion(request.getVersion());
        routine.setPackageId(request.getPackageId());
        routine.setTimesPurchased(0);
        // exportKey se genera automáticamente en @PrePersist

        RoutineModel saved = routinePersistencePort.save(routine);
        log.info("CREATE_ROUTINE_OK | routineId={} | user={} | originalRoutineId={}",
                saved.getId(), userEmail, saved.getOriginalRoutineId());
        serviceLogger.logRoutineCreationSuccess(saved.getId(), userEmail);

        SportModel sport = loadSport(saved.getSportId());
        return mapToResponse(saved, sport);
    }

    @Override
    public RoutineResponse getRoutineForTraining(Long id, String userEmail) {
        log.info("GET_ROUTINE_TRAINING | routineId={} | user={}", id, userEmail);

        UserModel user = findUser(userEmail);
        RoutineModel routine = routinePersistencePort
                .findFullRoutineByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RoutineNotFoundException(id));

        SportModel sport = loadSport(routine.getSportId());

        RoutineResponse response = mapToResponseBatch(routine, sport);

        routinePersistencePort.updateLastUsedAt(id, user.getId(), LocalDateTime.now());
        log.info("GET_ROUTINE_TRAINING_OK | routineId={}", id);
        return response;
    }

    @Override
    @Cacheable(value = "routines", key = "#id + '_' + #userEmail")
    public RoutineResponse getRoutineById(Long id, String userEmail) {
        log.debug("GET_ROUTINE | routineId={} | user={}", id, userEmail);

        UserModel user = findUser(userEmail);
        RoutineModel routine = routinePersistencePort
                .findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RoutineNotFoundException(id));

        SportModel sport = loadSport(routine.getSportId());
        return mapToResponse(routine, sport);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "routines", allEntries = true),
            @CacheEvict(value = "userRoutines", allEntries = true),
            @CacheEvict(value = "recentRoutines", allEntries = true),
            @CacheEvict(value = "routineStats", allEntries = true),
            @CacheEvict(value = "activeRoutines", allEntries = true)
    })
    public RoutineResponse updateRoutine(Long id, UpdateRoutineRequest request, String userEmail) {
        log.info("UPDATE_ROUTINE | routineId={} | user={}", id, userEmail);
        serviceLogger.logRoutineUpdateStart(id, userEmail);

        UserModel user = findUser(userEmail);
        RoutineModel existing = routinePersistencePort
                .findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RoutineNotFoundException(id));

        if (request.getName() != null)
            existing.setName(request.getName());
        if (request.getDescription() != null)
            existing.setDescription(request.getDescription());
        if (request.getGoal() != null)
            existing.setGoal(request.getGoal());
        if (request.getSessionsPerWeek() != null)
            existing.setSessionsPerWeek(request.getSessionsPerWeek());
        if (request.getIsActive() != null)
            existing.setIsActive(request.getIsActive());
        if (request.getSportId() != null)
            existing.setSportId(request.getSportId());
        if (request.getVersion() != null)
            existing.setVersion(request.getVersion());

        if (request.getTrainingDays() != null && !request.getTrainingDays().isEmpty()) {
            Set<DayOfWeek> days = request.getTrainingDays().stream()
                    .map(String::toUpperCase)
                    .map(DayOfWeek::valueOf)
                    .collect(Collectors.toSet());
            existing.setTrainingDays(days);
        }

        if (request.getSportId() == null && request.hasSportIdExplicit()) {
            existing.setSportId(null);
        }

        RoutineModel updated = routinePersistencePort.update(existing);
        SportModel sport = loadSport(updated.getSportId());

        serviceLogger.logRoutineUpdateSuccess(updated.getId(), userEmail);
        log.info("UPDATE_ROUTINE_OK | routineId={}", updated.getId());
        return mapToResponse(updated, sport);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "routines", allEntries = true),
            @CacheEvict(value = "userRoutines", allEntries = true),
            @CacheEvict(value = "recentRoutines", allEntries = true),
            @CacheEvict(value = "routineStats", allEntries = true),
            @CacheEvict(value = "activeRoutines", allEntries = true),
            @CacheEvict(value = "lastUsedRoutines", allEntries = true)
    })
    public void deleteRoutine(Long id, String userEmail) {
        log.info("DELETE_ROUTINE | routineId={} | user={}", id, userEmail);
        serviceLogger.logRoutineDeletionStart(id, userEmail);

        UserModel user = findUser(userEmail);
        routinePersistencePort.deleteByIdAndUserId(id, user.getId());

        serviceLogger.logRoutineDeletionSuccess(id, userEmail);
        log.info("DELETE_ROUTINE_OK | routineId={}", id);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "routines", allEntries = true),
            @CacheEvict(value = "activeRoutines", allEntries = true),
            @CacheEvict(value = "userRoutines", allEntries = true)
    })
    public void toggleRoutineActiveStatus(Long id, boolean isActive, String userEmail) {
        log.info("TOGGLE_ACTIVE | routineId={} | active={} | user={}", id, isActive, userEmail);
        UserModel user = findUser(userEmail);
        routinePersistencePort.toggleActiveStatus(id, user.getId(), isActive);
        serviceLogger.logRoutineStatusToggle(id, isActive, userEmail);
    }

    @Override
    @Cacheable(value = "userRoutines", key = "#userEmail + '_p' + #page + '_s' + #size + '_' + #sortBy + '_' + #sortDirection")
    public PageResponse<RoutineSummaryResponse> getUserRoutines(
            String userEmail, int page, int size, String sortBy, String sortDirection) {
        log.debug("GET_USER_ROUTINES | user={} | page={}", userEmail, page);

        UserModel user = findUser(userEmail);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        Page<RoutineModel> routinePage = routinePersistencePort.findByUserId(user.getId(), pageable);

        return mapToPageResponse(routinePage);
    }

    @Override
    public PageResponse<RoutineSummaryResponse> getUserRoutinesWithFilters(
            String userEmail, RoutineFilterRequest filters, int page, int size) {
        log.debug("GET_ROUTINES_FILTERED | user={} | page={}", userEmail, page);

        UserModel user = findUser(userEmail);
        Sort sort = Sort.by(Sort.Direction.fromString(filters.getSortDirection()), filters.getSortBy());
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<RoutineModel> routinePage = routinePersistencePort.findByUserIdAndFilters(user.getId(), filters, pageable);

        return mapToPageResponse(routinePage);
    }

    @Override
    @Cacheable(value = "recentRoutines", key = "#userEmail + '_' + #limit")
    public List<RoutineSummaryResponse> getRecentRoutines(String userEmail, int limit) {
        log.debug("GET_RECENT_ROUTINES | user={} | limit={}", userEmail, limit);
        serviceLogger.logRecentRoutinesRetrievalStart(userEmail, limit);

        UserModel user = findUser(userEmail);
        List<RoutineModel> routines = routinePersistencePort.findRecentByUserId(user.getId(), limit);

        serviceLogger.logRecentRoutinesRetrievalSuccess(userEmail, routines.size());
        return mapToSummaryListBatch(routines);
    }

    @Override
    @Cacheable(value = "activeRoutines", key = "#userEmail")
    public List<RoutineSummaryResponse> getActiveRoutines(String userEmail) {
        log.debug("GET_ACTIVE_ROUTINES | user={}", userEmail);
        serviceLogger.logActiveRoutinesRetrievalStart(userEmail);

        UserModel user = findUser(userEmail);
        List<RoutineModel> routines = routinePersistencePort.findActiveRoutinesByUserId(user.getId());

        serviceLogger.logActiveRoutinesRetrievalSuccess(userEmail, routines.size());
        return mapToSummaryListBatch(routines);
    }

    @Override
    @Cacheable(value = "lastUsedRoutines", key = "#userEmail + '_' + #limit")
    public List<RoutineSummaryResponse> getLastUsedRoutines(String userEmail, int limit) {
        log.debug("GET_LAST_USED | user={} | limit={}", userEmail, limit);
        UserModel user = findUser(userEmail);
        List<RoutineModel> routines = routinePersistencePort.findLastUsedByUserId(user.getId(), limit);
        return mapToSummaryListBatch(routines);
    }

    @Override
    @Cacheable(value = "routineStats", key = "#userEmail")
    public RoutineStatisticsResponse getRoutineStatistics(String userEmail) {
        log.debug("GET_STATS | user={}", userEmail);
        serviceLogger.logRoutineStatisticsRetrievalStart(userEmail);

        UserModel user = findUser(userEmail);
        long total = routinePersistencePort.countByUserId(user.getId());
        long active = routinePersistencePort.findActiveRoutinesByUserId(user.getId()).size();

        serviceLogger.logRoutineStatisticsRetrievalSuccess(userEmail);
        return RoutineStatisticsResponse.builder()
                .totalRoutines(total)
                .activeRoutines(active)
                .inactiveRoutines(total - active)
                .build();
    }

    @Override
    @Transactional
    @CacheEvict(value = "lastUsedRoutines", allEntries = true)
    public void markRoutineAsUsed(Long id, String userEmail) {
        log.debug("MARK_USED | routineId={} | user={}", id, userEmail);
        UserModel user = findUser(userEmail);
        routinePersistencePort.updateLastUsedAt(id, user.getId(), LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public RoutineResponse getRoutineByExportKey(UUID exportKey) {
        log.info("GET_ROUTINE_BY_EXPORT_KEY | exportKey={}", exportKey);

        RoutineModel routine = routinePersistencePort
                .findByExportKey(exportKey)
                .orElseThrow(() -> new RoutineNotFoundException("Routine not found with export key: " + exportKey));

        SportModel sport = loadSport(routine.getSportId());
        return mapToResponse(routine, sport);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "routines", allEntries = true),
            @CacheEvict(value = "userRoutines", allEntries = true),
            @CacheEvict(value = "recentRoutines", allEntries = true),
            @CacheEvict(value = "routineStats", allEntries = true)
    })
    public RoutineResponse importRoutineFromExportKey(UUID exportKey, String userEmail) {
        log.info("IMPORT_ROUTINE | exportKey={} | user={}", exportKey, userEmail);

        UserModel user = findUser(userEmail);

        // Validar límite de suscripción
        long currentCount = routinePersistencePort.countByUserId(user.getId());
        limitChecker.checkRoutineLimit(userEmail, currentCount);

        // Obtener rutina original
        RoutineModel originalRoutine = routinePersistencePort
                .findFullByExportKey(exportKey)
                .orElseThrow(() -> new RoutineNotFoundException("Routine not found with export key: " + exportKey));

        // Crear copia
        RoutineModel newRoutine = new RoutineModel();
        newRoutine.setName(originalRoutine.getName() + " (importada)");
        newRoutine.setDescription(originalRoutine.getDescription());
        newRoutine.setUserId(user.getId());
        newRoutine.setSportId(originalRoutine.getSportId());
        newRoutine.setIsActive(true);
        newRoutine.setTrainingDays(originalRoutine.getTrainingDays());
        newRoutine.setGoal(originalRoutine.getGoal());
        newRoutine.setSessionsPerWeek(originalRoutine.getSessionsPerWeek());

        // V2: marcar origen
        newRoutine.setOriginalRoutineId(originalRoutine.getId());
        newRoutine.setVersion(originalRoutine.getVersion());
        newRoutine.setPackageId(originalRoutine.getPackageId());
        newRoutine.setTimesPurchased(0);

        // Copiar estructura de ejercicios (deep copy)
        newRoutine.setExercises(deepCopyExercises(originalRoutine.getExercises()));

        RoutineModel saved = routinePersistencePort.save(newRoutine);

        // Incrementar contador de compras/importaciones en la rutina original
        routinePersistencePort.incrementPurchaseCount(originalRoutine.getId());

        log.info("IMPORT_ROUTINE_OK | newRoutineId={} | originalRoutineId={} | user={}",
                saved.getId(), originalRoutine.getId(), userEmail);

        SportModel sport = loadSport(saved.getSportId());
        return mapToResponse(saved, sport);
    }

    @Override
    @Transactional
    public void registerRoutinePurchase(Long routineId) {
        log.info("REGISTER_PURCHASE | routineId={}", routineId);
        routinePersistencePort.incrementPurchaseCount(routineId);
    }

    private List<RoutineExerciseModel> deepCopyExercises(List<RoutineExerciseModel> original) {
        if (original == null || original.isEmpty())
            return new ArrayList<>();

        return original.stream()
                .map(this::deepCopyExercise)
                .collect(Collectors.toList());
    }

    private RoutineExerciseModel deepCopyExercise(RoutineExerciseModel original) {
        RoutineExerciseModel copy = new RoutineExerciseModel();
        copy.setExerciseId(original.getExerciseId());
        copy.setPosition(original.getPosition());
        copy.setSessionNumber(original.getSessionNumber());
        copy.setDayOfWeek(original.getDayOfWeek());
        copy.setSessionOrder(original.getSessionOrder());
        copy.setRestAfterExercise(original.getRestAfterExercise());

        // Copiar target parameters
        if (original.getTargetParameters() != null) {
            copy.setTargetParameters(
                    original.getTargetParameters().stream()
                            .map(this::deepCopyExerciseParameter)
                            .collect(Collectors.toList()));
        }

        // Copiar sets
        if (original.getSets() != null) {
            copy.setSets(
                    original.getSets().stream()
                            .map(this::deepCopySetTemplate)
                            .collect(Collectors.toList()));
        }

        return copy;
    }

    private RoutineExerciseParameterModel deepCopyExerciseParameter(RoutineExerciseParameterModel original) {
        RoutineExerciseParameterModel copy = new RoutineExerciseParameterModel();
        copy.setParameterId(original.getParameterId());
        copy.setNumericValue(original.getNumericValue());
        copy.setIntegerValue(original.getIntegerValue());
        copy.setDurationValue(original.getDurationValue());
        copy.setStringValue(original.getStringValue());
        copy.setMinValue(original.getMinValue());
        copy.setMaxValue(original.getMaxValue());
        copy.setDefaultValue(original.getDefaultValue());
        return copy;
    }

    private RoutineSetTemplateModel deepCopySetTemplate(RoutineSetTemplateModel original) {
        RoutineSetTemplateModel copy = new RoutineSetTemplateModel();
        copy.setPosition(original.getPosition());
        copy.setSubSetNumber(original.getSubSetNumber());
        copy.setGroupId(original.getGroupId());
        copy.setSetType(original.getSetType());
        copy.setRestAfterSet(original.getRestAfterSet());

        if (original.getParameters() != null) {
            copy.setParameters(
                    original.getParameters().stream()
                            .map(this::deepCopySetParameter)
                            .collect(Collectors.toList()));
        }

        return copy;
    }

    private RoutineSetParameterModel deepCopySetParameter(RoutineSetParameterModel original) {
        RoutineSetParameterModel copy = new RoutineSetParameterModel();
        copy.setParameterId(original.getParameterId());
        copy.setNumericValue(original.getNumericValue());
        copy.setDurationValue(original.getDurationValue());
        copy.setIntegerValue(original.getIntegerValue());
        copy.setRepetitions(original.getRepetitions());
        return copy;
    }

    private List<RoutineSummaryResponse> mapToSummaryListBatch(List<RoutineModel> routines) {
        Set<Long> sportIds = routines.stream()
                .filter(r -> r.getSportId() != null)
                .map(RoutineModel::getSportId)
                .collect(Collectors.toSet());

        Map<Long, SportModel> sportMap = sportIds.stream()
                .map(id -> sportPersistencePort.findById(id).orElse(null))
                .filter(s -> s != null)
                .collect(Collectors.toMap(SportModel::getId, Function.identity()));

        return routines.stream()
                .map(routine -> mapToSummaryResponse(routine, sportMap.get(routine.getSportId())))
                .collect(Collectors.toList());
    }

    private RoutineResponse mapToResponseBatch(RoutineModel routine, SportModel sport) {
        if (routine.getExercises() == null || routine.getExercises().isEmpty()) {
            return mapToResponse(routine, sport);
        }

        Set<Long> exerciseIds = routine.getExercises().stream()
                .map(e -> e.getExerciseId())
                .collect(Collectors.toSet());

        Map<Long, ExerciseModel> exerciseMap = exerciseIds.stream()
                .map(id -> exercisePersistencePort.findById(id).orElse(null))
                .filter(e -> e != null)
                .collect(Collectors.toMap(ExerciseModel::getId, Function.identity()));

        Set<Long> paramIds = new java.util.HashSet<>();
        routine.getExercises().forEach(ex -> {
            if (ex.getTargetParameters() != null)
                ex.getTargetParameters().forEach(p -> paramIds.add(p.getParameterId()));
            if (ex.getSets() != null)
                ex.getSets().forEach(s -> {
                    if (s.getParameters() != null)
                        s.getParameters().forEach(p -> paramIds.add(p.getParameterId()));
                });
        });

        Map<Long, CustomParameterModel> paramMap = paramIds.stream()
                .map(id -> customParameterPersistencePort.findById(id).orElse(null))
                .filter(p -> p != null)
                .collect(Collectors.toMap(CustomParameterModel::getId, Function.identity()));

        RoutineResponse response = buildBaseResponse(routine, sport);

        List<RoutineExerciseResponse> exerciseResponses = routine.getExercises().stream()
                .map(exercise -> {
                    ExerciseModel exerciseModel = exerciseMap.get(exercise.getExerciseId());
                    return RoutineExerciseResponse.builder()
                            .id(exercise.getId())
                            .exerciseId(exercise.getExerciseId())
                            .exerciseName(exerciseModel != null ? exerciseModel.getName() : null)
                            .position(exercise.getPosition())
                            .sessionNumber(exercise.getSessionNumber())
                            .dayOfWeek(exercise.getDayOfWeek())
                            .sessionOrder(exercise.getSessionOrder())
                            .restAfterExercise(exercise.getRestAfterExercise())
                            .sets(exercise.getSets() != null ? exercise.getSets().size() : 0)
                            .targetParameters(mapParamsFromCache(exercise.getTargetParameters(), paramMap))
                            .setsTemplate(mapSetsFromCache(exercise.getSets(), paramMap))
                            .build();
                })
                .sorted(Comparator.comparing(RoutineExerciseResponse::getPosition))
                .collect(Collectors.toList());

        response.setExercises(exerciseResponses);
        return response;
    }

    private RoutineResponse mapToResponse(RoutineModel routine, SportModel sport) {
        RoutineResponse response = buildBaseResponse(routine, sport);

        if (routine.getExercises() != null && !routine.getExercises().isEmpty()) {
            List<RoutineExerciseResponse> exerciseResponses = routine.getExercises().stream()
                    .map(exercise -> {
                        ExerciseModel exerciseModel = exercisePersistencePort
                                .findById(exercise.getExerciseId()).orElse(null);
                        return RoutineExerciseResponse.builder()
                                .id(exercise.getId())
                                .exerciseId(exercise.getExerciseId())
                                .exerciseName(exerciseModel != null ? exerciseModel.getName() : null)
                                .position(exercise.getPosition())
                                .sessionNumber(exercise.getSessionNumber())
                                .dayOfWeek(exercise.getDayOfWeek())
                                .sessionOrder(exercise.getSessionOrder())
                                .restAfterExercise(exercise.getRestAfterExercise())
                                .sets(exercise.getSets() != null ? exercise.getSets().size() : 0)
                                .targetParameters(mapToParameterResponses(exercise.getTargetParameters()))
                                .setsTemplate(mapToSetTemplateResponses(exercise.getSets()))
                                .build();
                    })
                    .sorted(Comparator.comparing(RoutineExerciseResponse::getPosition))
                    .collect(Collectors.toList());
            response.setExercises(exerciseResponses);
        } else {
            response.setExercises(new ArrayList<>());
        }

        return response;
    }

    private RoutineResponse buildBaseResponse(RoutineModel routine, SportModel sport) {
        RoutineResponse response = new RoutineResponse();
        response.setId(routine.getId());
        response.setName(routine.getName());
        response.setDescription(routine.getDescription());
        response.setSportId(routine.getSportId());
        response.setSportName(sport != null ? sport.getName() : null);
        response.setIsActive(routine.getIsActive());
        response.setCreatedAt(routine.getCreatedAt());
        response.setUpdatedAt(routine.getUpdatedAt());
        response.setLastUsedAt(routine.getLastUsedAt());
        response.setTrainingDays(routine.getTrainingDays());
        response.setGoal(routine.getGoal());
        response.setSessionsPerWeek(routine.getSessionsPerWeek());
        response.setOriginalRoutineId(routine.getOriginalRoutineId());
        response.setVersion(routine.getVersion());
        response.setPackageId(routine.getPackageId());
        response.setExportKey(routine.getExportKey());
        response.setTimesPurchased(routine.getTimesPurchased());

        return response;
    }

    private RoutineSummaryResponse mapToSummaryResponse(RoutineModel routine, SportModel sport) {
        int exerciseCount = routine.getExercises() != null ? routine.getExercises().size() : 0;
        return RoutineSummaryResponse.builder()
                .id(routine.getId())
                .name(routine.getName())
                .description(routine.getDescription())
                .sportId(routine.getSportId())
                .sportName(sport != null ? sport.getName() : null)
                .isActive(routine.getIsActive())
                .createdAt(routine.getCreatedAt())
                .updatedAt(routine.getUpdatedAt())
                .lastUsedAt(routine.getLastUsedAt())
                .trainingDays(routine.getTrainingDays())
                .goal(routine.getGoal())
                .sessionsPerWeek(routine.getSessionsPerWeek())
                .exerciseCount(exerciseCount)
                // V2 fields
                .originalRoutineId(routine.getOriginalRoutineId())
                .version(routine.getVersion())
                .packageId(routine.getPackageId())
                .exportKey(routine.getExportKey())
                .timesPurchased(routine.getTimesPurchased())
                .build();
    }

    private PageResponse<RoutineSummaryResponse> mapToPageResponse(Page<RoutineModel> page) {
        List<RoutineSummaryResponse> content = mapToSummaryListBatch(page.getContent());
        return PageResponse.<RoutineSummaryResponse>builder()
                .content(content)
                .page(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    private List<RoutineExerciseParameterResponse> mapParamsFromCache(
            List<RoutineExerciseParameterModel> parameters, Map<Long, CustomParameterModel> paramMap) {
        if (parameters == null || parameters.isEmpty())
            return new ArrayList<>();
        return parameters.stream()
                .map(param -> {
                    CustomParameterModel model = paramMap.get(param.getParameterId());
                    return RoutineExerciseParameterResponse.builder()
                            .id(param.getId())
                            .parameterId(param.getParameterId())
                            .parameterName(model != null ? model.getName() : null)
                            .parameterType(model != null && model.getParameterType() != null
                                    ? model.getParameterType().name()
                                    : null)
                            .numericValue(param.getNumericValue())
                            .integerValue(param.getIntegerValue())
                            .durationValue(param.getDurationValue())
                            .stringValue(param.getStringValue())
                            .minValue(param.getMinValue())
                            .maxValue(param.getMaxValue())
                            .defaultValue(param.getDefaultValue())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<RoutineSetTemplateResponse> mapSetsFromCache(
            List<RoutineSetTemplateModel> sets, Map<Long, CustomParameterModel> paramMap) {
        if (sets == null || sets.isEmpty())
            return new ArrayList<>();
        return sets.stream()
                .sorted(Comparator.comparing(RoutineSetTemplateModel::getPosition))
                .map(set -> RoutineSetTemplateResponse.builder()
                        .id(set.getId())
                        .position(set.getPosition())
                        .subSetNumber(set.getSubSetNumber())
                        .groupId(set.getGroupId())
                        .setType(set.getSetType())
                        .restAfterSet(set.getRestAfterSet())
                        .parameters(mapSetParamsFromCache(set.getParameters(), paramMap))
                        .build())
                .collect(Collectors.toList());
    }

    private List<RoutineSetParameterResponse> mapSetParamsFromCache(
            List<RoutineSetParameterModel> parameters, Map<Long, CustomParameterModel> paramMap) {
        if (parameters == null || parameters.isEmpty())
            return new ArrayList<>();
        return parameters.stream()
                .map(param -> {
                    CustomParameterModel model = paramMap.get(param.getParameterId());
                    return RoutineSetParameterResponse.builder()
                            .id(param.getId())
                            .parameterId(param.getParameterId())
                            .parameterName(model != null ? model.getName() : null)
                            .parameterType(model != null && model.getParameterType() != null
                                    ? model.getParameterType().name()
                                    : null)
                            .numericValue(param.getNumericValue())
                            .durationValue(param.getDurationValue())
                            .integerValue(param.getIntegerValue())
                            .repetitions(param.getRepetitions())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<RoutineExerciseParameterResponse> mapToParameterResponses(
            List<RoutineExerciseParameterModel> parameters) {
        if (parameters == null || parameters.isEmpty())
            return new ArrayList<>();
        return parameters.stream()
                .map(param -> {
                    CustomParameterModel model = customParameterPersistencePort
                            .findById(param.getParameterId()).orElse(null);
                    return RoutineExerciseParameterResponse.builder()
                            .id(param.getId())
                            .parameterId(param.getParameterId())
                            .parameterName(model != null ? model.getName() : null)
                            .parameterType(model != null && model.getParameterType() != null
                                    ? model.getParameterType().name()
                                    : null)
                            .numericValue(param.getNumericValue())
                            .integerValue(param.getIntegerValue())
                            .durationValue(param.getDurationValue())
                            .stringValue(param.getStringValue())
                            .minValue(param.getMinValue())
                            .maxValue(param.getMaxValue())
                            .defaultValue(param.getDefaultValue())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<RoutineSetTemplateResponse> mapToSetTemplateResponses(List<RoutineSetTemplateModel> sets) {
        if (sets == null || sets.isEmpty())
            return new ArrayList<>();
        return sets.stream()
                .sorted(Comparator.comparing(RoutineSetTemplateModel::getPosition))
                .map(set -> RoutineSetTemplateResponse.builder()
                        .id(set.getId())
                        .position(set.getPosition())
                        .subSetNumber(set.getSubSetNumber())
                        .groupId(set.getGroupId())
                        .setType(set.getSetType())
                        .restAfterSet(set.getRestAfterSet())
                        .parameters(mapToSetParameterResponses(set.getParameters()))
                        .build())
                .collect(Collectors.toList());
    }

    private List<RoutineSetParameterResponse> mapToSetParameterResponses(List<RoutineSetParameterModel> parameters) {
        if (parameters == null || parameters.isEmpty())
            return new ArrayList<>();
        return parameters.stream()
                .map(param -> {
                    CustomParameterModel model = customParameterPersistencePort
                            .findById(param.getParameterId()).orElse(null);
                    return RoutineSetParameterResponse.builder()
                            .id(param.getId())
                            .parameterId(param.getParameterId())
                            .parameterName(model != null ? model.getName() : null)
                            .parameterType(model != null && model.getParameterType() != null
                                    ? model.getParameterType().name()
                                    : null)
                            .numericValue(param.getNumericValue())
                            .durationValue(param.getDurationValue())
                            .integerValue(param.getIntegerValue())
                            .repetitions(param.getRepetitions())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private UserModel findUser(String email) {
        return userPersistencePort.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    private SportModel loadSport(Long sportId) {
        if (sportId == null)
            return null;
        return sportPersistencePort.findById(sportId).orElse(null);
    }
}