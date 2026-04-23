package com.fitapp.backend.category.aplication.port.input;

import com.fitapp.backend.category.aplication.dto.request.ExerciseCategoryFilterRequest;
import com.fitapp.backend.category.aplication.dto.request.ExerciseCategoryRequest;
import com.fitapp.backend.category.aplication.dto.response.ExerciseCategoryPageResponse;
import com.fitapp.backend.category.aplication.dto.response.ExerciseCategoryResponse;
import com.fitapp.backend.category.domain.model.ExerciseCategoryModel;

import java.util.List;

public interface ExerciseCategoryUseCase {
    
    // Operaciones CRUD
    ExerciseCategoryModel createCategory(ExerciseCategoryRequest request, String userEmail);
    ExerciseCategoryModel updateCategory(Long id, ExerciseCategoryRequest request, String userEmail);
    void deleteCategory(Long id, String userEmail);
    ExerciseCategoryModel getCategoryById(Long id, String userEmail);

    
    // Consultas paginadas
    ExerciseCategoryPageResponse getAllCategoriesPaginated(ExerciseCategoryFilterRequest filterRequest);
    ExerciseCategoryPageResponse getMyCategoriesPaginated(String userEmail, ExerciseCategoryFilterRequest filterRequest);
    ExerciseCategoryPageResponse getPredefinedCategoriesPaginated(ExerciseCategoryFilterRequest filterRequest);
    ExerciseCategoryPageResponse getAvailableCategoriesPaginated(String userEmail, Long sportId, ExerciseCategoryFilterRequest filterRequest);
    
    // Operaciones específicas
    void toggleCategoryStatus(Long id, String userEmail);
    void toggleCategoryVisibility(Long id, String userEmail);
    void incrementCategoryUsage(Long categoryId);
    
    // Consultas de listas
    List<ExerciseCategoryResponse> getCategoriesBySport(Long sportId);
    List<ExerciseCategoryResponse> getMostUsedCategories(int limit);
    
    // Validaciones
    boolean isCategoryNameAvailable(String name, String userEmail);
}