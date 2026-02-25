package com.fitapp.backend.application.service;

import com.fitapp.backend.application.dto.parameter.request.CustomParameterFilterRequest;
import com.fitapp.backend.application.dto.parameter.request.CustomParameterRequest;
import com.fitapp.backend.application.dto.parameter.response.CustomParameterPageResponse;
import com.fitapp.backend.application.dto.parameter.response.CustomParameterResponse;
import com.fitapp.backend.application.logging.ParameterServiceLogger;
import com.fitapp.backend.application.ports.input.CustomParameterUseCase;
import com.fitapp.backend.application.ports.output.CustomParameterPersistencePort;
import com.fitapp.backend.application.ports.output.UserPersistencePort;
import com.fitapp.backend.domain.exception.DuplicateParameterException;
import com.fitapp.backend.domain.exception.ParameterNotFoundException;
import com.fitapp.backend.domain.exception.UnauthorizedOperationException;
import com.fitapp.backend.domain.exception.UserNotFoundException;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomParameterServiceImpl implements CustomParameterUseCase {

    private final CustomParameterPersistencePort parameterPersistencePort;
    private final UserPersistencePort userPersistencePort;
    private final ParameterServiceLogger parameterLogger;

    @Override
    @Transactional(readOnly = true)
    public CustomParameterPageResponse getAllParametersPaginated(CustomParameterFilterRequest filterRequest) {

        parameterLogger.logServiceEntry("getAllParametersPaginated", filterRequest);
        Pageable pageable = createPageable(filterRequest);
        Page<CustomParameterModel> page = parameterPersistencePort.findByFilters(filterRequest, pageable);
        parameterLogger.logParameterRetrieval("SYSTEM", page.getNumberOfElements(), "ALL_PAGINATED");
        return buildPageResponse(page);

    }

    @Override
    @Transactional(readOnly = true)
    public CustomParameterPageResponse getMyParametersPaginated(String userEmail,
            CustomParameterFilterRequest filterRequest) {

        parameterLogger.logServiceEntry("getMyParametersPaginated", userEmail, filterRequest);

        var user = userPersistencePort.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + userEmail));

        filterRequest.setOwnerId(user.getId());
        filterRequest.setOnlyMine(true);

        Pageable pageable = createPageable(filterRequest);
        Page<CustomParameterModel> page = parameterPersistencePort.findByFilters(filterRequest, pageable);

        parameterLogger.logParameterRetrieval(userEmail, page.getNumberOfElements(), "MY_PAGINATED");

        return buildPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomParameterPageResponse getAvailableParametersPaginated(String userEmail,
            CustomParameterFilterRequest filterRequest) {

        parameterLogger.logServiceEntry("getAvailableParametersPaginated", userEmail, filterRequest);

        var user = userPersistencePort.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + userEmail));

        Pageable pageable = createPageable(filterRequest);
        Page<CustomParameterModel> page = parameterPersistencePort.findAvailableForUser(user.getId(), pageable);

        parameterLogger.logParameterRetrieval(userEmail, page.getNumberOfElements(), "AVAILABLE_PAGINATED");

        return buildPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomParameterModel getParameterById(Long id) {
        parameterLogger.logServiceEntry("getParameterById", id);
        return parameterPersistencePort.findById(id)
                .orElseThrow(() -> new ParameterNotFoundException("Parámetro no encontrado con id: " + id));
    }

    @Override
    @Transactional
    public CustomParameterModel createParameter(CustomParameterRequest request, String userEmail) {
        parameterLogger.logParameterCreationStart(userEmail, request.getName());
        parameterLogger.logServiceEntry("createParameter", request, userEmail);

        request.logRequestData();

        var user = userPersistencePort.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + userEmail));

        boolean exists = parameterPersistencePort
                .findByNameAndOwnerIdAndSportId(request.getName(), user.getId())
                .isPresent();
        if (exists) {
            throw new DuplicateParameterException("Ya existe un parámetro con el nombre '" + request.getName() +
                    "' para este usuario");
        }

        validateParameterName(request.getName());

        CustomParameterModel model = new CustomParameterModel();
        model.setName(request.getName());
        model.setDescription(request.getDescription());
        model.setParameterType(request.getParameterType());
        model.setUnit(request.getUnit());
        model.setIsGlobal(request.getIsGlobal() != null ? request.getIsGlobal() : false);
        model.setIsActive(true);
        model.setFavorite(false);
        model.setOwnerId(user.getId());
        model.setUsageCount(0);

        model.validateFormat();
        model.logModelData("CREATING");

        CustomParameterModel savedModel = parameterPersistencePort.save(model);
        parameterLogger.logParameterCreationSuccess(savedModel.getId(), userEmail);
        parameterLogger.logServiceExit("createParameter", savedModel.getId());
        return savedModel;
    }

    @Override
    @Transactional
    public CustomParameterModel updateParameter(Long id, CustomParameterRequest request, String userEmail) {
        parameterLogger.logParameterUpdateStart(id, userEmail);
        parameterLogger.logServiceEntry("updateParameter", id, request, userEmail);

        var user = userPersistencePort.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + userEmail));

        CustomParameterModel existing = parameterPersistencePort.findById(id)
                .orElseThrow(() -> new ParameterNotFoundException("Parámetro no encontrado con id: " + id));

        if (!existing.getOwnerId().equals(user.getId())) {
            throw new UnauthorizedOperationException("No tiene permisos para actualizar este parámetro");
        }

        if (!existing.getName().equals(request.getName())) {
            boolean exists = parameterPersistencePort
                    .findByNameAndOwnerIdAndSportId(request.getName(), user.getId())
                    .isPresent();
            if (exists) {
                throw new DuplicateParameterException(
                        "Ya existe un parámetro con el nombre '" + request.getName() + "'");
            }
        }

        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setParameterType(request.getParameterType());
        existing.setUnit(request.getUnit());
        existing.validateFormat();
        existing.logModelData("UPDATING");

        CustomParameterModel updated = parameterPersistencePort.save(existing);
        parameterLogger.logParameterUpdateSuccess(id, userEmail);
        parameterLogger.logServiceExit("updateParameter", updated.getId());
        return updated;
    }

    @Override
    @Transactional
    public void deleteParameter(Long id, String userEmail) {
        parameterLogger.logParameterDeletionStart(id, userEmail);
        parameterLogger.logServiceEntry("deleteParameter", id, userEmail);

        var user = userPersistencePort.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + userEmail));

        CustomParameterModel existing = parameterPersistencePort.findById(id)
                .orElseThrow(() -> new ParameterNotFoundException("Parámetro no encontrado con id: " + id));

        if (!existing.getOwnerId().equals(user.getId())) {
            throw new UnauthorizedOperationException("No tiene permisos para eliminar este parámetro");
        }

        parameterPersistencePort.delete(id);
        parameterLogger.logParameterDeletionSuccess(id, userEmail);
        parameterLogger.logServiceExit("deleteParameter", "Parameter deleted successfully");
    }

    @Override
    @Transactional
    public void toggleParameterStatus(Long id, String userEmail) {
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
        log.info("PARAMETER_STATUS_TOGGLED | id={} | active={}", id, existing.getIsActive());
    }

    @Override
    @Transactional
    public void toggleFavorite(Long id, String userEmail) {
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
        log.info("PARAMETER_FAVORITE_TOGGLED | id={} | favorite={}", id, existing.isFavorite());
        parameterLogger.logServiceExit("toggleFavorite", "Favorite toggled to " + existing.isFavorite());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParameterType> getAllParameterTypes() {
        parameterLogger.logServiceEntry("getAllParameterTypes");
        List<ParameterType> types = parameterPersistencePort.findAllDistinctParameterTypes();
        log.info("RETRIEVED_PARAMETER_TYPES | count={}", types.size());
        return types;
    }

    @Override
    @Transactional
    public void incrementParameterUsage(Long parameterId) {
        parameterLogger.logServiceEntry("incrementParameterUsage", parameterId);
        parameterPersistencePort.incrementUsageCount(parameterId);
        parameterLogger.logParameterUsageIncrement(parameterId, -1);
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
        return response;
    }

    private void validateParameterName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del parámetro no puede estar vacío");
        }
        if (!name.matches("^[a-z]+([A-Z][a-z]*)*$")) {
            log.warn("PARAMETER_NAME_FORMAT_VALIDATION | name={} | format not camelCase", name);
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("El nombre del parámetro no puede exceder 100 caracteres");
        }
    }
}