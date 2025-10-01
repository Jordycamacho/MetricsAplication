package com.fitapp.backend.application.service;

import com.fitapp.backend.infrastructure.persistence.converter.ExerciseConverter;
import com.fitapp.backend.application.ports.output.ExercisePersistencePort;
import com.fitapp.backend.application.dto.exercise.CreateExerciseRequest;
import com.fitapp.backend.application.dto.exercise.UpdateExerciseRequest;
import com.fitapp.backend.application.ports.output.SportPersistencePort;
import com.fitapp.backend.application.ports.output.UserPersistencePort;
import com.fitapp.backend.application.dto.exercise.ExerciseResponse;
import com.fitapp.backend.application.ports.input.ExerciseUseCase;
import org.springframework.transaction.annotation.Transactional;
import com.fitapp.backend.domain.model.ExerciseModel;
import com.fitapp.backend.domain.model.SportModel;
import com.fitapp.backend.domain.model.UserModel;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExerciseServiceImpl implements ExerciseUseCase {

    private final ExercisePersistencePort exercisePersistencePort;
    private final UserPersistencePort userPersistencePort;
    private final SportPersistencePort sportPersistencePort;
    private final ExerciseConverter exerciseConverter;

    @Override
    @Transactional(readOnly = true)
    public ExerciseResponse getExerciseById(Long id, String userEmail) {
        log.info("Fetching exercise with ID: {} for user: {}", id, userEmail);

        UserModel user = userPersistencePort.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", userEmail);
                    return new RuntimeException("User not found");
                });

        ExerciseModel exercise = exercisePersistencePort.findById(id)
                .orElseThrow(() -> {
                    log.error("Exercise not found with ID: {}", id);
                    return new RuntimeException("Exercise not found");
                });

        if (!exercise.getUserId().equals(user.getId()) && !exercise.getIsPredefined()) {
            log.error("User {} not authorized to access exercise {}", userEmail, id);
            throw new RuntimeException("Not authorized to access this exercise");
        }

        return mapToResponse(exercise);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExerciseResponse> getExercisesBySport(Long sportId, String userEmail) {
        log.info("Fetching exercises for sport ID: {} for user: {}", sportId, userEmail);

        UserModel user = userPersistencePort.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ExerciseModel> exercises = exercisePersistencePort.findBySportId(sportId);

        return exercises.stream()
                .filter(exercise -> exercise.getIsPredefined() || exercise.getUserId().equals(user.getId()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ExerciseResponse createExercise(CreateExerciseRequest request, String userEmail) {
        log.info("Creating exercise for user: {}", userEmail);

        UserModel user = userPersistencePort.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SportModel sport = null;
        if (request.getSportId() != null) {
            sport = sportPersistencePort.findById(request.getSportId())
                    .orElseThrow(() -> new RuntimeException("Sport not found"));
        }

        ExerciseModel exercise = ExerciseModel.builder()
                .name(request.getName())
                .description(request.getDescription())
                .sportId(request.getSportId())
                .userId(user.getId())
                .isPredefined(false)
                .parameterTemplates(request.getParameterTemplates())
                .createdAt(LocalDateTime.now())
                .build();

        ExerciseModel savedExercise = exercisePersistencePort.save(exercise);
        log.info("Exercise created successfully with ID: {}", savedExercise.getId());

        return mapToResponse(savedExercise);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExerciseResponse> getUserExercises(String userEmail) {
        log.info("Fetching user exercises for user: {}", userEmail);

        UserModel user = userPersistencePort.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ExerciseModel> exercises = exercisePersistencePort.findByUserId(user.getId());

        return exercises.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExerciseResponse> getPredefinedExercises() {
        log.info("Fetching predefined exercises");

        List<ExerciseModel> exercises = exercisePersistencePort.findPredefinedExercises();

        return exercises.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ExerciseResponse updateExercise(Long id, UpdateExerciseRequest request, String userEmail) {
        log.info("Updating exercise with ID: {} for user: {}", id, userEmail);

        UserModel user = userPersistencePort.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ExerciseModel existingExercise = exercisePersistencePort.findById(id)
                .orElseThrow(() -> new RuntimeException("Exercise not found"));

        if (!existingExercise.getUserId().equals(user.getId())) {
            throw new RuntimeException("Not authorized to update this exercise");
        }

        if (request.getName() != null) {
            existingExercise.setName(request.getName());
        }
        if (request.getDescription() != null) {
            existingExercise.setDescription(request.getDescription());
        }
        if (request.getSportId() != null) {
            sportPersistencePort.findById(request.getSportId())
                    .orElseThrow(() -> new RuntimeException("Sport not found"));
            existingExercise.setSportId(request.getSportId());
        }
        if (request.getParameterTemplates() != null) {
            existingExercise.setParameterTemplates(request.getParameterTemplates());
        }

        existingExercise.setUpdatedAt(LocalDateTime.now());

        ExerciseModel updatedExercise = exercisePersistencePort.save(existingExercise);
        log.info("Exercise updated successfully with ID: {}", updatedExercise.getId());

        return mapToResponse(updatedExercise);
    }

    @Override
    public void deleteExercise(Long id, String userEmail) {
        log.info("Deleting exercise with ID: {} for user: {}", id, userEmail);

        UserModel user = userPersistencePort.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ExerciseModel exercise = exercisePersistencePort.findById(id)
                .orElseThrow(() -> new RuntimeException("Exercise not found"));

        if (!exercise.getUserId().equals(user.getId())) {
            throw new RuntimeException("Not authorized to delete this exercise");
        }

        exercisePersistencePort.deleteById(id);
        log.info("Exercise deleted successfully with ID: {}", id);
    }

    private ExerciseResponse mapToResponse(ExerciseModel exercise) {
        ExerciseResponse response = new ExerciseResponse();
        response.setId(exercise.getId());
        response.setName(exercise.getName());
        response.setDescription(exercise.getDescription());
        response.setSportId(exercise.getSportId());
        response.setUserId(exercise.getUserId());
        response.setPredefined(exercise.getIsPredefined());
        response.setParameterTemplates(exercise.getParameterTemplates());
        response.setCreatedAt(exercise.getCreatedAt());
        response.setUpdatedAt(exercise.getUpdatedAt());
        return response;
    }
}