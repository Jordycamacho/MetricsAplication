package com.fitapp.backend.application.service;

import com.fitapp.backend.application.dto.exercise.AddExercisesToRoutineRequest;
import com.fitapp.backend.application.dto.page.PageResponse;
import com.fitapp.backend.application.dto.routine.request.CreateRoutineRequest;
import com.fitapp.backend.application.dto.routine.request.RoutineFilterRequest;
import com.fitapp.backend.application.dto.routine.request.UpdateRoutineRequest;
import com.fitapp.backend.application.dto.routine.response.RoutineExerciseResponse;
import com.fitapp.backend.application.dto.routine.response.RoutineResponse;
import com.fitapp.backend.application.dto.routine.response.RoutineStatisticsResponse;
import com.fitapp.backend.application.dto.routine.response.RoutineSummaryResponse;
import com.fitapp.backend.application.logging.RoutineServiceLogger;
import com.fitapp.backend.application.ports.input.RoutineUseCase;
import com.fitapp.backend.application.ports.output.ExercisePersistencePort;
import com.fitapp.backend.application.ports.output.RoutinePersistencePort;
import com.fitapp.backend.application.ports.output.SportPersistencePort;
import com.fitapp.backend.application.ports.output.UserPersistencePort;
import com.fitapp.backend.domain.exception.ExerciseNotFoundException;
import com.fitapp.backend.domain.exception.RoutineNotFoundException;
import com.fitapp.backend.domain.model.ExerciseModel;
import com.fitapp.backend.domain.model.RoutineExerciseModel;
import com.fitapp.backend.domain.model.RoutineModel;
import com.fitapp.backend.domain.model.SportModel;
import com.fitapp.backend.domain.model.UserModel;
import com.fitapp.backend.infrastructure.persistence.entity.enums.DayOfWeek;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoutineServiceImpl implements RoutineUseCase {
        private final RoutinePersistencePort routinePersistencePort;
        private final UserPersistencePort userPersistencePort;
        private final ExercisePersistencePort exercisePersistencePort;
        private final SportPersistencePort sportPersistencePort;
        private final RoutineServiceLogger serviceLogger;

        @Override
        @CacheEvict(value = { "routines", "userRoutines" }, allEntries = true)
        public RoutineResponse createRoutine(CreateRoutineRequest request, String userEmail) {

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

                RoutineModel savedRoutine = routinePersistencePort.save(routine);
                serviceLogger.logRoutineCreationSuccess(routine.getId(), userEmail);
                return mapToResponse(savedRoutine, sport);
        }

        @Override
        @Cacheable(value = "routines", key = "#id + '_' + #userEmail")
        public RoutineResponse getRoutineById(Long id, String userEmail) {
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
        @CacheEvict(value = { "routines", "userRoutines" }, key = "#request.routineId")
        public RoutineResponse addExercisesToRoutine(AddExercisesToRoutineRequest request, String userEmail) {
                serviceLogger.logExercisesAdditionStart(null, userEmail, request.getExercises().size());
                UserModel user = userPersistencePort.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                RoutineModel routine = routinePersistencePort.findByIdAndUserId(request.getRoutineId(), user.getId())
                                .orElseThrow(() -> new RoutineNotFoundException(request.getRoutineId()));

                SportModel sport = null;
                if (routine.getSportId() != null) {
                        sport = sportPersistencePort.findById(routine.getSportId())
                                        .orElse(null);
                }

                List<RoutineExerciseModel> exercises = request.getExercises().stream()
                                .map(exerciseRequest -> {
                                        ExerciseModel exercise = exercisePersistencePort
                                                        .findById(exerciseRequest.getExerciseId())
                                                        .orElseThrow(() -> new ExerciseNotFoundException(
                                                                        exerciseRequest.getExerciseId()));

                                        RoutineExerciseModel routineExercise = new RoutineExerciseModel();
                                        routineExercise.setExerciseId(exercise.getId());
                                        routineExercise.setSets(exerciseRequest.getSets());
                                        routineExercise.setTargetReps(
                                                        exerciseRequest.getTargetReps() != null
                                                                        ? exerciseRequest.getTargetReps().toString()
                                                                        : null);
                                        routineExercise.setTargetWeight(exerciseRequest.getTargetWeight());
                                        routineExercise.setRestIntervalSeconds(
                                                        exerciseRequest.getRestIntervalSeconds());
                                        routineExercise.setId(routine.getId());

                                        return routineExercise;
                                })
                                .collect(Collectors.toList());

                routine.getExercises().addAll(exercises);

                RoutineModel updatedRoutine = routinePersistencePort.save(routine);
                serviceLogger.logExercisesAdditionSuccess(updatedRoutine.getId(), userEmail, exercises.size());
                return mapToResponse(updatedRoutine, sport);
        }

        @Override
        @Transactional
        @CacheEvict(value = { "routines", "userRoutines", "routineStats" }, key = "#userEmail")
        public RoutineResponse updateRoutine(Long id, UpdateRoutineRequest request, String userEmail) {

                serviceLogger.logRoutineUpdateStart(id, userEmail);

                UserModel user = userPersistencePort.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                RoutineModel existingRoutine = routinePersistencePort.findByIdAndUserId(id, user.getId())
                                .orElseThrow(() -> new RoutineNotFoundException(id));

                if (request.getName() != null)
                        existingRoutine.setName(request.getName());
                if (request.getDescription() != null)
                        existingRoutine.setDescription(request.getDescription());
                if (request.getGoal() != null)
                        existingRoutine.setGoal(request.getGoal());
                if (request.getSessionsPerWeek() != null)
                        existingRoutine.setSessionsPerWeek(request.getSessionsPerWeek());
                if (request.getIsActive() != null)
                        existingRoutine.setIsActive(request.getIsActive());

                if (request.getTrainingDays() != null && !request.getTrainingDays().isEmpty()) {
                        Set<DayOfWeek> trainingDays = request.getTrainingDays().stream()
                                        .map(String::toUpperCase)
                                        .map(DayOfWeek::valueOf)
                                        .collect(Collectors.toSet());
                        existingRoutine.setTrainingDays(trainingDays);
                }

                if (request.getSportId() != null) {
                        SportModel sport = sportPersistencePort.findById(request.getSportId())
                                        .orElseThrow(() -> new RuntimeException("Sport not found"));
                        existingRoutine.setSportId(sport.getId());
                }

                RoutineModel updatedRoutine = routinePersistencePort.update(existingRoutine);

                SportModel sport = null;
                if (updatedRoutine.getSportId() != null) {
                        sport = sportPersistencePort.findById(updatedRoutine.getSportId()).orElse(null);
                }

                serviceLogger.logRoutineUpdateSuccess(updatedRoutine.getId(), userEmail);
                return mapToResponse(updatedRoutine, sport);
        }

        @Override
        @Transactional
        @CacheEvict(value = { "routines", "userRoutines", "routineStats" }, key = "#userEmail")
        public void deleteRoutine(Long id, String userEmail) {
                serviceLogger.logRoutineDeletionStart(id, userEmail);
                UserModel user = userPersistencePort.findByEmail(userEmail)
                                .orElseThrow(() -> {
                                        serviceLogger.logRoutineDeletionError(id, userEmail, userEmail);
                                        return new RuntimeException("User not found");
                                });
                routinePersistencePort.deleteByIdAndUserId(id, user.getId());
                serviceLogger.logRoutineDeletionSuccess(id, userEmail);
        }

        @Override
        @Transactional
        @CacheEvict(value = { "routines", "userRoutines", "routineStats" }, key = "#userEmail")
        public void toggleRoutineActiveStatus(Long id, boolean isActive, String userEmail) {
                UserModel user = userPersistencePort.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                routinePersistencePort.toggleActiveStatus(id, user.getId(), isActive);
                serviceLogger.logRoutineStatusToggle(id, isActive, userEmail);
        }

        @Override
        @Cacheable(value = "userRoutines", key = "#userEmail + '_' + #page + '_' + #size + '_' + #sortBy + '_' + #sortDirection")
        public PageResponse<RoutineSummaryResponse> getUserRoutines(String userEmail, int page, int size, String sortBy,
                        String sortDirection) {
                UserModel user = userPersistencePort.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Pageable pageable = PageRequest.of(page, size,
                                Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
                Page<RoutineModel> routinePage = routinePersistencePort.findByUserId(user.getId(), pageable);

                return mapToPageResponse(routinePage);
        }

        @Override
        @Cacheable(value = "userRoutinesFiltered", key = "#userEmail + '_' + #filters.hashCode() + '_' + #page + '_' + #size")
        public PageResponse<RoutineSummaryResponse> getUserRoutinesWithFilters(String userEmail,
                        RoutineFilterRequest filters, int page, int size) {
            
                UserModel user = userPersistencePort.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Pageable pageable = PageRequest.of(page, size);
                Page<RoutineModel> routinePage = routinePersistencePort.findByUserIdAndFilters(user.getId(), filters,
                                pageable);

                return mapToPageResponse(routinePage);
        }

        @Override
        @Cacheable(value = "recentRoutines", key = "#userEmail + '_' + #limit")
        public List<RoutineSummaryResponse> getRecentRoutines(String userEmail, int limit) {
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
                serviceLogger.logRoutineStatisticsRetrievalStart(userEmail);
                UserModel user = userPersistencePort.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                long totalRoutines = routinePersistencePort.countByUserId(user.getId());
                List<RoutineModel> activeRoutines = routinePersistencePort.findActiveRoutinesByUserId(user.getId());

                serviceLogger.logRoutineStatisticsRetrievalSuccess(userEmail);
                return RoutineStatisticsResponse.builder()
                                .totalRoutines(totalRoutines)
                                .activeRoutines(activeRoutines.size())
                                .inactiveRoutines(totalRoutines - activeRoutines.size())
                                .build();
        }

        private RoutineResponse mapToResponse(RoutineModel routine, SportModel sport) {
                RoutineResponse response = new RoutineResponse();
                response.setId(routine.getId());
                response.setName(routine.getName());
                response.setDescription(routine.getDescription());
                response.setSportId(routine.getSportId());
                response.setSportName(sport != null ? sport.getName() : null);
                response.setIsActive(routine.getIsActive());
                response.setCreatedAt(routine.getCreatedAt());
                response.setUpdatedAt(routine.getUpdatedAt());
                response.setTrainingDays(routine.getTrainingDays());
                response.setGoal(routine.getGoal());
                response.setSessionsPerWeek(routine.getSessionsPerWeek());
                List<RoutineExerciseResponse> exerciseResponses = routine.getExercises().stream()
                                .map(exercise -> {
                                        RoutineExerciseResponse exerciseResponse = new RoutineExerciseResponse();
                                        exerciseResponse.setId(exercise.getId());
                                        exerciseResponse.setExerciseId(exercise.getExerciseId());
                                        exerciseResponse.setSets(exercise.getSets());
                                        exerciseResponse.setTargetReps(exercise.getTargetReps());
                                        exerciseResponse.setTargetWeight(exercise.getTargetWeight());
                                        exerciseResponse.setRestIntervalSeconds(exercise.getRestIntervalSeconds());
                                        return exerciseResponse;
                                })
                                .collect(Collectors.toList());

                response.setExercises(exerciseResponses);
                return response;
        }

        private PageResponse<RoutineSummaryResponse> mapToPageResponse(Page<RoutineModel> routinePage) {
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
                                .trainingDays(routine.getTrainingDays())
                                .goal(routine.getGoal())
                                .sessionsPerWeek(routine.getSessionsPerWeek())
                                .exerciseCount(exerciseCount)
                                .build();
        }

}