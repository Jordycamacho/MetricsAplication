package com.fitapp.backend.application.service;

import com.fitapp.backend.application.dto.parameter.request.CustomParameterFilterRequest;
import com.fitapp.backend.application.dto.parameter.request.CustomParameterRequest;
import com.fitapp.backend.application.dto.parameter.response.CustomParameterPageResponse;
import com.fitapp.backend.application.dto.parameter.response.CustomParameterResponse;
import com.fitapp.backend.application.logging.ParameterServiceLogger;
import com.fitapp.backend.application.ports.input.CustomParameterUseCase;
import com.fitapp.backend.application.ports.output.CustomParameterPersistencePort;
import com.fitapp.backend.application.ports.output.UserPersistencePort;
import com.fitapp.backend.domain.model.CustomParameterModel;
import com.fitapp.backend.infrastructure.persistence.entity.enums.ParameterType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomParameterServiceImpl implements CustomParameterUseCase {
    
    private final CustomParameterPersistencePort parameterPersistencePort;
    private final UserPersistencePort userPersistencePort;
    private final ParameterServiceLogger parameterLogger;
    
    @Override
    @Transactional(readOnly = true)
    public CustomParameterPageResponse getAllParametersPaginated(CustomParameterFilterRequest filterRequest) {
        parameterLogger.logServiceEntry("getAllParametersPaginated", filterRequest);
        
        try {
            Pageable pageable = createPageable(filterRequest);
            Page<CustomParameterModel> page = parameterPersistencePort.findByFilters(filterRequest, pageable);
            
            parameterLogger.logParameterRetrieval("SYSTEM", page.getNumberOfElements(), "ALL_PAGINATED");
            
            return buildPageResponse(page);
        } catch (Exception e) {
            parameterLogger.logServiceError("getAllParametersPaginated", "Error retrieving parameters", e);
            throw e;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public CustomParameterPageResponse getMyParametersPaginated(String userEmail, CustomParameterFilterRequest filterRequest) {
        parameterLogger.logServiceEntry("getMyParametersPaginated", userEmail, filterRequest);
        
        try {
            var user = userPersistencePort.findByEmail(userEmail)
                    .orElseThrow(() -> {
                        log.error("USER_NOT_FOUND_FOR_PARAMETERS | email={}", userEmail);
                        return new RuntimeException("User not found");
                    });
            
            filterRequest.setOwnerId(user.getId());
            filterRequest.setOnlyMine(true);
            
            Pageable pageable = createPageable(filterRequest);
            Page<CustomParameterModel> page = parameterPersistencePort.findByFilters(filterRequest, pageable);
            
            parameterLogger.logParameterRetrieval(userEmail, page.getNumberOfElements(), "MY_PAGINATED");
            
            return buildPageResponse(page);
        } catch (Exception e) {
            parameterLogger.logServiceError("getMyParametersPaginated", "Error retrieving user parameters", e);
            throw e;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public CustomParameterPageResponse getAvailableParametersPaginated(String userEmail, Long sportId, 
                                                                      CustomParameterFilterRequest filterRequest) {
        parameterLogger.logServiceEntry("getAvailableParametersPaginated", userEmail, sportId, filterRequest);
        
        try {
            var user = userPersistencePort.findByEmail(userEmail)
                    .orElseThrow(() -> {
                        log.error("USER_NOT_FOUND_FOR_AVAILABLE_PARAMETERS | email={}", userEmail);
                        return new RuntimeException("User not found");
                    });
            
            Pageable pageable = createPageable(filterRequest);
            Page<CustomParameterModel> page = parameterPersistencePort.findAvailableForUser(user.getId(), sportId, pageable);
            
            parameterLogger.logParameterRetrieval(userEmail, page.getNumberOfElements(), "AVAILABLE_PAGINATED");
            
            return buildPageResponse(page);
        } catch (Exception e) {
            parameterLogger.logServiceError("getAvailableParametersPaginated", "Error retrieving available parameters", e);
            throw e;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public CustomParameterModel getParameterById(Long id) {
        parameterLogger.logServiceEntry("getParameterById", id);
        
        try {
            return parameterPersistencePort.findById(id)
                    .orElseThrow(() -> {
                        log.error("PARAMETER_NOT_FOUND | id={}", id);
                        return new RuntimeException("Parameter not found: " + id);
                    });
        } catch (Exception e) {
            parameterLogger.logServiceError("getParameterById", "Error retrieving parameter", e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public CustomParameterModel createParameter(CustomParameterRequest request, String userEmail) {
        parameterLogger.logParameterCreationStart(userEmail, request.getName());
        parameterLogger.logServiceEntry("createParameter", request, userEmail);
        
        try {
            request.logRequestData();
            
            var user = userPersistencePort.findByEmail(userEmail)
                    .orElseThrow(() -> {
                        log.error("USER_NOT_FOUND_FOR_PARAMETER_CREATION | email={}", userEmail);
                        return new RuntimeException("User not found");
                    });
            
            // Validar unicidad
            boolean exists = parameterPersistencePort
                    .findByNameAndOwnerIdAndSportId(request.getName(), user.getId(), request.getSportId())
                    .isPresent();
            
            if (exists) {
                log.error("DUPLICATE_PARAMETER | name={} | ownerId={} | sportId={}", 
                         request.getName(), user.getId(), request.getSportId());
                throw new RuntimeException("Ya existe un parámetro con este nombre para el usuario y deporte especificados");
            }
            
            // Validar formato del nombre
            validateParameterName(request.getName());
            
            CustomParameterModel model = new CustomParameterModel();
            model.setName(request.getName());
            model.setDisplayName(request.getDisplayName() != null ? request.getDisplayName() : request.getName());
            model.setDescription(request.getDescription());
            model.setParameterType(request.getParameterType());
            model.setUnit(request.getUnit());
            model.setValidationRules(request.getValidationRules());
            model.setIsGlobal(request.getIsGlobal() != null ? request.getIsGlobal() : false);
            model.setIsActive(true);
            model.setOwnerId(user.getId());
            model.setSportId(request.getSportId());
            model.setCategory(request.getCategory());
            model.setUsageCount(0);
            
            model.validateFormat();
            model.logModelData("CREATING");
            
            CustomParameterModel savedModel = parameterPersistencePort.save(model);
            
            parameterLogger.logParameterCreationSuccess(savedModel.getId(), userEmail);
            parameterLogger.logServiceExit("createParameter", savedModel.getId());
            
            return savedModel;
        } catch (Exception e) {
            parameterLogger.logServiceError("createParameter", "Error creating parameter", e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public CustomParameterModel updateParameter(Long id, CustomParameterRequest request, String userEmail) {
        parameterLogger.logParameterUpdateStart(id, userEmail);
        parameterLogger.logServiceEntry("updateParameter", id, request, userEmail);
        
        try {
            var user = userPersistencePort.findByEmail(userEmail)
                    .orElseThrow(() -> {
                        log.error("USER_NOT_FOUND_FOR_PARAMETER_UPDATE | email={}", userEmail);
                        return new RuntimeException("User not found");
                    });
            
            CustomParameterModel existing = parameterPersistencePort.findById(id)
                    .orElseThrow(() -> {
                        log.error("PARAMETER_NOT_FOUND_FOR_UPDATE | id={}", id);
                        return new RuntimeException("Parameter not found");
                    });
            
            // Verificar permisos
            if (!existing.getOwnerId().equals(user.getId())) {
                log.error("UNAUTHORIZED_PARAMETER_UPDATE | parameterId={} | requesterId={} | ownerId={}", 
                         id, user.getId(), existing.getOwnerId());
                throw new RuntimeException("No tiene permisos para actualizar este parámetro");
            }
            
            // Validar unicidad si cambia el nombre
            if (!existing.getName().equals(request.getName())) {
                boolean exists = parameterPersistencePort
                        .findByNameAndOwnerIdAndSportId(request.getName(), user.getId(), request.getSportId())
                        .isPresent();
                
                if (exists) {
                    log.error("DUPLICATE_PARAMETER_NAME_ON_UPDATE | name={}", request.getName());
                    throw new RuntimeException("Ya existe un parámetro con este nombre");
                }
            }
            
            // Actualizar campos
            existing.setName(request.getName());
            existing.setDisplayName(request.getDisplayName());
            existing.setDescription(request.getDescription());
            existing.setParameterType(request.getParameterType());
            existing.setUnit(request.getUnit());
            existing.setValidationRules(request.getValidationRules());
            existing.setSportId(request.getSportId());
            existing.setCategory(request.getCategory());
            
            existing.validateFormat();
            existing.logModelData("UPDATING");
            
            CustomParameterModel updated = parameterPersistencePort.save(existing);
            
            parameterLogger.logParameterUpdateSuccess(id, userEmail);
            parameterLogger.logServiceExit("updateParameter", updated.getId());
            
            return updated;
        } catch (Exception e) {
            parameterLogger.logServiceError("updateParameter", "Error updating parameter", e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public void deleteParameter(Long id, String userEmail) {
        parameterLogger.logParameterDeletionStart(id, userEmail);
        parameterLogger.logServiceEntry("deleteParameter", id, userEmail);
        
        try {
            var user = userPersistencePort.findByEmail(userEmail)
                    .orElseThrow(() -> {
                        log.error("USER_NOT_FOUND_FOR_PARAMETER_DELETION | email={}", userEmail);
                        return new RuntimeException("User not found");
                    });
            
            CustomParameterModel existing = parameterPersistencePort.findById(id)
                    .orElseThrow(() -> {
                        log.error("PARAMETER_NOT_FOUND_FOR_DELETION | id={}", id);
                        return new RuntimeException("Parameter not found");
                    });
            
            // Verificar permisos
            if (!existing.getOwnerId().equals(user.getId())) {
                log.error("UNAUTHORIZED_PARAMETER_DELETION | parameterId={} | requesterId={} | ownerId={}", 
                         id, user.getId(), existing.getOwnerId());
                throw new RuntimeException("No tiene permisos para eliminar este parámetro");
            }
            
            parameterPersistencePort.delete(id);
            
            parameterLogger.logParameterDeletionSuccess(id, userEmail);
            parameterLogger.logServiceExit("deleteParameter", "Parameter deleted successfully");
            
        } catch (Exception e) {
            parameterLogger.logServiceError("deleteParameter", "Error deleting parameter", e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public void toggleParameterStatus(Long id, String userEmail) {
        parameterLogger.logServiceEntry("toggleParameterStatus", id, userEmail);
        
        try {
            var user = userPersistencePort.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            CustomParameterModel existing = parameterPersistencePort.findById(id)
                    .orElseThrow(() -> new RuntimeException("Parameter not found"));
            
            // Verificar permisos
            if (!existing.getOwnerId().equals(user.getId())) {
                throw new RuntimeException("No tiene permisos para modificar este parámetro");
            }
            
            existing.setIsActive(!existing.getIsActive());
            parameterPersistencePort.save(existing);
            
            log.info("PARAMETER_STATUS_TOGGLED | id={} | active={}", id, existing.getIsActive());
            
        } catch (Exception e) {
            parameterLogger.logServiceError("toggleParameterStatus", "Error toggling parameter status", e);
            throw e;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        parameterLogger.logServiceEntry("getAllCategories");
        
        try {
            List<String> categories = parameterPersistencePort.findAllDistinctCategories();
            log.info("RETRIEVED_CATEGORIES | count={}", categories.size());
            return categories;
        } catch (Exception e) {
            parameterLogger.logServiceError("getAllCategories", "Error retrieving categories", e);
            throw e;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ParameterType> getAllParameterTypes() {
        parameterLogger.logServiceEntry("getAllParameterTypes");
        
        try {
            List<ParameterType> types = parameterPersistencePort.findAllDistinctParameterTypes();
            log.info("RETRIEVED_PARAMETER_TYPES | count={}", types.size());
            return types;
        } catch (Exception e) {
            parameterLogger.logServiceError("getAllParameterTypes", "Error retrieving parameter types", e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public void incrementParameterUsage(Long parameterId) {
        parameterLogger.logServiceEntry("incrementParameterUsage", parameterId);
        
        try {
            parameterPersistencePort.incrementUsageCount(parameterId);
            parameterLogger.logParameterUsageIncrement(parameterId, -1); // -1 porque no sabemos el nuevo valor
        } catch (Exception e) {
            parameterLogger.logServiceError("incrementParameterUsage", "Error incrementing usage count", e);
            // No lanzamos excepción porque no es crítico
        }
    }
    
    private Pageable createPageable(CustomParameterFilterRequest filterRequest) {
        if (filterRequest.getSortFields() != null && !filterRequest.getSortFields().isEmpty()) {
            List<Sort.Order> orders = filterRequest.getSortFields().stream()
                    .map(field -> new Sort.Order(field.getDirection(), field.getField()))
                    .collect(Collectors.toList());
            
            return PageRequest.of(
                filterRequest.getPage(),
                filterRequest.getSize(),
                Sort.by(orders)
            );
        }
        
        return PageRequest.of(
            filterRequest.getPage(),
            filterRequest.getSize(),
            Sort.by(filterRequest.getDirection(), filterRequest.getSortBy())
        );
    }
    
    private CustomParameterPageResponse buildPageResponse(Page<CustomParameterModel> page) {
        List<CustomParameterResponse> content = page.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return CustomParameterPageResponse.builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .numberOfElements(page.getNumberOfElements())
                .build();
    }
    
    private CustomParameterResponse convertToResponse(CustomParameterModel model) {
        CustomParameterResponse response = new CustomParameterResponse();
        response.setId(model.getId());
        response.setName(model.getName());
        response.setDisplayName(model.getDisplayName());
        response.setDescription(model.getDescription());
        response.setParameterType(model.getParameterType());
        response.setUnit(model.getUnit());
        response.setValidationRules(model.getValidationRules());
        response.setIsGlobal(model.getIsGlobal());
        response.setIsActive(model.getIsActive());
        response.setOwnerId(model.getOwnerId());
        response.setSportId(model.getSportId());
        response.setCategory(model.getCategory());
        response.setCreatedAt(model.getCreatedAt());
        response.setUpdatedAt(model.getUpdatedAt());
        response.setUsageCount(model.getUsageCount());
        
        return response;
    }
    
    private void validateParameterName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("El nombre del parámetro no puede estar vacío");
        }
        
        // Validar formato camelCase
        if (!name.matches("^[a-z]+([A-Z][a-z]*)*$")) {
            log.warn("PARAMETER_NAME_FORMAT_VALIDATION | name={} | format not camelCase", name);
            // No lanzamos excepción, solo log, para ser flexibles
        }
        
        // Validar longitud
        if (name.length() > 100) {
            throw new RuntimeException("El nombre del parámetro no puede exceder 100 caracteres");
        }
    }
}