package com.fitapp.backend.application.service;

import com.fitapp.backend.application.dto.exercise.request.ExerciseFilterRequest;
import com.fitapp.backend.application.dto.exercise.request.ExerciseRequest;
import com.fitapp.backend.application.dto.exercise.response.ExercisePageResponse;
import com.fitapp.backend.application.dto.exercise.response.ExerciseResponse;
import com.fitapp.backend.application.logging.ExerciseLogger;
import com.fitapp.backend.application.ports.input.ExerciseUseCase;
import com.fitapp.backend.application.ports.output.CustomParameterPersistencePort;
import com.fitapp.backend.application.ports.output.ExerciseCategoryPersistencePort;
import com.fitapp.backend.application.ports.output.ExercisePersistencePort;
import com.fitapp.backend.application.ports.output.SportPersistencePort;
import com.fitapp.backend.application.ports.output.UserPersistencePort;
import com.fitapp.backend.domain.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExerciseServiceImpl implements ExerciseUseCase {
    
    private final ExercisePersistencePort exercisePersistencePort;
    private final UserPersistencePort userPersistencePort;
    private final SportPersistencePort sportPersistencePort;
    private final ExerciseCategoryPersistencePort categoryPersistencePort;
    private final CustomParameterPersistencePort parameterPersistencePort;
    private final ExerciseLogger exerciseLogger;
    
    @Override
    @Transactional(readOnly = true)
    public ExercisePageResponse getAllExercisesPaginated(ExerciseFilterRequest filterRequest) {
        exerciseLogger.logServiceEntry("getAllExercisesPaginated", filterRequest);
        
        try {
            Pageable pageable = createPageable(filterRequest);
            Page<ExerciseModel> page = exercisePersistencePort.findByFilters(filterRequest, pageable);
            
            exerciseLogger.logExerciseRetrieval("SYSTEM", page.getNumberOfElements(), "ALL_PAGINATED");
            
            return buildPageResponse(page, filterRequest);
        } catch (Exception e) {
            exerciseLogger.logServiceError("getAllExercisesPaginated", "Error retrieving exercises", e);
            throw e;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public ExercisePageResponse getMyExercisesPaginated(String userEmail, ExerciseFilterRequest filterRequest) {
        exerciseLogger.logServiceEntry("getMyExercisesPaginated", userEmail, filterRequest);
        
        try {
            UserModel user = getUserByEmail(userEmail);
            
            filterRequest.setCreatedBy(user.getId());
            
            Pageable pageable = createPageable(filterRequest);
            Page<ExerciseModel> page = exercisePersistencePort.findByFilters(filterRequest, pageable);
            
            exerciseLogger.logExerciseRetrieval(userEmail, page.getNumberOfElements(), "MY_PAGINATED");
            
            return buildPageResponse(page, filterRequest);
        } catch (Exception e) {
            exerciseLogger.logServiceError("getMyExercisesPaginated", "Error retrieving user exercises", e);
            throw e;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public ExercisePageResponse getAvailableExercisesPaginated(String userEmail, ExerciseFilterRequest filterRequest) {
        exerciseLogger.logServiceEntry("getAvailableExercisesPaginated", userEmail, filterRequest);
        
        try {
            UserModel user = getUserByEmail(userEmail);
            
            Pageable pageable = createPageable(filterRequest);
            Page<ExerciseModel> page = exercisePersistencePort.findAvailableForUser(user.getId(), pageable);
            
            exerciseLogger.logExerciseRetrieval(userEmail, page.getNumberOfElements(), "AVAILABLE_PAGINATED");
            
            return buildPageResponse(page, filterRequest);
        } catch (Exception e) {
            exerciseLogger.logServiceError("getAvailableExercisesPaginated", "Error retrieving available exercises", e);
            throw e;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public ExercisePageResponse getExercisesBySport(String userEmail, Long sportId, ExerciseFilterRequest filterRequest) {
        exerciseLogger.logServiceEntry("getExercisesBySport", userEmail, sportId, filterRequest);
        
        try {
            UserModel user = getUserByEmail(userEmail);
            
            Pageable pageable = createPageable(filterRequest);
            Page<ExerciseModel> page = exercisePersistencePort.findAvailableForUserAndSport(user.getId(), sportId, pageable);
            
            exerciseLogger.logExerciseRetrieval(userEmail, page.getNumberOfElements(), "BY_SPORT");
            
            return buildPageResponse(page, filterRequest);
        } catch (Exception e) {
            exerciseLogger.logServiceError("getExercisesBySport", "Error retrieving exercises by sport", e);
            throw e;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public ExerciseModel getExerciseById(Long id) {
        exerciseLogger.logServiceEntry("getExerciseById", id);
        
        try {
            ExerciseModel exercise = exercisePersistencePort.findById(id)
                    .orElseThrow(() -> {
                        log.error("EXERCISE_NOT_FOUND | id={}", id);
                        return new RuntimeException("Exercise not found");
                    });
            
            exerciseLogger.logExerciseDetailAccess("SYSTEM", id, exercise.getName());
            return exercise;
        } catch (Exception e) {
            exerciseLogger.logServiceError("getExerciseById", "Error retrieving exercise", e);
            throw e;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public ExerciseModel getExerciseByIdWithRelations(Long id) {
        exerciseLogger.logServiceEntry("getExerciseByIdWithRelations", id);
        
        try {
            ExerciseModel exercise = exercisePersistencePort.findByIdWithRelations(id)
                    .orElseThrow(() -> {
                        log.error("EXERCISE_NOT_FOUND_WITH_RELATIONS | id={}", id);
                        return new RuntimeException("Exercise not found");
                    });
            
            exerciseLogger.logExerciseDetailAccess("SYSTEM", id, exercise.getName());
            return exercise;
        } catch (Exception e) {
            exerciseLogger.logServiceError("getExerciseByIdWithRelations", "Error retrieving exercise with relations", e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public ExerciseModel createExercise(ExerciseRequest request, String userEmail) {
        exerciseLogger.logServiceEntry("createExercise", request, userEmail);
        
        try {
            UserModel user = getUserByEmail(userEmail);
            request.logRequestData();
            
            // Validar que no exista un ejercicio con el mismo nombre para este usuario
            exercisePersistencePort.findByNameAndCreatedById(request.getName(), user.getId())
                    .ifPresent(exercise -> {
                        log.error("EXERCISE_ALREADY_EXISTS | name={} | userId={}", request.getName(), user.getId());
                        throw new RuntimeException("Exercise already exists for this user");
                    });
            
            // Validar deporte
            SportModel sport = sportPersistencePort.findById(request.getSportId())
                    .orElseThrow(() -> {
                        log.error("SPORT_NOT_FOUND | sportId={}", request.getSportId());
                        return new RuntimeException("Sport not found");
                    });
            
            // Validar categorías
            Set<ExerciseCategoryModel> categories = validateCategories(request.getCategoryIds());
            
            // Validar parámetros soportados
            Set<CustomParameterModel> supportedParameters = validateParameters(request.getSupportedParameterIds());
            
            // Crear modelo del ejercicio
            ExerciseModel exerciseModel = new ExerciseModel();
            exerciseModel.setName(request.getName());
            exerciseModel.setDescription(request.getDescription());
            exerciseModel.setExerciseType(request.getExerciseType());
            exerciseModel.setSportId(sport.getId());
            exerciseModel.setSportName(sport.getName());
            exerciseModel.setCreatedById(user.getId());
            exerciseModel.setCreatedByEmail(user.getEmail());
            exerciseModel.setCategoryIds(categories.stream()
                    .map(ExerciseCategoryModel::getId)
                    .collect(Collectors.toSet()));
            exerciseModel.setCategoryNames(categories.stream()
                    .map(ExerciseCategoryModel::getName)
                    .collect(Collectors.toSet()));
            exerciseModel.setSupportedParameterIds(supportedParameters.stream()
                    .map(CustomParameterModel::getId)
                    .collect(Collectors.toSet()));
            exerciseModel.setSupportedParameterNames(supportedParameters.stream()
                    .map(CustomParameterModel::getName)
                    .collect(Collectors.toSet()));
            exerciseModel.setIsActive(true);
            exerciseModel.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : false);
            exerciseModel.setUsageCount(0);
            exerciseModel.setRating(0.0);
            exerciseModel.setRatingCount(0);
            
            // Validar el modelo
            exerciseModel.validate();
            
            // Guardar
            ExerciseModel savedExercise = exercisePersistencePort.save(exerciseModel);
            
            // Incrementar uso de parámetros
            supportedParameters.forEach(param -> {
                parameterPersistencePort.incrementUsageCount(param.getId());
            });
            
            exerciseLogger.logExerciseCreation(userEmail, savedExercise.getId(), savedExercise.getName());
            
            return savedExercise;
        } catch (Exception e) {
            exerciseLogger.logServiceError("createExercise", "Error creating exercise", e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public ExerciseModel updateExercise(Long id, ExerciseRequest request, String userEmail) {
        exerciseLogger.logServiceEntry("updateExercise", id, request, userEmail);
        
        try {
            UserModel user = getUserByEmail(userEmail);
            
            // Obtener ejercicio existente
            ExerciseModel existingExercise = exercisePersistencePort.findById(id)
                    .orElseThrow(() -> {
                        log.error("EXERCISE_NOT_FOUND_FOR_UPDATE | id={}", id);
                        return new RuntimeException("Exercise not found");
                    });
            
            // Validar permisos
            validateOwnership(existingExercise, user.getId());
            
            // Validar deporte si se cambia
            SportModel sport = existingExercise.getSportId().equals(request.getSportId()) 
                    ? sportPersistencePort.findById(existingExercise.getSportId()).orElse(null)
                    : sportPersistencePort.findById(request.getSportId())
                            .orElseThrow(() -> {
                                log.error("SPORT_NOT_FOUND_FOR_UPDATE | sportId={}", request.getSportId());
                                return new RuntimeException("Sport not found");
                            });
            
            // Validar categorías
            Set<ExerciseCategoryModel> categories = request.getCategoryIds() != null 
                    ? validateCategories(request.getCategoryIds())
                    : validateCategories(existingExercise.getCategoryIds());
            
            // Validar parámetros
            Set<CustomParameterModel> supportedParameters = request.getSupportedParameterIds() != null
                    ? validateParameters(request.getSupportedParameterIds())
                    : validateParameters(existingExercise.getSupportedParameterIds());
            
            // Actualizar modelo
            existingExercise.setName(request.getName() != null ? request.getName() : existingExercise.getName());
            existingExercise.setDescription(request.getDescription() != null ? request.getDescription() : existingExercise.getDescription());
            existingExercise.setExerciseType(request.getExerciseType() != null ? request.getExerciseType() : existingExercise.getExerciseType());
            existingExercise.setSportId(sport.getId());
            existingExercise.setSportName(sport.getName());
            existingExercise.setCategoryIds(categories.stream()
                    .map(ExerciseCategoryModel::getId)
                    .collect(Collectors.toSet()));
            existingExercise.setCategoryNames(categories.stream()
                    .map(ExerciseCategoryModel::getName)
                    .collect(Collectors.toSet()));
            existingExercise.setSupportedParameterIds(supportedParameters.stream()
                    .map(CustomParameterModel::getId)
                    .collect(Collectors.toSet()));
            existingExercise.setSupportedParameterNames(supportedParameters.stream()
                    .map(CustomParameterModel::getName)
                    .collect(Collectors.toSet()));
            existingExercise.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : existingExercise.getIsPublic());
            
            // Validar
            existingExercise.validate();
            
            // Guardar
            ExerciseModel updatedExercise = exercisePersistencePort.save(existingExercise);
            
            exerciseLogger.logExerciseUpdate(userEmail, id, updatedExercise.getName());
            
            return updatedExercise;
        } catch (Exception e) {
            exerciseLogger.logServiceError("updateExercise", "Error updating exercise", e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public void deleteExercise(Long id, String userEmail) {
        exerciseLogger.logServiceEntry("deleteExercise", id, userEmail);
        
        try {
            UserModel user = getUserByEmail(userEmail);
            
            ExerciseModel exercise = exercisePersistencePort.findById(id)
                    .orElseThrow(() -> {
                        log.error("EXERCISE_NOT_FOUND_FOR_DELETION | id={}", id);
                        return new RuntimeException("Exercise not found");
                    });
            
            // Validar permisos
            validateOwnership(exercise, user.getId());
            
            // Verificar si el ejercicio está en uso (podrías verificar en sesiones de entrenamiento)
            if (exercise.getUsageCount() > 0) {
                log.warn("EXERCISE_IN_USE | id={} | usageCount={}", id, exercise.getUsageCount());
                // Podrías optar por desactivar en lugar de eliminar
                exercise.setIsActive(false);
                exercisePersistencePort.save(exercise);
                exerciseLogger.logExerciseDeactivation(userEmail, id);
            } else {
                exercisePersistencePort.delete(id);
                exerciseLogger.logExerciseDeletion(userEmail, id);
            }
        } catch (Exception e) {
            exerciseLogger.logServiceError("deleteExercise", "Error deleting exercise", e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public void toggleExerciseStatus(Long id, String userEmail) {
        exerciseLogger.logServiceEntry("toggleExerciseStatus", id, userEmail);
        
        try {
            UserModel user = getUserByEmail(userEmail);
            
            ExerciseModel exercise = exercisePersistencePort.findById(id)
                    .orElseThrow(() -> {
                        log.error("EXERCISE_NOT_FOUND_FOR_TOGGLE | id={}", id);
                        return new RuntimeException("Exercise not found");
                    });
            
            // Validar permisos
            validateOwnership(exercise, user.getId());
            
            exercise.setIsActive(!exercise.getIsActive());
            exercisePersistencePort.save(exercise);
            
            exerciseLogger.logExerciseStatusToggle(userEmail, id, exercise.getIsActive());
        } catch (Exception e) {
            exerciseLogger.logServiceError("toggleExerciseStatus", "Error toggling exercise status", e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public void incrementExerciseUsage(Long exerciseId) {
        exerciseLogger.logServiceEntry("incrementExerciseUsage", exerciseId);
        
        try {
            exercisePersistencePort.incrementUsageCount(exerciseId);
            exerciseLogger.logExerciseUsageIncrement(exerciseId);
        } catch (Exception e) {
            exerciseLogger.logServiceError("incrementExerciseUsage", "Error incrementing exercise usage", e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public void rateExercise(Long exerciseId, Double rating, String userEmail) {
        exerciseLogger.logServiceEntry("rateExercise", exerciseId, rating, userEmail);
        
        try {
            getUserByEmail(userEmail); // Validar usuario
            
            if (rating < 1.0 || rating > 5.0) {
                throw new IllegalArgumentException("Rating must be between 1 and 5");
            }
            
            exercisePersistencePort.addRating(exerciseId, rating);
            exerciseLogger.logExerciseRating(exerciseId, rating, userEmail);
        } catch (Exception e) {
            exerciseLogger.logServiceError("rateExercise", "Error rating exercise", e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public ExerciseModel duplicateExercise(Long exerciseId, String userEmail) {
        exerciseLogger.logServiceEntry("duplicateExercise", exerciseId, userEmail);
        
        try {
            UserModel user = getUserByEmail(userEmail);
            
            ExerciseModel original = exercisePersistencePort.findByIdWithRelations(exerciseId)
                    .orElseThrow(() -> {
                        log.error("EXERCISE_NOT_FOUND_FOR_DUPLICATION | id={}", exerciseId);
                        return new RuntimeException("Exercise not found");
                    });
            
            // Crear copia
            ExerciseModel copy = new ExerciseModel();
            copy.setName(original.getName() + " (Copy)");
            copy.setDescription(original.getDescription());
            copy.setExerciseType(original.getExerciseType());
            copy.setSportId(original.getSportId());
            copy.setSportName(original.getSportName());
            copy.setCreatedById(user.getId());
            copy.setCreatedByEmail(user.getEmail());
            copy.setCategoryIds(new HashSet<>(original.getCategoryIds()));
            copy.setCategoryNames(new HashSet<>(original.getCategoryNames()));
            copy.setSupportedParameterIds(new HashSet<>(original.getSupportedParameterIds()));
            copy.setSupportedParameterNames(new HashSet<>(original.getSupportedParameterNames()));
            copy.setIsActive(true);
            copy.setIsPublic(false);
            copy.setUsageCount(0);
            copy.setRating(0.0);
            copy.setRatingCount(0);
            
            ExerciseModel savedCopy = exercisePersistencePort.save(copy);
            
            exerciseLogger.logExerciseDuplication(userEmail, exerciseId, savedCopy.getId());
            
            return savedCopy;
        } catch (Exception e) {
            exerciseLogger.logServiceError("duplicateExercise", "Error duplicating exercise", e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public ExerciseModel makeExercisePublic(Long exerciseId, String userEmail) {
        exerciseLogger.logServiceEntry("makeExercisePublic", exerciseId, userEmail);
        
        try {
            UserModel user = getUserByEmail(userEmail);
            
            ExerciseModel exercise = exercisePersistencePort.findById(exerciseId)
                    .orElseThrow(() -> {
                        log.error("EXERCISE_NOT_FOUND_FOR_PUBLISH | id={}", exerciseId);
                        return new RuntimeException("Exercise not found");
                    });
            
            // Validar permisos
            validateOwnership(exercise, user.getId());
            
            exercise.setIsPublic(true);
            ExerciseModel updatedExercise = exercisePersistencePort.save(exercise);
            
            exerciseLogger.logExercisePublish(userEmail, exerciseId);
            
            return updatedExercise;
        } catch (Exception e) {
            exerciseLogger.logServiceError("makeExercisePublic", "Error making exercise public", e);
            throw e;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public ExercisePageResponse getRecentlyUsedExercises(String userEmail, Pageable pageable) {
        exerciseLogger.logServiceEntry("getRecentlyUsedExercises", userEmail, pageable);
        
        try {
            Page<ExerciseModel> page = exercisePersistencePort.findRecentlyUsed(pageable);
            return buildPageResponse(page, null);
        } catch (Exception e) {
            exerciseLogger.logServiceError("getRecentlyUsedExercises", "Error retrieving recently used exercises", e);
            throw e;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public ExercisePageResponse getMostPopularExercises(Pageable pageable) {
        exerciseLogger.logServiceEntry("getMostPopularExercises", pageable);
        
        try {
            Page<ExerciseModel> page = exercisePersistencePort.findMostPopular(pageable);
            return buildPageResponse(page, null);
        } catch (Exception e) {
            exerciseLogger.logServiceError("getMostPopularExercises", "Error retrieving most popular exercises", e);
            throw e;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public ExercisePageResponse getTopRatedExercises(Pageable pageable) {
        exerciseLogger.logServiceEntry("getTopRatedExercises", pageable);
        
        try {
            Page<ExerciseModel> page = exercisePersistencePort.findTopRated(pageable);
            return buildPageResponse(page, null);
        } catch (Exception e) {
            exerciseLogger.logServiceError("getTopRatedExercises", "Error retrieving top rated exercises", e);
            throw e;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long getUserExerciseCount(String userEmail) {
        exerciseLogger.logServiceEntry("getUserExerciseCount", userEmail);
        
        try {
            UserModel user = getUserByEmail(userEmail);
            return exercisePersistencePort.countByUser(user.getId());
        } catch (Exception e) {
            exerciseLogger.logServiceError("getUserExerciseCount", "Error counting user exercises", e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public void cleanupInactiveExercises(int daysInactive) {
        exerciseLogger.logServiceEntry("cleanupInactiveExercises", daysInactive);
        
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysInactive);
            List<ExerciseModel> inactiveExercises = exercisePersistencePort.findInactiveBefore(cutoffDate);
            
            inactiveExercises.forEach(exercise -> {
                log.info("CLEANUP_INACTIVE_EXERCISE | id={} | name={} | lastUpdated={}", 
                        exercise.getId(), exercise.getName(), exercise.getUpdatedAt());
                exercisePersistencePort.delete(exercise.getId());
            });
            
            exerciseLogger.logCleanup(inactiveExercises.size(), daysInactive);
        } catch (Exception e) {
            exerciseLogger.logServiceError("cleanupInactiveExercises", "Error cleaning up inactive exercises", e);
            throw e;
        }
    }
    
    // Métodos privados de ayuda
    private UserModel getUserByEmail(String email) {
        return userPersistencePort.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("USER_NOT_FOUND | email={}", email);
                    return new RuntimeException("User not found");
                });
    }
    
    private void validateOwnership(ExerciseModel exercise, Long userId) {
        if (!exercise.getCreatedById().equals(userId)) {
            log.error("UNAUTHORIZED_EXERCISE_ACCESS | userId={} | exerciseCreatorId={}", 
                    userId, exercise.getCreatedById());
            throw new RuntimeException("Unauthorized to modify this exercise");
        }
    }
    
    private Set<ExerciseCategoryModel> validateCategories(Set<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Set.of();
        }
        
        return categoryIds.stream()
                .map(categoryId -> categoryPersistencePort.findById(categoryId)
                        .orElseThrow(() -> {
                            log.error("CATEGORY_NOT_FOUND | categoryId={}", categoryId);
                            return new RuntimeException("Category not found: " + categoryId);
                        }))
                .collect(Collectors.toSet());
    }
    
    private Set<CustomParameterModel> validateParameters(Set<Long> parameterIds) {
        if (parameterIds == null || parameterIds.isEmpty()) {
            return Set.of();
        }
        
        return parameterIds.stream()
                .map(parameterId -> parameterPersistencePort.findById(parameterId)
                        .orElseThrow(() -> {
                            log.error("PARAMETER_NOT_FOUND | parameterId={}", parameterId);
                            return new RuntimeException("Parameter not found: " + parameterId);
                        }))
                .collect(Collectors.toSet());
    }
    
    private Pageable createPageable(ExerciseFilterRequest filterRequest) {
        if (filterRequest == null) {
            return PageRequest.of(0, 20, Sort.by("name").ascending());
        }
        
        // Ordenamiento personalizado
        Sort sort;
        
        if (Boolean.TRUE.equals(filterRequest.getSortByPopularity())) {
            sort = Sort.by(Sort.Direction.DESC, "usageCount");
        } else if (Boolean.TRUE.equals(filterRequest.getSortByRating())) {
            sort = Sort.by(Sort.Direction.DESC, "rating");
        } else if (filterRequest.getSortFields() != null && !filterRequest.getSortFields().isEmpty()) {
            // Múltiples campos de ordenamiento
            sort = filterRequest.getSortFields().stream()
                    .map(field -> new Sort.Order(field.getDirection(), field.getField()))
                    .collect(Collectors.collectingAndThen(
                            Collectors.toList(),
                            Sort::by
                    ));
        } else {
            // Ordenamiento simple
            sort = Sort.by(filterRequest.getDirection(), filterRequest.getSortBy());
        }
        
        return PageRequest.of(filterRequest.getPage(), filterRequest.getSize(), sort);
    }
    
    private ExercisePageResponse buildPageResponse(Page<ExerciseModel> page, ExerciseFilterRequest filterRequest) {
        List<ExerciseResponse> content = page.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        String appliedFilters = filterRequest != null ? buildAppliedFiltersString(filterRequest) : "";
        
        return ExercisePageResponse.builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .numberOfElements(page.getNumberOfElements())
                .appliedFilters(appliedFilters)
                .build();
    }
    
    private String buildAppliedFiltersString(ExerciseFilterRequest filterRequest) {
        StringBuilder sb = new StringBuilder();
        
        if (StringUtils.hasText(filterRequest.getSearch())) {
            sb.append("search=").append(filterRequest.getSearch()).append(";");
        }
        if (filterRequest.getExerciseType() != null) {
            sb.append("type=").append(filterRequest.getExerciseType()).append(";");
        }
        if (filterRequest.getSportId() != null) {
            sb.append("sport=").append(filterRequest.getSportId()).append(";");
        }
        
        return sb.toString();
    }
    
    private ExerciseResponse convertToResponse(ExerciseModel model) {
        ExerciseResponse response = new ExerciseResponse();
        response.setId(model.getId());
        response.setName(model.getName());
        response.setDescription(model.getDescription());
        response.setExerciseType(model.getExerciseType());
        response.setSportId(model.getSportId());
        response.setSportName(model.getSportName());
        response.setCreatedById(model.getCreatedById());
        response.setCreatedByEmail(model.getCreatedByEmail());
        response.setCategoryIds(model.getCategoryIds());
        response.setCategoryNames(model.getCategoryNames());
        response.setSupportedParameterIds(model.getSupportedParameterIds());
        response.setSupportedParameterNames(model.getSupportedParameterNames());
        response.setIsActive(model.getIsActive());
        response.setIsPublic(model.getIsPublic());
        response.setUsageCount(model.getUsageCount());
        response.setRating(model.getRating());
        response.setRatingCount(model.getRatingCount());
        response.setCreatedAt(model.getCreatedAt());
        response.setUpdatedAt(model.getUpdatedAt());
        response.setLastUsedAt(model.getLastUsedAt());
        
        return response;
    }
}