package com.fitapp.backend.category.aplication.service;

import com.fitapp.backend.category.aplication.dto.request.ExerciseCategoryFilterRequest;
import com.fitapp.backend.category.aplication.dto.request.ExerciseCategoryRequest;
import com.fitapp.backend.category.aplication.dto.response.ExerciseCategoryPageResponse;
import com.fitapp.backend.category.aplication.dto.response.ExerciseCategoryResponse;
import com.fitapp.backend.category.aplication.port.input.ExerciseCategoryUseCase;
import com.fitapp.backend.category.aplication.port.output.ExerciseCategoryPersistencePort;
import com.fitapp.backend.category.domain.model.ExerciseCategoryModel;
import com.fitapp.backend.domain.exception.CategoryDuplicateException;
import com.fitapp.backend.domain.exception.CategoryNotFoundException;
import com.fitapp.backend.domain.exception.CategoryOwnershipException;
import com.fitapp.backend.domain.exception.PredefinedCategoryException;
import com.fitapp.backend.infrastructure.config.CacheService;
import com.fitapp.backend.suscription.aplication.service.SubscriptionLimitChecker;
import com.fitapp.backend.user.aplication.port.output.UserPersistencePort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExerciseCategoryServiceImpl implements ExerciseCategoryUseCase {

    private final ExerciseCategoryPersistencePort categoryPersistencePort;
    private final UserPersistencePort userPersistencePort;
    private final CacheService cacheService;
    private final SubscriptionLimitChecker limitChecker;

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ExerciseCategoryModel createCategory(ExerciseCategoryRequest request, String userEmail) {
        log.info("SERVICE_CREATE_CATEGORY_START | user={} | name={}", userEmail, request.getName());

        var user = userPersistencePort.findByEmail(userEmail)
                .orElseThrow(() -> new com.fitapp.backend.domain.exception.UserNotFoundException(
                        "Usuario no encontrado: " + userEmail));

        // Verificar límite de categorías personalizadas
        long currentCount = categoryPersistencePort.countByOwnerId(user.getId());
        limitChecker.checkCustomCategoryLimit(userEmail, currentCount);

        if (categoryPersistencePort.findByNameAndOwnerId(request.getName(), user.getId()).isPresent()) {
            throw new CategoryDuplicateException(request.getName(), userEmail);
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
        cacheService.clearUserCategoryCache(saved.getId(), userEmail);
        log.info("SERVICE_CREATE_CATEGORY_SUCCESS | id={} | user={}", saved.getId(), userEmail);
        return saved;
    }

    @Override
    @Transactional
    public ExerciseCategoryModel updateCategory(Long id, ExerciseCategoryRequest request, String userEmail) {
        log.info("SERVICE_UPDATE_CATEGORY_START | user={} | categoryId={}", userEmail, id);

        var user = userPersistencePort.findByEmail(userEmail)
                .orElseThrow(() -> new com.fitapp.backend.domain.exception.UserNotFoundException(
                        "Usuario no encontrado: " + userEmail));

        var existingCategory = categoryPersistencePort.findById(id)
                .orElseThrow(() -> {
                    log.warn("CATEGORY_NOT_FOUND_FOR_UPDATE | id={}", id);
                    return new CategoryNotFoundException(id);
                });

        if (!existingCategory.getOwnerId().equals(user.getId())) {
            log.warn("CATEGORY_UPDATE_UNAUTHORIZED | user={} | categoryId={}", userEmail, id);
            throw new CategoryOwnershipException(id, userEmail);
        }

        if (Boolean.TRUE.equals(existingCategory.getIsPredefined())) {
            log.warn("CATEGORY_UPDATE_PREDEFINED | categoryId={}", id);
            throw new PredefinedCategoryException(id);
        }

        existingCategory.setName(request.getName());
        existingCategory.setDescription(request.getDescription());
        existingCategory
                .setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : existingCategory.getIsPublic());
        existingCategory.setSportId(request.getSportId());
        existingCategory.setParentCategoryId(request.getParentCategoryId());
        existingCategory.setUpdatedAt(LocalDateTime.now());

        existingCategory.validate();

        ExerciseCategoryModel updated = categoryPersistencePort.save(existingCategory);

        cacheService.clearUserCategoryCache(id, userEmail);
        log.info("SERVICE_UPDATE_CATEGORY_SUCCESS | id={} | user={}", id, userEmail);
        return updated;
    }

    @Override
    @Transactional
    public void deleteCategory(Long id, String userEmail) {
        log.info("SERVICE_DELETE_CATEGORY_START | user={} | categoryId={}", userEmail, id);

        var user = userPersistencePort.findByEmail(userEmail)
                .orElseThrow(() -> new com.fitapp.backend.domain.exception.UserNotFoundException(
                        "Usuario no encontrado: " + userEmail));

        var category = categoryPersistencePort.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        if (!category.getOwnerId().equals(user.getId())) {
            log.warn("CATEGORY_DELETE_UNAUTHORIZED | user={} | categoryId={}", userEmail, id);
            throw new CategoryOwnershipException(id, userEmail);
        }

        if (Boolean.TRUE.equals(category.getIsPredefined())) {
            log.warn("CATEGORY_DELETE_PREDEFINED | categoryId={}", id);
            throw new PredefinedCategoryException(id);
        }

        categoryPersistencePort.delete(id);

        cacheService.clearUserCategoryCache(id, userEmail);
        log.info("SERVICE_DELETE_CATEGORY_SUCCESS | id={} | user={}", id, userEmail);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "category-by-id", key = "#id")
    public ExerciseCategoryModel getCategoryById(Long id, String userEmail) {
        log.debug("SERVICE_GET_CATEGORY_BY_ID | id={} | user={}", id, userEmail);
        return categoryPersistencePort.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    // ── Consultas paginadas ───────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ExerciseCategoryPageResponse getAllCategoriesPaginated(ExerciseCategoryFilterRequest filterRequest) {
        StopWatch sw = new StopWatch();
        sw.start();
        log.info("SERVICE_GET_ALL_CATEGORIES | search={} | isPredefined={}",
                filterRequest.getSearch(), filterRequest.getIsPredefined());

        Pageable pageable = createPageable(filterRequest);
        Page<ExerciseCategoryModel> page = categoryPersistencePort.findByFilters(filterRequest, pageable);

        sw.stop();
        log.info("SERVICE_GET_ALL_CATEGORIES_SUCCESS | totalElements={} | timeMs={}",
                page.getTotalElements(), sw.getTotalTimeMillis());
        return buildPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public ExerciseCategoryPageResponse getMyCategoriesPaginated(String userEmail,
            ExerciseCategoryFilterRequest filterRequest) {
        StopWatch sw = new StopWatch();
        sw.start();
        log.info("SERVICE_GET_MY_CATEGORIES | user={}", userEmail);

        var user = userPersistencePort.findByEmail(userEmail)
                .orElseThrow(() -> new com.fitapp.backend.domain.exception.UserNotFoundException(
                        "Usuario no encontrado: " + userEmail));

        filterRequest.setOwnerId(user.getId());
        filterRequest.setOnlyMine(true);
        filterRequest.setIncludePredefined(false);

        Pageable pageable = createPageable(filterRequest);
        Page<ExerciseCategoryModel> page = categoryPersistencePort.findByFilters(filterRequest, pageable);

        sw.stop();
        log.info("SERVICE_GET_MY_CATEGORIES_SUCCESS | user={} | count={} | timeMs={}",
                userEmail, page.getTotalElements(), sw.getTotalTimeMillis());
        return buildPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public ExerciseCategoryPageResponse getPredefinedCategoriesPaginated(ExerciseCategoryFilterRequest filterRequest) {
        StopWatch sw = new StopWatch();
        sw.start();
        log.info("SERVICE_GET_PREDEFINED_CATEGORIES | search={}", filterRequest.getSearch());

        filterRequest.setIsPredefined(true);
        filterRequest.setIncludePredefined(true);

        Pageable pageable = createPageable(filterRequest);
        Page<ExerciseCategoryModel> page = categoryPersistencePort.findByFilters(filterRequest, pageable);

        sw.stop();
        log.info("SERVICE_GET_PREDEFINED_CATEGORIES_SUCCESS | count={} | timeMs={}",
                page.getTotalElements(), sw.getTotalTimeMillis());
        return buildPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public ExerciseCategoryPageResponse getAvailableCategoriesPaginated(String userEmail, Long sportId,
            ExerciseCategoryFilterRequest filterRequest) {
        StopWatch sw = new StopWatch();
        sw.start();
        log.info("SERVICE_GET_AVAILABLE_CATEGORIES | user={} | sportId={}", userEmail, sportId);

        var user = userPersistencePort.findByEmail(userEmail)
                .orElseThrow(() -> new com.fitapp.backend.domain.exception.UserNotFoundException(
                        "Usuario no encontrado: " + userEmail));

        Pageable pageable = createPageable(filterRequest);
        Page<ExerciseCategoryModel> page = categoryPersistencePort.findAvailableForUser(
                user.getId(), sportId, pageable);

        sw.stop();
        log.info("SERVICE_GET_AVAILABLE_CATEGORIES_SUCCESS | user={} | count={} | timeMs={}",
                userEmail, page.getTotalElements(), sw.getTotalTimeMillis());
        return buildPageResponse(page);
    }

    // ── Operaciones específicas ───────────────────────────────────────────────

    @Override
    @Transactional
    public void toggleCategoryStatus(Long id, String userEmail) {
        log.info("SERVICE_TOGGLE_STATUS | categoryId={} | user={}", id, userEmail);

        var user = userPersistencePort.findByEmail(userEmail)
                .orElseThrow(() -> new com.fitapp.backend.domain.exception.UserNotFoundException(
                        "Usuario no encontrado: " + userEmail));

        var category = categoryPersistencePort.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        if (!category.getOwnerId().equals(user.getId())) {
            throw new CategoryOwnershipException(id, userEmail);
        }

        if (Boolean.TRUE.equals(category.getIsPredefined())) {
            throw new PredefinedCategoryException(id);
        }

        boolean newStatus = !Boolean.TRUE.equals(category.getIsActive());
        category.setIsActive(newStatus);
        category.setUpdatedAt(LocalDateTime.now());
        categoryPersistencePort.save(category);

        cacheService.clearUserCategoryCache(id, userEmail);
        log.info("SERVICE_TOGGLE_STATUS_SUCCESS | categoryId={} | newStatus={}", id, newStatus);
    }

    @Override
    @Transactional
    public void toggleCategoryVisibility(Long id, String userEmail) {
        log.info("SERVICE_TOGGLE_VISIBILITY | categoryId={} | user={}", id, userEmail);

        var user = userPersistencePort.findByEmail(userEmail)
                .orElseThrow(() -> new com.fitapp.backend.domain.exception.UserNotFoundException(
                        "Usuario no encontrado: " + userEmail));

        var category = categoryPersistencePort.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        if (!category.getOwnerId().equals(user.getId())) {
            throw new CategoryOwnershipException(id, userEmail);
        }

        if (Boolean.TRUE.equals(category.getIsPredefined())) {
            throw new PredefinedCategoryException(id);
        }

        boolean newVisibility = !Boolean.TRUE.equals(category.getIsPublic());
        category.setIsPublic(newVisibility);
        category.setUpdatedAt(LocalDateTime.now());
        categoryPersistencePort.save(category);

        cacheService.clearUserCategoryCache(id, userEmail);
        log.info("SERVICE_TOGGLE_VISIBILITY_SUCCESS | categoryId={} | isPublic={}", id, newVisibility);
    }

    @Override
    @Transactional
    public void incrementCategoryUsage(Long categoryId) {
        log.debug("SERVICE_INCREMENT_USAGE | categoryId={}", categoryId);
        // No lanzamos excepción si no existe — es una operación de telemetría, no debe
        // fallar el flujo principal
        categoryPersistencePort.findById(categoryId).ifPresentOrElse(
                category -> {
                    categoryPersistencePort.incrementUsageCount(categoryId);
                    // Limpiar solo la cache del ID para que se actualice el contador
                    cacheService.clearUserCategoryCache(categoryId, null);
                    log.debug("SERVICE_INCREMENT_USAGE_SUCCESS | categoryId={}", categoryId);
                },
                () -> log.warn("SERVICE_INCREMENT_USAGE_SKIP | categoryId={} not found", categoryId));
    }

    // ── Consultas de listas ───────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "available-categories", key = "#sportId != null ? #sportId : 'all'")
    public List<ExerciseCategoryResponse> getCategoriesBySport(Long sportId) {
        log.debug("SERVICE_GET_CATEGORIES_BY_SPORT | sportId={}", sportId);

        ExerciseCategoryFilterRequest filter = new ExerciseCategoryFilterRequest();
        filter.setSportId(sportId);
        filter.setIsActive(true);
        filter.setIncludePredefined(true);
        filter.setPage(0);
        filter.setSize(200); // Lista completa para uso en selects/dropdowns del móvil

        Pageable pageable = createPageable(filter);
        Page<ExerciseCategoryModel> page = categoryPersistencePort.findByFilters(filter, pageable);

        log.debug("SERVICE_GET_CATEGORIES_BY_SPORT_SUCCESS | sportId={} | count={}",
                sportId, page.getTotalElements());
        return page.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "most-used-categories", key = "#limit")
    public List<ExerciseCategoryResponse> getMostUsedCategories(int limit) {
        log.debug("SERVICE_GET_MOST_USED | limit={}", limit);

        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "usageCount"));
        Page<ExerciseCategoryModel> page = categoryPersistencePort.findMostUsedCategories(pageable);

        log.debug("SERVICE_GET_MOST_USED_SUCCESS | returned={}", page.getNumberOfElements());
        return page.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // ── Validaciones ─────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public boolean isCategoryNameAvailable(String name, String userEmail) {
        log.debug("SERVICE_CHECK_NAME_AVAILABLE | name={} | user={}", name, userEmail);

        var user = userPersistencePort.findByEmail(userEmail)
                .orElseThrow(() -> new com.fitapp.backend.domain.exception.UserNotFoundException(
                        "Usuario no encontrado: " + userEmail));

        boolean takenByUser = categoryPersistencePort.findByNameAndOwnerId(name, user.getId()).isPresent();
        boolean takenByPredefined = categoryPersistencePort.findByNameAndIsPredefined(name).isPresent();

        boolean available = !takenByUser && !takenByPredefined;
        log.debug("SERVICE_CHECK_NAME_AVAILABLE_RESULT | name={} | available={}", name, available);
        return available;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

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
}