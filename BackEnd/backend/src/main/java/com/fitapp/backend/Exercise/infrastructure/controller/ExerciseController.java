package com.fitapp.backend.Exercise.infrastructure.controller;

import com.fitapp.backend.Exercise.aplication.dto.request.ExerciseFilterRequest;
import com.fitapp.backend.Exercise.aplication.dto.request.ExerciseRequest;
import com.fitapp.backend.Exercise.aplication.dto.response.ExercisePageResponse;
import com.fitapp.backend.Exercise.aplication.dto.response.ExerciseResponse;
import com.fitapp.backend.Exercise.aplication.logging.ExerciseLogger;
import com.fitapp.backend.Exercise.aplication.port.input.ExerciseUseCase;
import com.fitapp.backend.Exercise.domain.model.ExerciseModel;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
@Tag(name = "Gestión de Ejercicios", description = "Endpoints para la gestión de ejercicios personalizados")
@Slf4j
public class ExerciseController {

    private final ExerciseUseCase exerciseUseCase;
    private final ExerciseLogger exerciseLogger;

    @Operation(summary = "Obtener todos los ejercicios (paginado con filtros)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ejercicios obtenidos exitosamente"),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping("/search")
    public ResponseEntity<ExercisePageResponse> getAllExercises(
            @Valid @RequestBody ExerciseFilterRequest filterRequest,
            HttpServletRequest request) {
        setCorrelationId(request);

        log.info("CONTROLLER_GET_ALL_EXERCISES | endpoint={} | search={} | page={}",
                request.getRequestURI(), filterRequest.getSearch(), filterRequest.getPage());

        ExercisePageResponse response = exerciseUseCase.getAllExercisesPaginated(filterRequest);

        log.info("CONTROLLER_GET_ALL_EXERCISES_SUCCESS | totalElements={}", response.getTotalElements());
        clearCorrelationId();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener mis ejercicios personales (paginado con filtros)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ejercicios personales obtenidos exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping("/my/search")
    public ResponseEntity<ExercisePageResponse> getMyExercises(
            @Valid @RequestBody ExerciseFilterRequest filterRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {
        setCorrelationId(request);

        String userEmail = jwt.getClaimAsString("email");
        log.info("CONTROLLER_GET_MY_EXERCISES | endpoint={} | user={} | search={}",
                request.getRequestURI(), userEmail, filterRequest.getSearch());

        ExercisePageResponse response = exerciseUseCase.getMyExercisesPaginated(userEmail, filterRequest);

        log.info("CONTROLLER_GET_MY_EXERCISES_SUCCESS | user={} | totalElements={}",
                userEmail, response.getTotalElements());
        clearCorrelationId();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener ejercicios disponibles para un usuario (paginado con filtros)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ejercicios disponibles obtenidos exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping("/available/search")
    public ResponseEntity<ExercisePageResponse> getAvailableExercises(
            @Valid @RequestBody ExerciseFilterRequest filterRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {
        setCorrelationId(request);

        String userEmail = jwt.getClaimAsString("email");
        log.info("CONTROLLER_GET_AVAILABLE_EXERCISES | endpoint={} | user={} | search={}",
                request.getRequestURI(), userEmail, filterRequest.getSearch());

        ExercisePageResponse response = exerciseUseCase.getAvailableExercisesPaginated(userEmail, filterRequest);

        log.info("CONTROLLER_GET_AVAILABLE_EXERCISES_SUCCESS | user={} | totalElements={}",
                userEmail, response.getTotalElements());
        clearCorrelationId();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener ejercicios por deporte (paginado con filtros)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ejercicios obtenidos exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping("/sport/{sportId}/search")
    public ResponseEntity<ExercisePageResponse> getExercisesBySport(
            @PathVariable Long sportId,
            @Valid @RequestBody ExerciseFilterRequest filterRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {
        setCorrelationId(request);

        String userEmail = jwt.getClaimAsString("email");
        log.info("CONTROLLER_GET_EXERCISES_BY_SPORT | endpoint={} | user={} | sportId={}",
                request.getRequestURI(), userEmail, sportId);

        ExercisePageResponse response = exerciseUseCase.getExercisesBySport(userEmail, sportId, filterRequest);

        log.info("CONTROLLER_GET_EXERCISES_BY_SPORT_SUCCESS | user={} | sportId={} | totalElements={}",
                userEmail, sportId, response.getTotalElements());
        clearCorrelationId();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener un ejercicio por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ejercicio obtenido exitosamente"),
            @ApiResponse(responseCode = "404", description = "Ejercicio no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ExerciseResponse> getExerciseById(
            @PathVariable Long id,
            HttpServletRequest request) {
        setCorrelationId(request);

        log.info("CONTROLLER_GET_EXERCISE_BY_ID | endpoint={} | exerciseId={}",
                request.getRequestURI(), id);

        ExerciseResponse response = convertToResponse(exerciseUseCase.getExerciseById(id));

        log.info("CONTROLLER_GET_EXERCISE_BY_ID_SUCCESS | exerciseId={}", id);
        clearCorrelationId();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Crear un nuevo ejercicio")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ejercicio creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "409", description = "Ejercicio duplicado")
    })
    @PostMapping
    public ResponseEntity<ExerciseResponse> createExercise(
            @Valid @RequestBody ExerciseRequest exerciseRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {
        setCorrelationId(request);

        String userEmail = jwt.getClaimAsString("email");
        log.info("CONTROLLER_CREATE_EXERCISE | endpoint={} | user={} | exerciseName={}",
                request.getRequestURI(), userEmail, exerciseRequest.getName());

        logRequestData(exerciseRequest, request);

        ExerciseResponse response = convertToResponse(
                exerciseUseCase.createExercise(exerciseRequest, userEmail));

        log.info("CONTROLLER_CREATE_EXERCISE_SUCCESS | exerciseId={} | user={}",
                response.getId(), userEmail);
        clearCorrelationId();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Actualizar un ejercicio")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ejercicio actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos"),
            @ApiResponse(responseCode = "404", description = "Ejercicio no encontrado"),
            @ApiResponse(responseCode = "409", description = "Ejercicio duplicado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ExerciseResponse> updateExercise(
            @PathVariable Long id,
            @Valid @RequestBody ExerciseRequest exerciseRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {
        setCorrelationId(request);

        String userEmail = jwt.getClaimAsString("email");
        log.info("CONTROLLER_UPDATE_EXERCISE | endpoint={} | user={} | exerciseId={}",
                request.getRequestURI(), userEmail, id);

        ExerciseResponse response = convertToResponse(
                exerciseUseCase.updateExercise(id, exerciseRequest, userEmail));

        log.info("CONTROLLER_UPDATE_EXERCISE_SUCCESS | exerciseId={} | user={}", id, userEmail);
        clearCorrelationId();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Eliminar un ejercicio")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ejercicio eliminado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos"),
            @ApiResponse(responseCode = "404", description = "Ejercicio no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExercise(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {
        setCorrelationId(request);

        String userEmail = jwt.getClaimAsString("email");
        log.info("CONTROLLER_DELETE_EXERCISE | endpoint={} | user={} | exerciseId={}",
                request.getRequestURI(), userEmail, id);

        exerciseUseCase.deleteExercise(id, userEmail);

        log.info("CONTROLLER_DELETE_EXERCISE_SUCCESS | exerciseId={} | user={}", id, userEmail);
        clearCorrelationId();

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Activar/Desactivar un ejercicio")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Estado cambiado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos"),
            @ApiResponse(responseCode = "404", description = "Ejercicio no encontrado")
    })
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<Void> toggleExerciseStatus(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {
        setCorrelationId(request);

        String userEmail = jwt.getClaimAsString("email");
        log.info("CONTROLLER_TOGGLE_EXERCISE_STATUS | endpoint={} | user={} | exerciseId={}",
                request.getRequestURI(), userEmail, id);

        exerciseUseCase.toggleExerciseStatus(id, userEmail);

        log.info("CONTROLLER_TOGGLE_EXERCISE_STATUS_SUCCESS | exerciseId={} | user={}", id, userEmail);
        clearCorrelationId();

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Incrementar contador de uso de un ejercicio")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Contador incrementado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Ejercicio no encontrado")
    })
    @PatchMapping("/{id}/increment-usage")
    public ResponseEntity<Void> incrementExerciseUsage(
            @PathVariable Long id,
            HttpServletRequest request) {
        setCorrelationId(request);

        log.info("CONTROLLER_INCREMENT_EXERCISE_USAGE | endpoint={} | exerciseId={}",
                request.getRequestURI(), id);

        exerciseUseCase.incrementExerciseUsage(id);

        log.info("CONTROLLER_INCREMENT_EXERCISE_USAGE_SUCCESS | exerciseId={}", id);
        clearCorrelationId();

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Calificar un ejercicio")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ejercicio calificado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Calificación inválida"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Ejercicio no encontrado")
    })
    @PostMapping("/{id}/rate")
    public ResponseEntity<Void> rateExercise(
            @PathVariable Long id,
            @RequestParam Double rating,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {
        setCorrelationId(request);

        String userEmail = jwt.getClaimAsString("email");
        log.info("CONTROLLER_RATE_EXERCISE | endpoint={} | user={} | exerciseId={} | rating={}",
                request.getRequestURI(), userEmail, id, rating);

        exerciseUseCase.rateExercise(id, rating, userEmail);

        log.info("CONTROLLER_RATE_EXERCISE_SUCCESS | exerciseId={} | rating={}", id, rating);
        clearCorrelationId();

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Duplicar un ejercicio")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ejercicio duplicado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Ejercicio no encontrado")
    })
    @PostMapping("/{id}/duplicate")
    public ResponseEntity<ExerciseResponse> duplicateExercise(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {
        setCorrelationId(request);

        String userEmail = jwt.getClaimAsString("email");
        log.info("CONTROLLER_DUPLICATE_EXERCISE | endpoint={} | user={} | exerciseId={}",
                request.getRequestURI(), userEmail, id);

        ExerciseResponse response = convertToResponse(
                exerciseUseCase.duplicateExercise(id, userEmail));

        log.info("CONTROLLER_DUPLICATE_EXERCISE_SUCCESS | originalId={} | copyId={}", id, response.getId());
        clearCorrelationId();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Hacer público un ejercicio")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ejercicio hecho público exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos"),
            @ApiResponse(responseCode = "404", description = "Ejercicio no encontrado")
    })
    @PatchMapping("/{id}/make-public")
    public ResponseEntity<ExerciseResponse> makeExercisePublic(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {
        setCorrelationId(request);

        String userEmail = jwt.getClaimAsString("email");
        log.info("CONTROLLER_MAKE_EXERCISE_PUBLIC | endpoint={} | user={} | exerciseId={}",
                request.getRequestURI(), userEmail, id);

        ExerciseResponse response = convertToResponse(
                exerciseUseCase.makeExercisePublic(id, userEmail));

        log.info("CONTROLLER_MAKE_EXERCISE_PUBLIC_SUCCESS | exerciseId={}", id);
        clearCorrelationId();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener ejercicios recientemente usados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ejercicios obtenidos exitosamente")
    })
    @GetMapping("/recently-used")
    public ResponseEntity<ExercisePageResponse> getRecentlyUsedExercises(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        setCorrelationId(request);

        log.info("CONTROLLER_GET_RECENTLY_USED_EXERCISES | endpoint={} | page={} | size={}",
                request.getRequestURI(), page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("lastUsedAt").descending());
        ExercisePageResponse response = exerciseUseCase.getRecentlyUsedExercises("SYSTEM", pageable);

        log.info("CONTROLLER_GET_RECENTLY_USED_EXERCISES_SUCCESS | totalElements={}",
                response.getTotalElements());
        clearCorrelationId();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener ejercicios más populares")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ejercicios obtenidos exitosamente")
    })
    @GetMapping("/most-popular")
    public ResponseEntity<ExercisePageResponse> getMostPopularExercises(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        setCorrelationId(request);

        log.info("CONTROLLER_GET_MOST_POPULAR_EXERCISES | endpoint={} | page={} | size={}",
                request.getRequestURI(), page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("usageCount").descending());
        ExercisePageResponse response = exerciseUseCase.getMostPopularExercises(pageable);

        log.info("CONTROLLER_GET_MOST_POPULAR_EXERCISES_SUCCESS | totalElements={}",
                response.getTotalElements());
        clearCorrelationId();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener ejercicios mejor calificados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ejercicios obtenidos exitosamente")
    })
    @GetMapping("/top-rated")
    public ResponseEntity<ExercisePageResponse> getTopRatedExercises(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        setCorrelationId(request);

        log.info("CONTROLLER_GET_TOP_RATED_EXERCISES | endpoint={} | page={} | size={}",
                request.getRequestURI(), page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("rating").descending());
        ExercisePageResponse response = exerciseUseCase.getTopRatedExercises(pageable);

        log.info("CONTROLLER_GET_TOP_RATED_EXERCISES_SUCCESS | totalElements={}",
                response.getTotalElements());
        clearCorrelationId();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener contador de ejercicios del usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contador obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/count/my")
    public ResponseEntity<Long> getMyExerciseCount(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {
        setCorrelationId(request);

        String userEmail = jwt.getClaimAsString("email");
        log.info("CONTROLLER_GET_MY_EXERCISE_COUNT | endpoint={} | user={}",
                request.getRequestURI(), userEmail);

        Long count = exerciseUseCase.getUserExerciseCount(userEmail);

        log.info("CONTROLLER_GET_MY_EXERCISE_COUNT_SUCCESS | user={} | count={}", userEmail, count);
        clearCorrelationId();

        return ResponseEntity.ok(count);
    }

    // Métodos auxiliares
    private void setCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader("X-Correlation-ID");
        if (correlationId != null && !correlationId.isEmpty()) {
            exerciseLogger.setCorrelationId(correlationId);
        }
    }

    private void clearCorrelationId() {
        exerciseLogger.clearCorrelationId();
    }

    private void logRequestData(ExerciseRequest request, HttpServletRequest httpRequest) {
        log.debug("EXERCISE_REQUEST_DETAILS | name={} | type={} | isPublic={} | categories={} | parameters={}",
                request.getName(), request.getExerciseType(),
                request.getIsPublic(), request.getCategoryIds().size(),
                request.getSupportedParameterIds().size());
    }

    private ExerciseResponse convertToResponse(ExerciseModel model) {
        ExerciseResponse response = new ExerciseResponse();
        response.setId(model.getId());
        response.setName(model.getName());
        response.setDescription(model.getDescription());
        response.setExerciseType(model.getExerciseType());
        response.setSports(model.getSports());
        response.setCreatedById(model.getCreatedById());
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