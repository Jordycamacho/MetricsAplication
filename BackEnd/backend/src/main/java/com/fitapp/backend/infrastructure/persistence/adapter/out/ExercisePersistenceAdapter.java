package com.fitapp.backend.infrastructure.persistence.adapter.out;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.fitapp.backend.application.ports.output.ExercisePersistencePort;
import com.fitapp.backend.domain.model.ExerciseModel;
import com.fitapp.backend.infrastructure.persistence.converter.ExerciseConverter;
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
}