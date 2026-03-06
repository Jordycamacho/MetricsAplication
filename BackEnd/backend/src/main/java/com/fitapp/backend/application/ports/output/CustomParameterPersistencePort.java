package com.fitapp.backend.application.ports.output;

import com.fitapp.backend.application.dto.parameter.request.CustomParameterFilterRequest;
import com.fitapp.backend.domain.model.CustomParameterModel;
import com.fitapp.backend.infrastructure.persistence.entity.enums.ParameterType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface CustomParameterPersistencePort {
    Optional<CustomParameterModel> findById(Long id);
    long countByOwnerId(Long userId);
    List<CustomParameterModel> findAll();
    Page<CustomParameterModel> findAll(Pageable pageable);
    Page<CustomParameterModel> findByFilters(CustomParameterFilterRequest filters, Pageable pageable);
    Optional<CustomParameterModel> findByNameAndOwnerIdAndSportId(String name, Long ownerId);
    Page<CustomParameterModel> findByOwnerId(Long ownerId, Pageable pageable);
    Page<CustomParameterModel> findByIsGlobalTrue(Pageable pageable);
    Page<CustomParameterModel> findAvailableForUser(Long userId, Pageable pageable);
    List<ParameterType> findAllDistinctParameterTypes();
    CustomParameterModel save(CustomParameterModel parameterModel);
    void delete(Long id);
    void incrementUsageCount(Long parameterId);
}