package com.fitapp.backend.application.service;

import com.fitapp.backend.application.dto.RoutineSetParameter.response.RoutineSetParameterResponse;
import com.fitapp.backend.application.dto.RoutineSetTemplate.response.RoutineSetTemplateResponse;
import com.fitapp.backend.application.dto.page.PageResponse;
import com.fitapp.backend.application.dto.routine.request.CreateRoutineRequest;
import com.fitapp.backend.application.dto.routine.request.RoutineFilterRequest;
import com.fitapp.backend.application.dto.routine.request.UpdateRoutineRequest;
import com.fitapp.backend.application.dto.routine.response.RoutineExerciseParameterResponse;
import com.fitapp.backend.application.dto.routine.response.RoutineExerciseResponse;
import com.fitapp.backend.application.dto.routine.response.RoutineResponse;
import com.fitapp.backend.application.dto.routine.response.RoutineStatisticsResponse;
import com.fitapp.backend.application.dto.routine.response.RoutineSummaryResponse;
import com.fitapp.backend.application.logging.RoutineServiceLogger;
import com.fitapp.backend.application.ports.input.RoutineUseCase;
import com.fitapp.backend.application.ports.output.CustomParameterPersistencePort;
import com.fitapp.backend.application.ports.output.ExercisePersistencePort;
import com.fitapp.backend.application.ports.output.RoutinePersistencePort;
import com.fitapp.backend.application.ports.output.SportPersistencePort;
import com.fitapp.backend.application.ports.output.UserPersistencePort;
import com.fitapp.backend.domain.exception.RoutineNotFoundException;
import com.fitapp.backend.domain.model.CustomParameterModel;
import com.fitapp.backend.domain.model.ExerciseModel;
import com.fitapp.backend.domain.model.RoutineExerciseParameterModel;
import com.fitapp.backend.domain.model.RoutineModel;
import com.fitapp.backend.domain.model.RoutineSetParameterModel;
import com.fitapp.backend.domain.model.RoutineSetTemplateModel;
import com.fitapp.backend.domain.model.SportModel;
import com.fitapp.backend.domain.model.UserModel;
import com.fitapp.backend.infrastructure.persistence.entity.enums.DayOfWeek;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
import java.util.Set;

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

        @Override
        @CacheEvict(value = { "routines", "userRoutines" }, allEntries = true)
        public RoutineResponse createRoutine(CreateRoutineRequest request, String userEmail) {
                log.info("Creando nueva rutina para usuario: {}", userEmail);
                serviceLogger.logRoutineCreationStart(userEmail, request.getName());

                if (request.getTrainingDays() == null || request.getTrainingDays().isEmpty()) {
                        throw new IllegalArgumentException("Training days are required");
                }

                Set<DayOfWeek> trainingDays = request.getTrainingDays().stream()
                                .map(String::toUpperCase)
                                .map(DayOfWeek::valueOf)
                                .collect(Collectors.toSet());

                UserModel user = userPersistencePort.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                SportModel sport = null;
                if (request.getSportId() != null) {
                        sport = sportPersistencePort.findById(request.getSportId())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Sport not found with id: " + request.getSportId()));
                }

                RoutineModel routine = new RoutineModel();
                routine.setName(request.getName());
                routine.setDescription(request.getDescription());
                routine.setUserId(user.getId());
                routine.setSportId(sport != null ? sport.getId() : null);
                routine.setIsActive(true);
                routine.setExercises(new ArrayList<>());
                routine.setTrainingDays(trainingDays);
                routine.setGoal(request.getGoal());
                routine.setSessionsPerWeek(request.getSessionsPerWeek());
                routine.setLastUsedAt(null);

                RoutineModel savedRoutine = routinePersistencePort.save(routine);
                log.info("Rutina creada exitosamente con ID: {}", savedRoutine.getId());
                serviceLogger.logRoutineCreationSuccess(savedRoutine.getId(), userEmail);
                return mapToResponse(savedRoutine, sport);
        }

        @Override
        @Cacheable(value = "routineForTraining", key = "#id + '_' + #userEmail")
        public RoutineResponse getRoutineForTraining(Long id, String userEmail) {
                log.info("Obteniendo rutina para entrenamiento: id={}, usuario={}", id, userEmail);

                UserModel user = userPersistencePort.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                RoutineModel routine = routinePersistencePort.findFullRoutineByIdAndUserId(id, user.getId())
                                .orElseThrow(() -> new RoutineNotFoundException(id));

                SportModel sport = null;
                if (routine.getSportId() != null) {
                        sport = sportPersistencePort.findById(routine.getSportId()).orElse(null);
                }

                RoutineResponse response = mapToResponse(routine, sport);

                routinePersistencePort.updateLastUsedAt(id, user.getId(), LocalDateTime.now());

                log.info("Rutina para entrenamiento obtenida exitosamente: id={}", id);
                return response;
        }

        @Override
        @Cacheable(value = "routines", key = "#id + '_' + #userEmail")
        public RoutineResponse getRoutineById(Long id, String userEmail) {
                log.debug("Obteniendo rutina por ID: {}, usuario: {}", id, userEmail);

                UserModel user = userPersistencePort.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                RoutineModel routine = routinePersistencePort.findByIdAndUserId(id, user.getId())
                                .orElseThrow(() -> new RoutineNotFoundException(id));

                SportModel sport = null;
                if (routine.getSportId() != null) {
                        sport = sportPersistencePort.findById(routine.getSportId())
                                        .orElse(null);
                }

                return mapToResponse(routine, sport);
        }

        @Override
        @Transactional
        @CacheEvict(value = { "routines", "userRoutines", "routineStats" }, key = "#userEmail")
        public RoutineResponse updateRoutine(Long id, UpdateRoutineRequest request, String userEmail) {
                log.info("Actualizando rutina ID: {}, usuario: {}", id, userEmail);
                serviceLogger.logRoutineUpdateStart(id, userEmail);

                UserModel user = userPersistencePort.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Long userId = user.getId();

                RoutineModel existingRoutine = routinePersistencePort.findByIdAndUserId(id, userId)
                                .orElseThrow(() -> new RoutineNotFoundException(id));

                if (request.getName() != null)
                        existingRoutine.setName(request.getName());
                if (request.getDescription() != null)
                        existingRoutine.setDescription(request.getDescription());
                if (request.getGoal() != null)
                        existingRoutine.setGoal(request.getGoal());
                if (request.getSessionsPerWeek() != null) {
                        log.debug("Estableciendo sessionsPerWeek a: {}", request.getSessionsPerWeek());
                        existingRoutine.setSessionsPerWeek(request.getSessionsPerWeek());
                }
                if (request.getIsActive() != null)
                        existingRoutine.setIsActive(request.getIsActive());

                if (request.getTrainingDays() != null && !request.getTrainingDays().isEmpty()) {
                        Set<DayOfWeek> trainingDays = request.getTrainingDays().stream()
                                        .map(String::toUpperCase)
                                        .map(DayOfWeek::valueOf)
                                        .collect(Collectors.toSet());
                        log.debug("Estableciendo trainingDays a: {}", trainingDays);
                        existingRoutine.setTrainingDays(trainingDays);
                }

                log.debug("DEBUG - Update Routine Request:");
                log.debug("  - SportId from request: {}", request.getSportId());
                log.debug("  - TrainingDays from request: {}", request.getTrainingDays());
                log.debug("  - SessionsPerWeek from request: {}", request.getSessionsPerWeek());
                log.debug("  - Goal from request: {}", request.getGoal());

                if (request.getSportId() != null) {
                        log.debug("  - Setting sportId to: {}", request.getSportId());
                        existingRoutine.setSportId(request.getSportId());
                }

                RoutineModel updatedRoutine = routinePersistencePort.update(existingRoutine);

                SportModel sport = null;
                if (updatedRoutine.getSportId() != null) {
                        sport = sportPersistencePort.findById(updatedRoutine.getSportId()).orElse(null);
                }

                serviceLogger.logRoutineUpdateSuccess(updatedRoutine.getId(), userEmail);
                log.info("Rutina actualizada exitosamente: {}", updatedRoutine.getId());
                return mapToResponse(updatedRoutine, sport);
        }

        @Override
        @Transactional
        @CacheEvict(value = { "routines", "userRoutines", "routineStats" }, key = "#userEmail")
        public void deleteRoutine(Long id, String userEmail) {
                log.info("Eliminando rutina ID: {}, usuario: {}", id, userEmail);
                serviceLogger.logRoutineDeletionStart(id, userEmail);

                UserModel user = userPersistencePort.findByEmail(userEmail)
                                .orElseThrow(() -> {
                                        serviceLogger.logRoutineDeletionError(id, userEmail, "User not found");
                                        return new RuntimeException("User not found");
                                });

                routinePersistencePort.deleteByIdAndUserId(id, user.getId());
                serviceLogger.logRoutineDeletionSuccess(id, userEmail);
                log.info("Rutina eliminada exitosamente: {}", id);
        }

        @Override
        @Transactional
        @CacheEvict(value = { "routines", "userRoutines", "routineStats" }, key = "#userEmail")
        public void toggleRoutineActiveStatus(Long id, boolean isActive, String userEmail) {
                log.info("Cambiando estado de rutina ID: {} a activo={}, usuario: {}", id, isActive, userEmail);

                UserModel user = userPersistencePort.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                routinePersistencePort.toggleActiveStatus(id, user.getId(), isActive);
                serviceLogger.logRoutineStatusToggle(id, isActive, userEmail);
                log.info("Estado de rutina cambiado exitosamente: {}", id);
        }

        @Override
        @Cacheable(value = "userRoutines", key = "#userEmail + '_' + #page + '_' + #size + '_' + #sortBy + '_' + #sortDirection")
        public PageResponse<RoutineSummaryResponse> getUserRoutines(String userEmail, int page, int size, String sortBy,
                        String sortDirection) {
                log.debug("Obteniendo rutinas del usuario: {}, página: {}, tamaño: {}", userEmail, page, size);

                UserModel user = userPersistencePort.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Pageable pageable = PageRequest.of(page, size,
                                Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
                Page<RoutineModel> routinePage = routinePersistencePort.findByUserId(user.getId(), pageable);

                return mapToPageResponse(routinePage);
        }

        @Override
        public PageResponse<RoutineSummaryResponse> getUserRoutinesWithFilters(String userEmail,
                        RoutineFilterRequest filters, int page, int size) {
                log.debug("Obteniendo rutinas con filtros para usuario: {}, página: {}", userEmail, page);

                UserModel user = userPersistencePort.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Sort sort = Sort.by(Sort.Direction.fromString(filters.getSortDirection()), filters.getSortBy());
                Pageable pageable = PageRequest.of(page, size, sort);

                Page<RoutineModel> routinePage = routinePersistencePort.findByUserIdAndFilters(
                                user.getId(), filters, pageable);

                return mapToPageResponse(routinePage);
        }

        @Override
        @Cacheable(value = "recentRoutines", key = "#userEmail + '_' + #limit")
        public List<RoutineSummaryResponse> getRecentRoutines(String userEmail, int limit) {
                log.debug("Obteniendo rutinas recientes para usuario: {}, límite: {}", userEmail, limit);
                serviceLogger.logRecentRoutinesRetrievalStart(userEmail, limit);

                UserModel user = userPersistencePort.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                List<RoutineModel> recentRoutines = routinePersistencePort.findRecentByUserId(user.getId(), limit);

                serviceLogger.logRecentRoutinesRetrievalSuccess(userEmail, recentRoutines.size());
                return recentRoutines.stream()
                                .map(this::mapToSummaryResponse)
                                .collect(Collectors.toList());
        }

        @Override
        @Cacheable(value = "activeRoutines", key = "#userEmail")
        public List<RoutineSummaryResponse> getActiveRoutines(String userEmail) {
                log.debug("Obteniendo rutinas activas para usuario: {}", userEmail);
                serviceLogger.logActiveRoutinesRetrievalStart(userEmail);

                UserModel user = userPersistencePort.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                List<RoutineModel> activeRoutines = routinePersistencePort.findActiveRoutinesByUserId(user.getId());

                serviceLogger.logActiveRoutinesRetrievalSuccess(userEmail, activeRoutines.size());
                return activeRoutines.stream()
                                .map(this::mapToSummaryResponse)
                                .collect(Collectors.toList());
        }

        @Override
        @Cacheable(value = "routineStats", key = "#userEmail")
        public RoutineStatisticsResponse getRoutineStatistics(String userEmail) {
                log.debug("Obteniendo estadísticas de rutinas para usuario: {}", userEmail);
                serviceLogger.logRoutineStatisticsRetrievalStart(userEmail);

                UserModel user = userPersistencePort.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                long totalRoutines = routinePersistencePort.countByUserId(user.getId());
                List<RoutineModel> activeRoutines = routinePersistencePort.findActiveRoutinesByUserId(user.getId());

                serviceLogger.logRoutineStatisticsRetrievalSuccess(userEmail);
                log.debug("Estadísticas obtenidas: total={}, activas={}", totalRoutines, activeRoutines.size());

                return RoutineStatisticsResponse.builder()
                                .totalRoutines(totalRoutines)
                                .activeRoutines(activeRoutines.size())
                                .inactiveRoutines(totalRoutines - activeRoutines.size())
                                .build();
        }

        private RoutineResponse mapToResponse(RoutineModel routine, SportModel sport) {
                log.debug("Mapeando RoutineModel a RoutineResponse: {}", routine.getId());

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

                if (routine.getExercises() != null) {
                        log.debug("Mapeando {} ejercicios", routine.getExercises().size());

                        List<RoutineExerciseResponse> exerciseResponses = routine.getExercises().stream()
                                        .map(exercise -> {
                                                ExerciseModel exerciseModel = exercisePersistencePort
                                                                .findById(exercise.getExerciseId())
                                                                .orElse(null);

                                                return RoutineExerciseResponse.builder()
                                                                .id(exercise.getId())
                                                                .exerciseId(exercise.getExerciseId())
                                                                .exerciseName(exerciseModel != null
                                                                                ? exerciseModel.getName()
                                                                                : null)
                                                                .position(exercise.getPosition())
                                                                .sessionNumber(exercise.getSessionNumber())
                                                                .dayOfWeek(exercise.getDayOfWeek())
                                                                .sessionOrder(exercise.getSessionOrder())
                                                                .restAfterExercise(exercise.getRestAfterExercise())
                                                                .sets(exercise.getSets() != null
                                                                                ? exercise.getSets().size()
                                                                                : 0)
                                                                .targetParameters(mapToParameterResponses(
                                                                                exercise.getTargetParameters()))
                                                                .setsTemplate(mapToSetTemplateResponses(
                                                                                exercise.getSets()))
                                                                .build();
                                        })
                                        .sorted(Comparator.comparing(RoutineExerciseResponse::getPosition))
                                        .collect(Collectors.toList());

                        response.setExercises(exerciseResponses);
                } else {
                        response.setExercises(new ArrayList<>());
                }

                log.debug("Mapeo completado para rutina: {}", routine.getId());
                return response;
        }

        private List<RoutineExerciseParameterResponse> mapToParameterResponses(
                        List<RoutineExerciseParameterModel> parameters) {
                if (parameters == null || parameters.isEmpty()) {
                        return new ArrayList<>();
                }

                log.debug("Mapeando {} parámetros", parameters.size());

                return parameters.stream()
                                .map(param -> {
                                        CustomParameterModel paramModel = customParameterPersistencePort
                                                        .findById(param.getParameterId())
                                                        .orElse(null);
                                        String parameterType = null;
                                        if (paramModel != null && paramModel.getParameterType() != null) {
                                                parameterType = paramModel.getParameterType().name();
                                        }

                                        return RoutineExerciseParameterResponse.builder()
                                                        .id(param.getId())
                                                        .parameterId(param.getParameterId())
                                                        .parameterName(paramModel != null ? paramModel.getName() : null)
                                                        .parameterType(parameterType)
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

        private List<RoutineSetTemplateResponse> mapToSetTemplateResponses(
                        List<RoutineSetTemplateModel> sets) {
                if (sets == null || sets.isEmpty()) {
                        return new ArrayList<>();
                }

                log.debug("Mapeando {} sets", sets.size());

                return sets.stream()
                                .sorted(Comparator.comparing(RoutineSetTemplateModel::getPosition))
                                .map(set -> {
                                        return RoutineSetTemplateResponse.builder()
                                                        .id(set.getId())
                                                        .position(set.getPosition())
                                                        .subSetNumber(set.getSubSetNumber())
                                                        .groupId(set.getGroupId())
                                                        .setType(set.getSetType())
                                                        .restAfterSet(set.getRestAfterSet())
                                                        .parameters(mapToSetParameterResponses(set.getParameters()))
                                                        .build();
                                })
                                .collect(Collectors.toList());
        }

        private List<RoutineSetParameterResponse> mapToSetParameterResponses(
                        List<RoutineSetParameterModel> parameters) {
                if (parameters == null || parameters.isEmpty()) {
                        return new ArrayList<>();
                }

                log.debug("Mapeando {} parámetros de set", parameters.size());

                return parameters.stream()
                                .map(param -> {
                                        CustomParameterModel paramModel = customParameterPersistencePort
                                                        .findById(param.getParameterId())
                                                        .orElse(null);
                                        String parameterType = null;
                                        if (paramModel != null && paramModel.getParameterType() != null) {
                                                parameterType = paramModel.getParameterType().name();
                                        }

                                        return RoutineSetParameterResponse.builder()
                                                        .id(param.getId())
                                                        .parameterId(param.getParameterId())
                                                        .parameterName(paramModel != null ? paramModel.getName() : null)
                                                        .parameterType(parameterType)
                                                        .numericValue(param.getNumericValue())
                                                        .durationValue(param.getDurationValue())
                                                        .integerValue(param.getIntegerValue())
                                                        .repetitions(param.getRepetitions())
                                                        .build();
                                })
                                .collect(Collectors.toList());
        }

        private PageResponse<RoutineSummaryResponse> mapToPageResponse(Page<RoutineModel> routinePage) {
                log.debug("Mapeando página de rutinas: {} elementos", routinePage.getContent().size());

                List<RoutineSummaryResponse> content = routinePage.getContent().stream()
                                .map(this::mapToSummaryResponse)
                                .collect(Collectors.toList());

                return PageResponse.<RoutineSummaryResponse>builder()
                                .content(content)
                                .pageNumber(routinePage.getNumber())
                                .pageSize(routinePage.getSize())
                                .totalElements(routinePage.getTotalElements())
                                .totalPages(routinePage.getTotalPages())
                                .first(routinePage.isFirst())
                                .last(routinePage.isLast())
                                .build();
        }

        private RoutineSummaryResponse mapToSummaryResponse(RoutineModel routine) {
                log.debug("Mapeando resumen de rutina: {}", routine.getId());

                SportModel sport = null;
                if (routine.getSportId() != null) {
                        sport = sportPersistencePort.findById(routine.getSportId()).orElse(null);
                }

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
                                .build();
        }

        @Override
        @Cacheable(value = "lastUsedRoutines", key = "#userEmail + '_' + #limit")
        public List<RoutineSummaryResponse> getLastUsedRoutines(String userEmail, int limit) {
                log.debug("Obteniendo rutinas usadas recientemente para usuario: {}, límite: {}", userEmail, limit);

                UserModel user = userPersistencePort.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                List<RoutineModel> lastUsedRoutines = routinePersistencePort.findLastUsedByUserId(user.getId(), limit);

                return lastUsedRoutines.stream()
                                .map(this::mapToSummaryResponse)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional
        public void markRoutineAsUsed(Long id, String userEmail) {
                log.debug("Marcando rutina como usada: {}, usuario: {}", id, userEmail);

                UserModel user = userPersistencePort.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                routinePersistencePort.updateLastUsedAt(id, user.getId(), LocalDateTime.now());
                log.debug("Rutina marcada como usada: {}", id);
        }
}