package com.fitapp.backend.infrastructure.persistence.adapter.out;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.fitapp.backend.application.ports.output.ExercisePersistencePort;
import com.fitapp.backend.domain.model.ExerciseModel;
import com.fitapp.backend.infrastructure.persistence.converter.ExerciseConverter;
import com.fitapp.backend.infrastructure.persistence.entity.ExerciseEntity;
import com.fitapp.backend.infrastructure.persistence.repository.ExerciseRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ExercisePersistenceAdapter implements ExercisePersistencePort {
    private final ExerciseRepository exerciseRepository;
    private final ExerciseConverter exerciseConverter;

    @Override
    public Optional<ExerciseModel> findById(Long id) {
        return exerciseRepository.findById(id)
                .map(exerciseConverter::toDomain);
    }

    @Override
    public List<ExerciseModel> findBySportId(Long sportId) {
        return exerciseRepository.findBySportId(sportId).stream()
                .map(exerciseConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExerciseModel> findByUserId(Long userId) {
        return exerciseRepository.findByUserId(userId).stream()
                .map(exerciseConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExerciseModel> findPredefinedExercises() {
        return exerciseRepository.findByIsPredefinedTrue().stream()
                .map(exerciseConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public ExerciseModel save(ExerciseModel exercise) {
        ExerciseEntity entity = exerciseConverter.toEntity(exercise);
        ExerciseEntity savedEntity = exerciseRepository.save(entity);
        return exerciseConverter.toDomain(savedEntity);
    }

    @Override
    public void deleteById(Long id) {
        exerciseRepository.deleteById(id);
    }
}