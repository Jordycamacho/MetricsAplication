package com.fitapp.backend.category.aplication.port.output;

import com.fitapp.backend.category.aplication.dto.request.ExerciseCategoryFilterRequest;
import com.fitapp.backend.category.domain.model.ExerciseCategoryModel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ExerciseCategoryPersistencePort {
    
    // Operaciones básicas
    Optional<ExerciseCategoryModel> findById(Long id);
    ExerciseCategoryModel save(ExerciseCategoryModel category);
    void delete(Long id);
    
    // Consultas específicas
    Optional<ExerciseCategoryModel> findByNameAndOwnerId(String name, Long ownerId);
    Optional<ExerciseCategoryModel> findByNameAndIsPredefined(String name);
    Page<ExerciseCategoryModel> findByOwnerId(Long ownerId, Pageable pageable);
    Page<ExerciseCategoryModel> findByIsPredefined(boolean isPredefined, Pageable pageable);
    Page<ExerciseCategoryModel> findByIsActive(boolean isActive, Pageable pageable);
    
    // Consultas con filtros
    Page<ExerciseCategoryModel> findByFilters(ExerciseCategoryFilterRequest filters, Pageable pageable);
    Page<ExerciseCategoryModel> findAvailableForUser(Long userId, Long sportId, Pageable pageable);
    
    // Estadísticas
    Long countByOwnerId(Long ownerId);
    Page<ExerciseCategoryModel> findMostUsedCategories(Pageable pageable);
    
    // Actualizaciones
    void incrementUsageCount(Long categoryId);
}