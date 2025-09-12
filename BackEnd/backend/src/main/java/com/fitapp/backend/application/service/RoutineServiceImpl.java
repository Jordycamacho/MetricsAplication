package com.fitapp.backend.application.service;

import com.fitapp.backend.application.dto.exercise.AddExercisesToRoutineRequest;
import com.fitapp.backend.application.dto.routine.CreateRoutineRequest;
import com.fitapp.backend.application.dto.routine.RoutineExerciseResponse;
import com.fitapp.backend.application.dto.routine.RoutineResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoutineServiceImpl implements RoutineUseCase {
        private final RoutinePersistencePort routinePersistencePort;
        private final UserPersistencePort userPersistencePort;
        private final ExercisePersistencePort exercisePersistencePort;
        private final SportPersistencePort sportPersistencePort;

        @Override
        @Transactional
        public RoutineResponse createRoutine(CreateRoutineRequest request, String userEmail) {

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
                routine.setEstimatedDuration(calculateEstimatedDuration(routine.getExercises()));
                routine.setTrainingDays(trainingDays);
                routine.setGoal(request.getGoal());
                routine.setDifficultyLevel(request.getDifficultyLevel());
                routine.setWeeksDuration(request.getWeeksDuration());
                routine.setSessionsPerWeek(request.getSessionsPerWeek());
                routine.setEquipmentNeeded(request.getEquipmentNeeded());
                
                RoutineModel savedRoutine = routinePersistencePort.save(routine);
                return mapToResponse(savedRoutine, sport);
        }

        @Override
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

        private Integer calculateEstimatedDuration(List<RoutineExerciseModel> exercises) {
                if (exercises == null || exercises.isEmpty()) {
                        return 0;
                }

                int totalSeconds = exercises.stream()
                                .mapToInt(exercise -> {
                                        int exerciseTime = exercise.getSets() * 60; // 60 segundos por serie
                                        int restTime = exercise.getRestIntervalSeconds() != null
                                                        ? exercise.getRestIntervalSeconds() * (exercise.getSets() - 1)
                                                        : 0;
                                        return exerciseTime + restTime;
                                })
                                .sum();

                return (int) Math.ceil(totalSeconds / 60.0); // Convertir a minutos
        }

        private RoutineResponse mapToResponse(RoutineModel routine, SportModel sport) {
                RoutineResponse response = new RoutineResponse();
                response.setId(routine.getId());
                response.setName(routine.getName());
                response.setDescription(routine.getDescription());
                response.setSportId(routine.getSportId());
                response.setSportName(sport != null ? sport.getName() : null);
                response.setIsActive(routine.getIsActive());
                response.setEstimatedDuration(routine.getEstimatedDuration());
                response.setCreatedAt(routine.getCreatedAt());
                response.setUpdatedAt(routine.getUpdatedAt());
                response.setTrainingDays(routine.getTrainingDays());
                response.setGoal(routine.getGoal());
                response.setDifficultyLevel(routine.getDifficultyLevel());
                response.setWeeksDuration(routine.getWeeksDuration());
                response.setSessionsPerWeek(routine.getSessionsPerWeek());
                response.setEquipmentNeeded(routine.getEquipmentNeeded());
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

        @Override
        @Transactional
        public RoutineResponse addExercisesToRoutine(AddExercisesToRoutineRequest request, String userEmail) {
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
                routine.setEstimatedDuration(calculateEstimatedDuration(routine.getExercises()));

                RoutineModel updatedRoutine = routinePersistencePort.save(routine);
                return mapToResponse(updatedRoutine, sport);
        }
}