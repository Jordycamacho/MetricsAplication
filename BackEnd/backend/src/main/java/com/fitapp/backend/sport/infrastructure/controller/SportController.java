package com.fitapp.backend.sport.infrastructure.controller;

import com.fitapp.backend.application.logging.SportServiceLogger;
import com.fitapp.backend.sport.aplication.dto.request.SportFilterRequest;
import com.fitapp.backend.sport.aplication.dto.request.SportRequest;
import com.fitapp.backend.sport.aplication.dto.response.SportPageResponse;
import com.fitapp.backend.sport.aplication.dto.response.SportResponse;
import com.fitapp.backend.sport.aplication.port.input.SportUseCase;
import com.fitapp.backend.sport.domain.model.SportModel;

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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sports")
@RequiredArgsConstructor
@Tag(name = "Gestión de Deportes", description = "Endpoints para la gestión de deportes")
@Slf4j
public class SportController {
    private final SportUseCase sportService;
    private final SportServiceLogger sportLogger;

    @Operation(summary = "Obtener todos los deportes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de deportes obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping
    public ResponseEntity<List<SportResponse>> getAllSports(HttpServletRequest request) {
        setCorrelationId(request);
        log.info("CONTROLLER_GET_ALL_SPORTS | endpoint={}", request.getRequestURI());

        List<SportModel> sports = sportService.getAllSports();
        List<SportResponse> responses = convertToResponseList(sports);

        log.info("CONTROLLER_GET_ALL_SPORTS_SUCCESS | count={}", responses.size());
        clearCorrelationId();

        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Obtener deportes predefinidos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deportes predefinidos obtenidos exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/predefined")
    public ResponseEntity<List<SportResponse>> getPredefinedSports(HttpServletRequest request) {
        setCorrelationId(request);
        log.info("CONTROLLER_GET_PREDEFINED_SPORTS | endpoint={}", request.getRequestURI());

        List<SportModel> sports = sportService.getPredefinedSports();
        List<SportResponse> responses = convertToResponseList(sports);

        log.info("CONTROLLER_GET_PREDEFINED_SPORTS_SUCCESS | count={}", responses.size());
        clearCorrelationId();

        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Obtener deportes personalizados del usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deportes personales obtenidos exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/custom")
    public ResponseEntity<List<SportResponse>> getUserSports(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {
        setCorrelationId(request);
        String userEmail = jwt.getClaimAsString("email");

        log.info("CONTROLLER_GET_USER_SPORTS | endpoint={} | user={}",
                request.getRequestURI(), userEmail);

        List<SportModel> sports = sportService.getUserSports(userEmail);
        List<SportResponse> responses = convertToResponseList(sports);

        log.info("CONTROLLER_GET_USER_SPORTS_SUCCESS | user={} | count={}",
                userEmail, responses.size());
        clearCorrelationId();

        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Crear un deporte personalizado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deporte creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping("/custom")
    public ResponseEntity<SportResponse> createCustomSport(
            @Valid @RequestBody SportRequest sportRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {
        setCorrelationId(request);
        String userEmail = jwt.getClaimAsString("email");

        log.info("CONTROLLER_CREATE_CUSTOM_SPORT | endpoint={} | user={} | sportName={}",
                request.getRequestURI(), userEmail, sportRequest.getName());

        logRequestData(sportRequest, request);

        SportModel createdSport = sportService.createCustomSport(sportRequest, userEmail);
        SportResponse response = convertToResponse(createdSport);

        log.info("CONTROLLER_CREATE_CUSTOM_SPORT_SUCCESS | sportId={} | user={}",
                response.getId(), userEmail);
        clearCorrelationId();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Eliminar un deporte personalizado")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deporte eliminado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos"),
            @ApiResponse(responseCode = "404", description = "Deporte no encontrado")
    })
    @DeleteMapping("/custom/{id}")
    public ResponseEntity<Void> deleteCustomSport(
            @Parameter(description = "ID del deporte a eliminar") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {
        setCorrelationId(request);
        String userEmail = jwt.getClaimAsString("email");

        log.info("CONTROLLER_DELETE_CUSTOM_SPORT | endpoint={} | user={} | sportId={}",
                request.getRequestURI(), userEmail, id);

        sportService.deleteCustomSport(id, userEmail);

        log.info("CONTROLLER_DELETE_CUSTOM_SPORT_SUCCESS | sportId={} | user={}", id, userEmail);
        clearCorrelationId();

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtener deportes paginados con filtros")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deportes obtenidos exitosamente"),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping("/search")
    public ResponseEntity<SportPageResponse> searchSports(
            @Valid @RequestBody SportFilterRequest filterRequest,
            HttpServletRequest request) {
        setCorrelationId(request);

        log.info("CONTROLLER_SEARCH_SPORTS | endpoint={} | search={} | page={}",
                request.getRequestURI(), filterRequest.getSearch(), filterRequest.getPage());

        SportPageResponse response = sportService.getAllSportsPaginated(filterRequest);

        log.info("CONTROLLER_SEARCH_SPORTS_SUCCESS | totalElements={} | totalPages={}",
                response.getTotalElements(), response.getTotalPages());

        clearCorrelationId();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener deportes predefinidos paginados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deportes predefinidos obtenidos exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping("/predefined/search")
    public ResponseEntity<SportPageResponse> searchPredefinedSports(
            @Valid @RequestBody SportFilterRequest filterRequest,
            HttpServletRequest request) {
        setCorrelationId(request);

        log.info("CONTROLLER_SEARCH_PREDEFINED_SPORTS | endpoint={} | search={}",
                request.getRequestURI(), filterRequest.getSearch());

        SportPageResponse response = sportService.getPredefinedSportsPaginated(filterRequest);

        log.info("CONTROLLER_SEARCH_PREDEFINED_SPORTS_SUCCESS | totalElements={}",
                response.getTotalElements());

        clearCorrelationId();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener deportes personales paginados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deportes personales obtenidos exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping("/custom/search")
    public ResponseEntity<SportPageResponse> searchUserSports(
            @Valid @RequestBody SportFilterRequest filterRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {
        setCorrelationId(request);

        String userEmail = jwt.getClaimAsString("email");
        log.info("CONTROLLER_SEARCH_USER_SPORTS | endpoint={} | user={} | search={}",
                request.getRequestURI(), userEmail, filterRequest.getSearch());

        SportPageResponse response = sportService.getUserSportsPaginated(userEmail, filterRequest);

        log.info("CONTROLLER_SEARCH_USER_SPORTS_SUCCESS | user={} | totalElements={}",
                userEmail, response.getTotalElements());

        clearCorrelationId();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Búsqueda rápida de deportes (GET con parámetros)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deportes obtenidos exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/quick-search")
    public ResponseEntity<SportPageResponse> quickSearch(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction,
            HttpServletRequest request) {
        setCorrelationId(request);

        log.info("CONTROLLER_QUICK_SEARCH | endpoint={} | search={} | category={} | page={}",
                request.getRequestURI(), search, page);

        SportFilterRequest filterRequest = new SportFilterRequest();
        filterRequest.setSearch(search);
        filterRequest.setPage(page);
        filterRequest.setSize(size);
        filterRequest.setSortBy(sortBy);
        filterRequest.setDirection(org.springframework.data.domain.Sort.Direction.valueOf(direction));

        SportPageResponse response = sportService.getAllSportsPaginated(filterRequest);

        log.info("CONTROLLER_QUICK_SEARCH_SUCCESS | totalElements={}", response.getTotalElements());

        clearCorrelationId();
        return ResponseEntity.ok(response);
    }

    private List<SportResponse> convertToResponseList(List<SportModel> sports) {
        return sports.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private SportResponse convertToResponse(SportModel model) {
        SportResponse response = new SportResponse();
        response.setId(model.getId());
        response.setName(model.getName());
        response.setIsPredefined(model.getIsPredefined());
        response.setSourceType(model.getSourceType());
        response.setParameterTemplate(model.getParameterTemplate());
        return response;
    }

    private void setCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader("X-Correlation-ID");
        if (correlationId != null) {
            sportLogger.setCorrelationId(correlationId);
        } else {
            sportLogger.getCorrelationId(); // Genera uno nuevo
        }
    }

    private void clearCorrelationId() {
        sportLogger.clearCorrelationId();
    }

    private void logRequestData(SportRequest request, HttpServletRequest httpRequest) {
        log.debug("REQUEST_BODY_DATA | name={} | sourceType={}",
                request.getName(), request.getSourceType());

        String contentType = httpRequest.getContentType();
        log.debug("REQUEST_FORMAT | contentType={} | method={}",
                contentType, httpRequest.getMethod());

        if (request.getParameterTemplate() != null) {
            log.debug("REQUEST_PARAMETER_TEMPLATE | size={}",
                    request.getParameterTemplate().size());
        }
    }
}