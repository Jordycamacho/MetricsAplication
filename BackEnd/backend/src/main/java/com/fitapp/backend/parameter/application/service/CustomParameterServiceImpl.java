package com.fitapp.backend.parameter.application.service;

import com.fitapp.backend.application.logging.ParameterServiceLogger;
import com.fitapp.backend.application.ports.output.UserPersistencePort;
import com.fitapp.backend.application.service.SubscriptionLimitChecker;
import com.fitapp.backend.domain.exception.DuplicateParameterException;
import com.fitapp.backend.domain.exception.ParameterNotFoundException;
import com.fitapp.backend.domain.exception.UnauthorizedOperationException;
import com.fitapp.backend.domain.exception.UserNotFoundException;
import com.fitapp.backend.infrastructure.persistence.entity.enums.MetricAggregation;
import com.fitapp.backend.parameter.application.dto.request.CustomParameterFilterRequest;
import com.fitapp.backend.parameter.application.dto.request.CustomParameterRequest;
import com.fitapp.backend.parameter.application.dto.response.CustomParameterPageResponse;
import com.fitapp.backend.parameter.application.dto.response.CustomParameterResponse;
import com.fitapp.backend.parameter.application.port.input.CustomParameterUseCase;
import com.fitapp.backend.parameter.domain.model.CustomParameterModel;
import com.fitapp.backend.parameter.infrastructure.persistence.adapter.CustomParameterPersistencePort;
import com.fitapp.backend.parameter.infrastructure.persistence.entity.ParameterType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomParameterServiceImpl implements CustomParameterUseCase {

    private final CustomParameterPersistencePort parameterPersistencePort;
    private final UserPersistencePort userPersistencePort;
    private final ParameterServiceLogger parameterLogger;
    private final SubscriptionLimitChecker limitChecker;


    @Override
    @Transactional(readOnly = true)
    public CustomParameterPageResponse getAllParametersPaginated(CustomParameterFilterRequest filterRequest) {
        StopWatch watch = new StopWatch("getAllParametersPaginated");
        watch.start();

        try {
            parameterLogger.logServiceEntry("getAllParametersPaginated", filterRequest);
            Pageable pageable = createPageable(filterRequest);
            Page<CustomParameterModel> page = parameterPersistencePort.findByFilters(filterRequest, pageable);
            
            watch.stop();
            parameterLogger.logParameterRetrieval("SYSTEM", page.getNumberOfElements(), "ALL_PAGINATED");
            log.info("SERVICE_GET_ALL_COMPLETED | totalElements={} | timeMs={}", 
                    page.getTotalElements(), watch.getTotalTimeMillis());
            
            return buildPageResponse(page);
            
        } catch (Exception e) {
            watch.stop();
            log.error("SERVICE_GET_ALL_ERROR | error={} | timeMs={}", 
                    e.getMessage(), watch.getTotalTimeMillis(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CustomParameterPageResponse getMyParametersPaginated(String userEmail,
            CustomParameterFilterRequest filterRequest) {
        StopWatch watch = new StopWatch("getMyParametersPaginated");
        watch.start();

        try {
            parameterLogger.logServiceEntry("getMyParametersPaginated", userEmail, filterRequest);

            var user = userPersistencePort.findByEmail(userEmail)
                    .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + userEmail));

            filterRequest.setOwnerId(user.getId());
            filterRequest.setOnlyMine(true);

            Pageable pageable = createPageable(filterRequest);
            Page<CustomParameterModel> page = parameterPersistencePort.findByFilters(filterRequest, pageable);

            watch.stop();
            parameterLogger.logParameterRetrieval(userEmail, page.getNumberOfElements(), "MY_PAGINATED");
            log.info("SERVICE_GET_MY_COMPLETED | user={} | totalElements={} | timeMs={}", 
                    userEmail, page.getTotalElements(), watch.getTotalTimeMillis());

            return buildPageResponse(page);
            
        } catch (Exception e) {
            watch.stop();
            log.error("SERVICE_GET_MY_ERROR | user={} | error={} | timeMs={}", 
                    userEmail, e.getMessage(), watch.getTotalTimeMillis(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CustomParameterPageResponse getAvailableParametersPaginated(String userEmail,
            CustomParameterFilterRequest filterRequest) {
        StopWatch watch = new StopWatch("getAvailableParametersPaginated");
        watch.start();

        try {
            parameterLogger.logServiceEntry("getAvailableParametersPaginated", userEmail, filterRequest);

            var user = userPersistencePort.findByEmail(userEmail)
                    .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + userEmail));

            Pageable pageable = createPageable(filterRequest);
            Page<CustomParameterModel> page = parameterPersistencePort.findAvailableForUser(user.getId(), pageable);

            watch.stop();
            parameterLogger.logParameterRetrieval(userEmail, page.getNumberOfElements(), "AVAILABLE_PAGINATED");
            log.info("SERVICE_GET_AVAILABLE_COMPLETED | user={} | totalElements={} | timeMs={}", 
                    userEmail, page.getTotalElements(), watch.getTotalTimeMillis());

            return buildPageResponse(page);
            
        } catch (Exception e) {
            watch.stop();
            log.error("SERVICE_GET_AVAILABLE_ERROR | user={} | error={} | timeMs={}", 
                    userEmail, e.getMessage(), watch.getTotalTimeMillis(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CustomParameterModel getParameterById(Long id) {
        StopWatch watch = new StopWatch("getParameterById");
        watch.start();
        
        try {
            parameterLogger.logServiceEntry("getParameterById", id);
            
            CustomParameterModel result = parameterPersistencePort.findById(id)
                    .orElseThrow(() -> new ParameterNotFoundException("Parámetro no encontrado con id: " + id));
            
            watch.stop();
            log.info("SERVICE_GET_BY_ID_COMPLETED | id={} | timeMs={}", id, watch.getTotalTimeMillis());
            
            return result;
            
        } catch (Exception e) {
            watch.stop();
            log.error("SERVICE_GET_BY_ID_ERROR | id={} | error={} | timeMs={}", 
                    id, e.getMessage(), watch.getTotalTimeMillis(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public CustomParameterModel createParameter(CustomParameterRequest request, String userEmail) {
        StopWatch watch = new StopWatch("createParameter");
        watch.start();
        
        try {
            parameterLogger.logParameterCreationStart(userEmail, request.getName());

            var user = userPersistencePort.findByEmail(userEmail)
                    .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + userEmail));

            // FIX: Verificar límite de parámetros personalizados ANTES de crear
            long currentCount = parameterPersistencePort.countByOwnerId(user.getId());
            limitChecker.checkCustomParameterLimit(userEmail, currentCount);

            // Validar duplicados
            boolean exists = parameterPersistencePort
                    .findByNameAndOwnerIdAndSportId(request.getName(), user.getId())
                    .isPresent();
            if (exists) {
                throw new DuplicateParameterException("Ya existe un parámetro con el nombre '" + request.getName() +
                        "' para este usuario");
            }

            // Construir modelo con valores por defecto inteligentes
            CustomParameterModel model = buildModelFromRequest(request, user.getId());

            // Validar formato y coherencia
            model.validateFormat();

            CustomParameterModel savedModel = parameterPersistencePort.save(model);
            
            watch.stop();
            parameterLogger.logParameterCreationSuccess(savedModel.getId(), userEmail);
            log.info("SERVICE_CREATE_COMPLETED | id={} | user={} | timeMs={}", 
                    savedModel.getId(), userEmail, watch.getTotalTimeMillis());
            
            return savedModel;
            
        } catch (Exception e) {
            watch.stop();
            log.error("SERVICE_CREATE_ERROR | user={} | name={} | error={} | timeMs={}", 
                    userEmail, request.getName(), e.getMessage(), watch.getTotalTimeMillis(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public CustomParameterModel updateParameter(Long id, CustomParameterRequest request, String userEmail) {
        StopWatch watch = new StopWatch("updateParameter");
        watch.start();
        
        try {
            parameterLogger.logParameterUpdateStart(id, userEmail);
            parameterLogger.logServiceEntry("updateParameter", id, request, userEmail);

            var user = userPersistencePort.findByEmail(userEmail)
                    .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + userEmail));

            CustomParameterModel existing = parameterPersistencePort.findById(id)
                    .orElseThrow(() -> new ParameterNotFoundException("Parámetro no encontrado con id: " + id));

            // Verificar permisos
            if (!existing.getOwnerId().equals(user.getId())) {
                throw new UnauthorizedOperationException("No tiene permisos para actualizar este parámetro");
            }

            // Validar duplicados si cambió el nombre
            if (!existing.getName().equals(request.getName())) {
                boolean exists = parameterPersistencePort
                        .findByNameAndOwnerIdAndSportId(request.getName(), user.getId())
                        .isPresent();
                if (exists) {
                    throw new DuplicateParameterException(
                            "Ya existe un parámetro con el nombre '" + request.getName() + "'");
                }
            }

            // Actualizar campos
            updateModelFromRequest(existing, request);
            existing.validateFormat();
            existing.logModelData("UPDATING");

            CustomParameterModel updated = parameterPersistencePort.save(existing);
            
            watch.stop();
            parameterLogger.logParameterUpdateSuccess(id, userEmail);
            parameterLogger.logServiceExit("updateParameter", updated.getId());
            log.info("SERVICE_UPDATE_COMPLETED | id={} | user={} | timeMs={}", 
                    id, userEmail, watch.getTotalTimeMillis());
            
            return updated;
            
        } catch (Exception e) {
            watch.stop();
            log.error("SERVICE_UPDATE_ERROR | id={} | user={} | error={} | timeMs={}", 
                    id, userEmail, e.getMessage(), watch.getTotalTimeMillis(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteParameter(Long id, String userEmail) {
        StopWatch watch = new StopWatch("deleteParameter");
        watch.start();
        
        try {
            parameterLogger.logParameterDeletionStart(id, userEmail);
            parameterLogger.logServiceEntry("deleteParameter", id, userEmail);

            var user = userPersistencePort.findByEmail(userEmail)
                    .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + userEmail));

            CustomParameterModel existing = parameterPersistencePort.findById(id)
                    .orElseThrow(() -> new ParameterNotFoundException("Parámetro no encontrado con id: " + id));

            // Verificar permisos
            if (!existing.getOwnerId().equals(user.getId())) {
                throw new UnauthorizedOperationException("No tiene permisos para eliminar este parámetro");
            }

            parameterPersistencePort.delete(id);
            
            watch.stop();
            parameterLogger.logParameterDeletionSuccess(id, userEmail);
            parameterLogger.logServiceExit("deleteParameter", "Parameter deleted successfully");
            log.info("SERVICE_DELETE_COMPLETED | id={} | user={} | timeMs={}", 
                    id, userEmail, watch.getTotalTimeMillis());
                    
        } catch (Exception e) {
            watch.stop();
            log.error("SERVICE_DELETE_ERROR | id={} | user={} | error={} | timeMs={}", 
                    id, userEmail, e.getMessage(), watch.getTotalTimeMillis(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void toggleParameterStatus(Long id, String userEmail) {
        StopWatch watch = new StopWatch("toggleParameterStatus");
        watch.start();
        
        try {
            parameterLogger.logServiceEntry("toggleParameterStatus", id, userEmail);

            var user = userPersistencePort.findByEmail(userEmail)
                    .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + userEmail));

            CustomParameterModel existing = parameterPersistencePort.findById(id)
                    .orElseThrow(() -> new ParameterNotFoundException("Parámetro no encontrado con id: " + id));

            if (!existing.getOwnerId().equals(user.getId())) {
                throw new UnauthorizedOperationException("No tiene permisos para modificar el estado de este parámetro");
            }

            existing.setIsActive(!existing.getIsActive());
            parameterPersistencePort.save(existing);
            
            watch.stop();
            log.info("PARAMETER_STATUS_TOGGLED | id={} | active={} | timeMs={}", 
                    id, existing.getIsActive(), watch.getTotalTimeMillis());
                    
        } catch (Exception e) {
            watch.stop();
            log.error("SERVICE_TOGGLE_STATUS_ERROR | id={} | user={} | error={} | timeMs={}", 
                    id, userEmail, e.getMessage(), watch.getTotalTimeMillis(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void toggleFavorite(Long id, String userEmail) {
        StopWatch watch = new StopWatch("toggleFavorite");
        watch.start();
        
        try {
            parameterLogger.logServiceEntry("toggleFavorite", id, userEmail);

            var user = userPersistencePort.findByEmail(userEmail)
                    .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + userEmail));

            CustomParameterModel existing = parameterPersistencePort.findById(id)
                    .orElseThrow(() -> new ParameterNotFoundException("Parámetro no encontrado con id: " + id));

            if (!existing.getOwnerId().equals(user.getId())) {
                throw new UnauthorizedOperationException("No tiene permisos para marcar como favorito este parámetro");
            }

            existing.setFavorite(!existing.isFavorite());
            parameterPersistencePort.save(existing);
            
            watch.stop();
            log.info("PARAMETER_FAVORITE_TOGGLED | id={} | favorite={} | timeMs={}", 
                    id, existing.isFavorite(), watch.getTotalTimeMillis());
            parameterLogger.logServiceExit("toggleFavorite", "Favorite toggled to " + existing.isFavorite());
            
        } catch (Exception e) {
            watch.stop();
            log.error("SERVICE_TOGGLE_FAVORITE_ERROR | id={} | user={} | error={} | timeMs={}", 
                    id, userEmail, e.getMessage(), watch.getTotalTimeMillis(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParameterType> getAllParameterTypes() {
        StopWatch watch = new StopWatch("getAllParameterTypes");
        watch.start();
        
        try {
            parameterLogger.logServiceEntry("getAllParameterTypes");
            List<ParameterType> types = parameterPersistencePort.findAllDistinctParameterTypes();
            
            watch.stop();
            log.info("RETRIEVED_PARAMETER_TYPES | count={} | timeMs={}", 
                    types.size(), watch.getTotalTimeMillis());
            
            return types;
            
        } catch (Exception e) {
            watch.stop();
            log.error("SERVICE_GET_TYPES_ERROR | error={} | timeMs={}", 
                    e.getMessage(), watch.getTotalTimeMillis(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void incrementParameterUsage(Long parameterId) {
        StopWatch watch = new StopWatch("incrementParameterUsage");
        watch.start();
        
        try {
            parameterLogger.logServiceEntry("incrementParameterUsage", parameterId);
            parameterPersistencePort.incrementUsageCount(parameterId);
            
            watch.stop();
            parameterLogger.logParameterUsageIncrement(parameterId, -1);
            log.info("SERVICE_INCREMENT_USAGE_COMPLETED | parameterId={} | timeMs={}", 
                    parameterId, watch.getTotalTimeMillis());
                    
        } catch (Exception e) {
            watch.stop();
            log.error("SERVICE_INCREMENT_USAGE_ERROR | parameterId={} | error={} | timeMs={}", 
                    parameterId, e.getMessage(), watch.getTotalTimeMillis(), e);
            throw e;
        }
    }

    // =========================== Métodos privados ===========================

    private Pageable createPageable(CustomParameterFilterRequest filterRequest) {
        if (filterRequest.getSortFields() != null && !filterRequest.getSortFields().isEmpty()) {
            List<Sort.Order> orders = filterRequest.getSortFields().stream()
                    .map(field -> new Sort.Order(field.getDirection(), field.getField()))
                    .collect(Collectors.toList());
            return PageRequest.of(filterRequest.getPage(), filterRequest.getSize(), Sort.by(orders));
        }
        return PageRequest.of(
                filterRequest.getPage(),
                filterRequest.getSize(),
                Sort.by(filterRequest.getDirection(), filterRequest.getSortBy()));
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
        response.setDescription(model.getDescription());
        response.setParameterType(model.getParameterType());
        response.setUnit(model.getUnit());
        response.setIsGlobal(model.getIsGlobal());
        response.setIsActive(model.getIsActive());
        response.setOwnerId(model.getOwnerId());
        response.setCreatedAt(model.getCreatedAt());
        response.setUpdatedAt(model.getUpdatedAt());
        response.setUsageCount(model.getUsageCount());
        response.setIsFavorite(model.isFavorite());
        // Campos v2
        response.setMetricAggregation(model.getMetricAggregation());
        response.setIsTrackable(model.isTrackable());
        return response;
    }

    /**
     * Construye un modelo desde el request con valores por defecto inteligentes
     */
    private CustomParameterModel buildModelFromRequest(CustomParameterRequest request, Long ownerId) {
        CustomParameterModel model = new CustomParameterModel();
        model.setName(request.getName());
        model.setDescription(request.getDescription());
        model.setParameterType(request.getParameterType());
        model.setUnit(normalizeUnit(request.getUnit(), request.getParameterType()));
        model.setIsGlobal(request.getIsGlobal() != null ? request.getIsGlobal() : false);
        model.setIsActive(true);
        model.setFavorite(request.getIsFavorite() != null ? request.getIsFavorite() : false);
        model.setOwnerId(ownerId);
        model.setUsageCount(0);
        
        // Campos v2 con valores por defecto inteligentes
        model.setTrackable(request.getIsTrackable() != null ? request.getIsTrackable() : true);
        model.setMetricAggregation(determineDefaultAggregation(request));
        
        return model;
    }

    /**
     * Actualiza un modelo existente desde el request
     */
    private void updateModelFromRequest(CustomParameterModel model, CustomParameterRequest request) {
        model.setName(request.getName());
        model.setDescription(request.getDescription());
        model.setParameterType(request.getParameterType());
        model.setUnit(normalizeUnit(request.getUnit(), request.getParameterType()));
        
        // Campos v2
        if (request.getIsTrackable() != null) {
            model.setTrackable(request.getIsTrackable());
        }
        
        // Solo actualizar aggregation si viene en el request o si cambió el tipo
        if (request.getMetricAggregation() != null) {
            model.setMetricAggregation(request.getMetricAggregation());
        } else if (model.isTrackable() && model.getMetricAggregation() == null) {
            model.setMetricAggregation(determineDefaultAggregation(request));
        }
    }

    /**
     * Normaliza la unidad según el tipo de parámetro
     */
    private String normalizeUnit(String unit, ParameterType type) {
        if (type == ParameterType.PERCENTAGE) {
            return "%";
        }
        if (type == ParameterType.BOOLEAN) {
            return null;
        }
        return unit;
    }

    /**
     * Determina la agregación por defecto según el tipo de parámetro
     */
    private MetricAggregation determineDefaultAggregation(CustomParameterRequest request) {
        if (request.getMetricAggregation() != null) {
            return request.getMetricAggregation();
        }
        
        if (Boolean.FALSE.equals(request.getIsTrackable())) {
            return null;
        }
        
        // Valores por defecto según tipo
        switch (request.getParameterType()) {
            case NUMBER:
            case INTEGER:
                return MetricAggregation.MAX; // Peso, altura, etc
            case DURATION:
                return MetricAggregation.MIN; // Mejor tiempo
            case DISTANCE:
                return MetricAggregation.MAX; // Mayor distancia
            case PERCENTAGE:
                return MetricAggregation.AVG; // Porcentaje promedio
            case BOOLEAN:
            case TEXT:
            default:
                return null; // No trackeable
        }
    }
}