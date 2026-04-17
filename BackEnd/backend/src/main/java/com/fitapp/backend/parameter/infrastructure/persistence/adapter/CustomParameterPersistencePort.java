package com.fitapp.backend.parameter.infrastructure.persistence.adapter;

import com.fitapp.backend.parameter.application.dto.request.CustomParameterFilterRequest;
import com.fitapp.backend.parameter.domain.model.CustomParameterModel;
import com.fitapp.backend.parameter.infrastructure.persistence.entity.ParameterType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface CustomParameterPersistencePort {
    Optional<CustomParameterModel> findById(Long id);
    long countByOwnerId(Long userId);
    List<CustomParameterModel> findAll();
    Optional<Long> findIdByNameAndGlobal(String name);
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