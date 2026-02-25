package com.fitapp.backend.infrastructure.controller;

import com.fitapp.backend.application.dto.parameter.request.CustomParameterFilterRequest;
import com.fitapp.backend.application.dto.parameter.request.CustomParameterRequest;
import com.fitapp.backend.application.dto.parameter.response.CustomParameterPageResponse;
import com.fitapp.backend.application.dto.parameter.response.CustomParameterResponse;
import com.fitapp.backend.application.logging.ParameterServiceLogger;
import com.fitapp.backend.application.ports.input.CustomParameterUseCase;
import com.fitapp.backend.infrastructure.persistence.entity.enums.ParameterType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parameters")
@RequiredArgsConstructor
@Tag(name = "Gestión de Parámetros Personalizados", description = "Endpoints para la gestión de parámetros personalizados")
@Slf4j
public class CustomParameterController {

    private final CustomParameterUseCase customParameterUseCase;
    private final ParameterServiceLogger parameterLogger;

    @Operation(summary = "Obtener todos los parámetros (paginado con filtros)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Parámetros obtenidos exitosamente"),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping("/search")
    public ResponseEntity<CustomParameterPageResponse> getAllParameters(
            @Valid @RequestBody CustomParameterFilterRequest filterRequest,
            HttpServletRequest request) {
        setCorrelationId(request);

        log.info("CONTROLLER_GET_ALL_PARAMETERS | endpoint={} | search={} | page={}",
                request.getRequestURI(), filterRequest.getSearch(), filterRequest.getPage());

        CustomParameterPageResponse response = customParameterUseCase.getAllParametersPaginated(filterRequest);

        log.info("CONTROLLER_GET_ALL_PARAMETERS_SUCCESS | totalElements={}", response.getTotalElements());
        clearCorrelationId();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener mis parámetros personales (paginado con filtros)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Parámetros personales obtenidos exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping("/my/search")
    public ResponseEntity<CustomParameterPageResponse> getMyParameters(
            @Valid @RequestBody CustomParameterFilterRequest filterRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {
        setCorrelationId(request);

        String userEmail = jwt.getClaimAsString("email");
        log.info("CONTROLLER_GET_MY_PARAMETERS | endpoint={} | user={} | search={}",
                request.getRequestURI(), userEmail, filterRequest.getSearch());

        CustomParameterPageResponse response = customParameterUseCase.getMyParametersPaginated(userEmail,
                filterRequest);

        log.info("CONTROLLER_GET_MY_PARAMETERS_SUCCESS | user={} | totalElements={}",
                userEmail, response.getTotalElements());
        clearCorrelationId();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener parámetros disponibles para un usuario (paginado con filtros)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Parámetros disponibles obtenidos exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping("/available/{sportId}/search")
    public ResponseEntity<CustomParameterPageResponse> getAvailableParameters(
            @PathVariable Long sportId,
            @Valid @RequestBody CustomParameterFilterRequest filterRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {
        setCorrelationId(request);

        String userEmail = jwt.getClaimAsString("email");
        log.info("CONTROLLER_GET_AVAILABLE_PARAMETERS | endpoint={} | user={} | sportId={}",
                request.getRequestURI(), userEmail, sportId);

        CustomParameterPageResponse response = customParameterUseCase.getAvailableParametersPaginated(
                userEmail, filterRequest);

        log.info("CONTROLLER_GET_AVAILABLE_PARAMETERS_SUCCESS | user={} | totalElements={}",
                userEmail, response.getTotalElements());
        clearCorrelationId();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener un parámetro por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Parámetro obtenido exitosamente"),
            @ApiResponse(responseCode = "404", description = "Parámetro no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomParameterResponse> getParameterById(
            @PathVariable Long id,
            HttpServletRequest request) {
        setCorrelationId(request);

        log.info("CONTROLLER_GET_PARAMETER_BY_ID | endpoint={} | parameterId={}",
                request.getRequestURI(), id);

        CustomParameterResponse response = convertToResponse(customParameterUseCase.getParameterById(id));

        log.info("CONTROLLER_GET_PARAMETER_BY_ID_SUCCESS | parameterId={}", id);
        clearCorrelationId();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Crear un nuevo parámetro personalizado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Parámetro creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "409", description = "Parámetro duplicado")
    })
    @PostMapping
    public ResponseEntity<CustomParameterResponse> createParameter(
            @Valid @RequestBody CustomParameterRequest parameterRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {
        setCorrelationId(request);

        String userEmail = jwt.getClaimAsString("email");
        log.info("CONTROLLER_CREATE_PARAMETER | endpoint={} | user={} | parameterName={}",
                request.getRequestURI(), userEmail, parameterRequest.getName());

        logRequestData(parameterRequest, request);

        CustomParameterResponse response = convertToResponse(
                customParameterUseCase.createParameter(parameterRequest, userEmail));

        log.info("CONTROLLER_CREATE_PARAMETER_SUCCESS | parameterId={} | user={}",
                response.getId(), userEmail);
        clearCorrelationId();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Actualizar un parámetro personalizado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Parámetro actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos"),
            @ApiResponse(responseCode = "404", description = "Parámetro no encontrado"),
            @ApiResponse(responseCode = "409", description = "Parámetro duplicado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CustomParameterResponse> updateParameter(
            @PathVariable Long id,
            @Valid @RequestBody CustomParameterRequest parameterRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {
        setCorrelationId(request);

        String userEmail = jwt.getClaimAsString("email");
        log.info("CONTROLLER_UPDATE_PARAMETER | endpoint={} | user={} | parameterId={}",
                request.getRequestURI(), userEmail, id);

        CustomParameterResponse response = convertToResponse(
                customParameterUseCase.updateParameter(id, parameterRequest, userEmail));

        log.info("CONTROLLER_UPDATE_PARAMETER_SUCCESS | parameterId={} | user={}", id, userEmail);
        clearCorrelationId();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Eliminar un parámetro personalizado")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Parámetro eliminado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos"),
            @ApiResponse(responseCode = "404", description = "Parámetro no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParameter(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {
        setCorrelationId(request);

        String userEmail = jwt.getClaimAsString("email");
        log.info("CONTROLLER_DELETE_PARAMETER | endpoint={} | user={} | parameterId={}",
                request.getRequestURI(), userEmail, id);

        customParameterUseCase.deleteParameter(id, userEmail);

        log.info("CONTROLLER_DELETE_PARAMETER_SUCCESS | parameterId={} | user={}", id, userEmail);
        clearCorrelationId();

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Marcar/desmarcar un parámetro como favorito")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Estado de favorito cambiado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos"),
            @ApiResponse(responseCode = "404", description = "Parámetro no encontrado")
    })
    @PatchMapping("/{id}/favorite")
    public ResponseEntity<Void> toggleFavorite(@PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        String userEmail = jwt.getClaimAsString("email");
        customParameterUseCase.toggleFavorite(id, userEmail);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Activar/desactivar un parámetro")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Estado cambiado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos"),
            @ApiResponse(responseCode = "404", description = "Parámetro no encontrado")
    })
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Void> toggleParameterStatus(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {
        setCorrelationId(request);

        String userEmail = jwt.getClaimAsString("email");
        log.info("CONTROLLER_TOGGLE_PARAMETER_STATUS | endpoint={} | user={} | parameterId={}",
                request.getRequestURI(), userEmail, id);

        customParameterUseCase.toggleParameterStatus(id, userEmail);

        log.info("CONTROLLER_TOGGLE_PARAMETER_STATUS_SUCCESS | parameterId={}", id);
        clearCorrelationId();

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtener todos los tipos de parámetros")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tipos obtenidos exitosamente")
    })
    @GetMapping("/types")
    public ResponseEntity<List<ParameterType>> getAllParameterTypes(HttpServletRequest request) {
        setCorrelationId(request);

        log.info("CONTROLLER_GET_ALL_PARAMETER_TYPES | endpoint={}", request.getRequestURI());

        List<ParameterType> types = customParameterUseCase.getAllParameterTypes();

        log.info("CONTROLLER_GET_ALL_PARAMETER_TYPES_SUCCESS | count={}", types.size());
        clearCorrelationId();

        return ResponseEntity.ok(types);
    }

    @Operation(summary = "Incrementar contador de uso de un parámetro")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Contador incrementado")
    })
    @PostMapping("/{id}/increment-usage")
    public ResponseEntity<Void> incrementParameterUsage(
            @PathVariable Long id,
            HttpServletRequest request) {
        setCorrelationId(request);

        log.info("CONTROLLER_INCREMENT_PARAMETER_USAGE | endpoint={} | parameterId={}",
                request.getRequestURI(), id);

        customParameterUseCase.incrementParameterUsage(id);

        log.info("CONTROLLER_INCREMENT_PARAMETER_USAGE_SUCCESS | parameterId={}", id);
        clearCorrelationId();

        return ResponseEntity.noContent().build();
    }

    private void setCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader("X-Correlation-ID");
        if (correlationId != null) {
            parameterLogger.setCorrelationId(correlationId);
        } else {
            parameterLogger.getCorrelationId();
        }
    }

    private void clearCorrelationId() {
        parameterLogger.clearCorrelationId();
    }

    private void logRequestData(CustomParameterRequest request, HttpServletRequest httpRequest) {
        log.debug("PARAMETER_REQUEST_BODY | name={} | type={} | unit={}",
                request.getName(), request.getParameterType(), request.getUnit());

        String contentType = httpRequest.getContentType();
        log.debug("PARAMETER_REQUEST_FORMAT | contentType={}", contentType);
    }

    private CustomParameterResponse convertToResponse(com.fitapp.backend.domain.model.CustomParameterModel model) {
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
}