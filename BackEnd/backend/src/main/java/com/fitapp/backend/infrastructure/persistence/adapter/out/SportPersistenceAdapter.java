package com.fitapp.backend.infrastructure.persistence.adapter.out;

import com.fitapp.backend.application.ports.output.SportPersistencePort;
import com.fitapp.backend.domain.model.SportModel;
import com.fitapp.backend.infrastructure.persistence.converter.SportConverter;
import com.fitapp.backend.infrastructure.persistence.entity.SportEntity;
import com.fitapp.backend.infrastructure.persistence.repository.SportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SportPersistenceAdapter implements SportPersistencePort {
private final SportRepository sportRepository;
    private final SportConverter sportConverter;

    @Override
    public Optional<SportModel> findById(Long id) {
        return sportRepository.findById(id)
                .map(sportConverter::toDomain);
    }

    @Override
    public List<SportModel> findAll() {
        return sportRepository.findAll().stream()
                .map(sportConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<SportModel> findByIsPredefinedTrue() {
        return sportRepository.findByIsPredefinedTrue().stream()
                .map(sportConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<SportModel> findByCreatedBy(Long userId) {
        return sportRepository.findByCreatedById(userId).stream()
                .map(sportConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public SportModel save(SportModel sportModel) {
        SportEntity entity = sportConverter.toEntity(sportModel);
        SportEntity savedEntity = sportRepository.save(entity);
        return sportConverter.toDomain(savedEntity);
    }

    @Override
    public void delete(Long id) {
        sportRepository.deleteById(id);
    }
}