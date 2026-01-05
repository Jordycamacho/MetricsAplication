package com.fitapp.backend.application.ports.input;

import com.fitapp.backend.application.dto.category.request.ExerciseCategoryFilterRequest;
import com.fitapp.backend.application.dto.category.request.ExerciseCategoryRequest;
import com.fitapp.backend.application.dto.category.response.ExerciseCategoryPageResponse;
import com.fitapp.backend.application.dto.category.response.ExerciseCategoryResponse;
import com.fitapp.backend.domain.model.ExerciseCategoryModel;

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