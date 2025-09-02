package com.fitapp.backend.application.service;

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

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
        UserModel user = userPersistencePort.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        SportModel sport = null;
        if (request.getSportId() != null) {
            sport = sportPersistencePort.findById(request.getSportId())
                    .orElseThrow(() -> new RuntimeException("Sport not found"));
        }
        
        RoutineModel routine = new RoutineModel();
        routine.setName(request.getName());
        routine.setDescription(request.getDescription());
        routine.setUserId(user.getId());
        routine.setSportId(sport != null ? sport.getId() : null);
        routine.setIsActive(true);
        
        List<RoutineExerciseModel> exercises = request.getExercises().stream()
                .map(exerciseRequest -> {
                    ExerciseModel exercise = exercisePersistencePort.findById(exerciseRequest.getExerciseId())
                            .orElseThrow(() -> new ExerciseNotFoundException(exerciseRequest.getExerciseId()));
                    
                    RoutineExerciseModel routineExercise = new RoutineExerciseModel();
                    routineExercise.setExerciseId(exercise.getId());
                    routineExercise.setSets(exerciseRequest.getSets());
                    routineExercise.setTargetReps(exerciseRequest.getTargetReps());
                    routineExercise.setTargetWeight(exerciseRequest.getTargetWeight());
                    routineExercise.setRestIntervalSeconds(exerciseRequest.getRestIntervalSeconds());
                    
                    return routineExercise;
                })
                .collect(Collectors.toList());
        
        routine.setExercises(exercises);
        routine.setEstimatedDuration(calculateEstimatedDuration(exercises));
        
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
        int totalSeconds = exercises.stream()
                .mapToInt(exercise -> {
                    int exerciseTime = exercise.getSets() * 60; // 60 segundos por serie
                    int restTime = exercise.getRestIntervalSeconds() != null ? 
                            exercise.getRestIntervalSeconds() * (exercise.getSets() - 1) : 0;
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
}