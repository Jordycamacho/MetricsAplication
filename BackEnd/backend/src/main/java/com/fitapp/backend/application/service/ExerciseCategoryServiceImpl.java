package com.fitapp.backend.application.service;

import com.fitapp.backend.application.dto.category.request.ExerciseCategoryFilterRequest;
import com.fitapp.backend.application.dto.category.request.ExerciseCategoryRequest;
import com.fitapp.backend.application.dto.category.response.ExerciseCategoryPageResponse;
import com.fitapp.backend.application.dto.category.response.ExerciseCategoryResponse;
import com.fitapp.backend.application.ports.input.ExerciseCategoryUseCase;
import com.fitapp.backend.application.ports.output.ExerciseCategoryPersistencePort;
import com.fitapp.backend.application.ports.output.UserPersistencePort;
import com.fitapp.backend.domain.model.ExerciseCategoryModel;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExerciseCategoryServiceImpl implements ExerciseCategoryUseCase {

    private final ExerciseCategoryPersistencePort categoryPersistencePort;
    private final UserPersistencePort userPersistencePort;

    @Override
    @Transactional
    public ExerciseCategoryModel createCategory(ExerciseCategoryRequest request, String userEmail) {
        log.info("SERVICE_CREATE_CATEGORY_START | user={} | name={}", userEmail, request.getName());

        var user = userPersistencePort.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.error("USER_NOT_FOUND_FOR_CATEGORY | email={}", userEmail);
                    return new RuntimeException("Usuario no encontrado");
                });

        // Validar unicidad del nombre
        boolean nameExists = categoryPersistencePort.findByNameAndOwnerId(request.getName(), user.getId()).isPresent();
        if (nameExists) {
            log.error("CATEGORY_NAME_DUPLICATE | user={} | name={}", userEmail, request.getName());
            throw new RuntimeException("Ya existe una categoría con este nombre");
        }

        ExerciseCategoryModel model = new ExerciseCategoryModel();
        model.setName(request.getName());
        model.setDescription(request.getDescription());
        model.setIsPredefined(false);
        model.setIsActive(true);
        model.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : false);
        model.setOwnerId(user.getId());
        model.setSportId(request.getSportId());
        model.setParentCategoryId(request.getParentCategoryId());
        model.setUsageCount(0);
        model.setCreatedAt(LocalDateTime.now());
        model.setUpdatedAt(LocalDateTime.now());

        model.validate();

        ExerciseCategoryModel saved = categoryPersistencePort.save(model);

        log.info("SERVICE_CREATE_CATEGORY_SUCCESS | id={} | user={}", saved.getId(), userEmail);
        return saved;
    }

    @Override
    @Transactional
    public ExerciseCategoryModel updateCategory(Long id, ExerciseCategoryRequest request, String userEmail) {
        log.info("SERVICE_UPDATE_CATEGORY_START | user={} | categoryId={}", userEmail, id);

        var user = userPersistencePort.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        var existingCategory = categoryPersistencePort.findById(id)
                .orElseThrow(() -> {
                    log.error("CATEGORY_NOT_FOUND | id={}", id);
                    return new RuntimeException("Categoría no encontrada");
                });

        // Validar permisos (solo dueño puede modificar)
        if (!existingCategory.getOwnerId().equals(user.getId())) {
            log.error("CATEGORY_UPDATE_UNAUTHORIZED | user={} | categoryId={} | ownerId={}",
                    userEmail, id, existingCategory.getOwnerId());
            throw new RuntimeException("No tienes permisos para modificar esta categoría");
        }

        // Validar que no sea predefinida
        if (Boolean.TRUE.equals(existingCategory.getIsPredefined())) {
            log.error("CATEGORY_UPDATE_PREDEFINED | categoryId={} is predefined", id);
            throw new RuntimeException("No se pueden modificar categorías predefinidas");
        }

        // Actualizar campos
        existingCategory.setName(request.getName());
        existingCategory.setDescription(request.getDescription());
        existingCategory
                .setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : existingCategory.getIsPublic());
        existingCategory.setSportId(request.getSportId());
        existingCategory.setParentCategoryId(request.getParentCategoryId());
        existingCategory.setUpdatedAt(LocalDateTime.now());

        existingCategory.validate();

        ExerciseCategoryModel updated = categoryPersistencePort.save(existingCategory);

        log.info("SERVICE_UPDATE_CATEGORY_SUCCESS | id={} | user={}", id, userEmail);
        return updated;
    }

    @Override
    @Transactional
    public void deleteCategory(Long id, String userEmail) {
        log.info("SERVICE_DELETE_CATEGORY_START | user={} | categoryId={}", userEmail, id);

        var user = userPersistencePort.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        var category = categoryPersistencePort.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        // Validar permisos
        if (!category.getOwnerId().equals(user.getId())) {
            log.error("CATEGORY_DELETE_UNAUTHORIZED | user={} | categoryId={}", userEmail, id);
            throw new RuntimeException("No tienes permisos para eliminar esta categoría");
        }

        // Validar que no sea predefinida
        if (Boolean.TRUE.equals(category.getIsPredefined())) {
            log.error("CATEGORY_DELETE_PREDEFINED | categoryId={} is predefined", id);
            throw new RuntimeException("No se pueden eliminar categorías predefinidas");
        }

        categoryPersistencePort.delete(id);

        log.info("SERVICE_DELETE_CATEGORY_SUCCESS | id={} | user={}", id, userEmail);
    }

    @Override
    @Transactional(readOnly = true)
    public ExerciseCategoryPageResponse getAllCategoriesPaginated(ExerciseCategoryFilterRequest filterRequest) {
        log.info("SERVICE_GET_ALL_CATEGORIES | filters={}", filterRequest);

        Pageable pageable = createPageable(filterRequest);
        Page<ExerciseCategoryModel> page = categoryPersistencePort.findByFilters(filterRequest, pageable);

        return buildPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public ExerciseCategoryPageResponse getMyCategoriesPaginated(String userEmail,
            ExerciseCategoryFilterRequest filterRequest) {
        log.info("SERVICE_GET_MY_CATEGORIES | user={}", userEmail);

        var user = userPersistencePort.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        filterRequest.setOwnerId(user.getId());
        filterRequest.setOnlyMine(true);
        filterRequest.setIncludePredefined(false);

        Pageable pageable = createPageable(filterRequest);
        Page<ExerciseCategoryModel> page = categoryPersistencePort.findByFilters(filterRequest, pageable);

        log.info("SERVICE_GET_MY_CATEGORIES_SUCCESS | user={} | count={}", userEmail, page.getTotalElements());
        return buildPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public ExerciseCategoryPageResponse getAvailableCategoriesPaginated(String userEmail, Long sportId,
            ExerciseCategoryFilterRequest filterRequest) {
        log.info("SERVICE_GET_AVAILABLE_CATEGORIES | user={} | sportId={}", userEmail, sportId);

        var user = userPersistencePort.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Pageable pageable = createPageable(filterRequest);
        Page<ExerciseCategoryModel> page = categoryPersistencePort.findAvailableForUser(user.getId(), sportId,
                pageable);

        log.info("SERVICE_GET_AVAILABLE_CATEGORIES_SUCCESS | user={} | count={}", userEmail, page.getTotalElements());
        return buildPageResponse(page);
    }

    // Métodos auxiliares
    private Pageable createPageable(ExerciseCategoryFilterRequest filterRequest) {
        Sort sort = Sort.by(filterRequest.getDirection(), filterRequest.getSortBy());
        return PageRequest.of(filterRequest.getPage(), filterRequest.getSize(), sort);
    }

    private ExerciseCategoryPageResponse buildPageResponse(Page<ExerciseCategoryModel> page) {
        List<ExerciseCategoryResponse> content = page.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ExerciseCategoryPageResponse.builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    private ExerciseCategoryResponse convertToResponse(ExerciseCategoryModel model) {
        return ExerciseCategoryResponse.builder()
                .id(model.getId())
                .name(model.getName())
                .description(model.getDescription())
                .isPredefined(model.getIsPredefined())
                .isActive(model.getIsActive())
                .isPublic(model.getIsPublic())
                .ownerId(model.getOwnerId())
                .sportId(model.getSportId())
                .parentCategoryId(model.getParentCategoryId())
                .usageCount(model.getUsageCount())
                .createdAt(model.getCreatedAt())
                .updatedAt(model.getUpdatedAt())
                .build();
    }

    @Override
    public ExerciseCategoryModel getCategoryById(Long id, String userEmail) {
        log.info("Getting category by ID: {} for user: {}", id, userEmail);

        ExerciseCategoryModel category = categoryPersistencePort.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));

        return category;
    }

    @Override
    public ExerciseCategoryPageResponse getPredefinedCategoriesPaginated(ExerciseCategoryFilterRequest filterRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPredefinedCategoriesPaginated'");
    }

    @Override
    public void toggleCategoryStatus(Long id, String userEmail) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toggleCategoryStatus'");
    }

    @Override
    public void toggleCategoryVisibility(Long id, String userEmail) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toggleCategoryVisibility'");
    }

    @Override
    public void incrementCategoryUsage(Long categoryId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'incrementCategoryUsage'");
    }

    @Override
    public List<ExerciseCategoryResponse> getCategoriesBySport(Long sportId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCategoriesBySport'");
    }

    @Override
    public List<ExerciseCategoryResponse> getMostUsedCategories(int limit) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMostUsedCategories'");
    }

    @Override
    public boolean isCategoryNameAvailable(String name, String userEmail) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isCategoryNameAvailable'");
    }
}