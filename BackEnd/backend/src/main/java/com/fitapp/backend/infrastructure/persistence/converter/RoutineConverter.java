package com.fitapp.backend.infrastructure.persistence.converter;

import com.fitapp.backend.domain.model.RoutineExerciseModel;
import com.fitapp.backend.domain.model.RoutineModel;
import com.fitapp.backend.infrastructure.persistence.entity.ExerciseEntity;
import com.fitapp.backend.infrastructure.persistence.entity.RoutineEntity;
import com.fitapp.backend.infrastructure.persistence.entity.RoutineExerciseEntity;
import com.fitapp.backend.infrastructure.persistence.entity.SportEntity;
import com.fitapp.backend.infrastructure.persistence.entity.UserEntity;
import com.fitapp.backend.infrastructure.persistence.repository.ExerciseRepository;
import com.fitapp.backend.infrastructure.persistence.repository.SportRepository;
import com.fitapp.backend.infrastructure.persistence.repository.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoutineConverter {

    private final SpringDataUserRepository springDataUserRepository;
    private final SportRepository sportRepository;
    private final ExerciseRepository exerciseRepository;

    public RoutineModel toDomain(RoutineEntity entity) {
        RoutineModel routine = new RoutineModel();
        routine.setId(entity.getId());
        routine.setName(entity.getName());
        routine.setDescription(entity.getDescription());
        routine.setIsActive(entity.getIsActive());
        routine.setCreatedAt(entity.getCreatedAt());
        routine.setUpdatedAt(entity.getUpdatedAt());
        routine.setUserId(entity.getUser().getId());
        routine.setSportId(entity.getSport() != null ? entity.getSport().getId() : null);
        routine.setTrainingDays(entity.getTrainingDays());
        routine.setGoal(entity.getGoal());
        routine.setSessionsPerWeek(entity.getSessionsPerWeek());
        if (entity.getExercises() != null) {
            routine.setExercises(entity.getExercises().stream()
                    .map(this::toDomainExercise)
                    .collect(Collectors.toList()));
        } else {
            routine.setExercises(new ArrayList<>());
        }

        return routine;
    }

    public RoutineEntity toEntity(RoutineModel domain) {
        RoutineEntity entity = new RoutineEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setIsActive(domain.getIsActive());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setTrainingDays(domain.getTrainingDays());
        entity.setGoal(domain.getGoal());
        entity.setSessionsPerWeek(domain.getSessionsPerWeek());

        UserEntity user = springDataUserRepository.findById(domain.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + domain.getUserId()));
        entity.setUser(user);

        if (domain.getSportId() != null) {
            SportEntity sport = sportRepository.findById(domain.getSportId())
                    .orElseThrow(() -> new RuntimeException("Sport not found with id: " + domain.getSportId()));
            entity.setSport(sport);
        }

        if (domain.getExercises() != null) {
            entity.setExercises(domain.getExercises().stream()
                    .map(exerciseModel -> this.toEntityExercise(exerciseModel, entity))
                    .collect(Collectors.toList()));
        } else {
            entity.setExercises(new ArrayList<>());
        }

        return entity;
    }

    private RoutineExerciseModel toDomainExercise(RoutineExerciseEntity entity) {
        RoutineExerciseModel exercise = new RoutineExerciseModel();
        exercise.setId(entity.getId());
        exercise.setExerciseId(entity.getExercise().getId());
        exercise.setSets(entity.getSets());
        exercise.setTargetReps(entity.getTargetReps());
        exercise.setTargetWeight(entity.getTargetWeight());
        exercise.setRestIntervalSeconds(
                entity.getRestInterval() != null ? (int) entity.getRestInterval().getSeconds() : null);
        exercise.setRoutineId(entity.getRoutine().getId());
        return exercise;
    }

    private RoutineExerciseEntity toEntityExercise(RoutineExerciseModel domain, RoutineEntity routineEntity) {
        RoutineExerciseEntity entity = new RoutineExerciseEntity();
        entity.setId(domain.getId());
        entity.setRoutine(routineEntity); // Establecer la relación
        entity.setSets(domain.getSets());
        entity.setTargetReps(domain.getTargetReps());
        entity.setTargetWeight(domain.getTargetWeight());
        entity.setRestInterval(
                domain.getRestIntervalSeconds() != null ? Duration.ofSeconds(domain.getRestIntervalSeconds()) : null);

        ExerciseEntity exercise = exerciseRepository.findById(domain.getExerciseId())
                .orElseThrow(() -> new RuntimeException("Exercise not found"));
        entity.setExercise(exercise);

        return entity;
    }
}