package com.fitapp.backend.category.infrastructure.controller;

import com.fitapp.backend.category.aplication.dto.request.ExerciseCategoryFilterRequest;
import com.fitapp.backend.category.aplication.dto.request.ExerciseCategoryRequest;
import com.fitapp.backend.category.aplication.dto.response.ExerciseCategoryPageResponse;
import com.fitapp.backend.category.aplication.dto.response.ExerciseCategoryResponse;
import com.fitapp.backend.category.aplication.port.input.ExerciseCategoryUseCase;
import com.fitapp.backend.category.domain.model.ExerciseCategoryModel;

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

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Gestión de Categorías de Ejercicios", description = "Endpoints para la gestión de categorías de ejercicios")
@Slf4j
public class ExerciseCategoryController {

    private final ExerciseCategoryUseCase categoryUseCase;

    @Operation(summary = "Obtener todas las categorías (paginado con filtros)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categorías obtenidas exitosamente"),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping("/search")
    public ResponseEntity<ExerciseCategoryPageResponse> getAllCategories(
            @Valid @RequestBody ExerciseCategoryFilterRequest filterRequest,
            HttpServletRequest request) {

        log.info("CONTROLLER_GET_ALL_CATEGORIES | endpoint={} | search={}",
                request.getRequestURI(), filterRequest.getSearch());

        ExerciseCategoryPageResponse response = categoryUseCase.getAllCategoriesPaginated(filterRequest);

        log.info("CONTROLLER_GET_ALL_CATEGORIES_SUCCESS | totalElements={}", response.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener mis categorías personales (paginado con filtros)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categorías personales obtenidas exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping("/my/search")
    public ResponseEntity<ExerciseCategoryPageResponse> getMyCategories(
            @Valid @RequestBody ExerciseCategoryFilterRequest filterRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {

        String userEmail = jwt.getClaimAsString("email");
        log.info("CONTROLLER_GET_MY_CATEGORIES | user={} | search={}", userEmail, filterRequest.getSearch());

        ExerciseCategoryPageResponse response = categoryUseCase.getMyCategoriesPaginated(userEmail, filterRequest);

        log.info("CONTROLLER_GET_MY_CATEGORIES_SUCCESS | user={} | totalElements={}",
                userEmail, response.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener categorías disponibles para un usuario (paginado)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categorías disponibles obtenidas exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping("/available/{sportId}/search")
    public ResponseEntity<ExerciseCategoryPageResponse> getAvailableCategories(
            @PathVariable Long sportId,
            @Valid @RequestBody ExerciseCategoryFilterRequest filterRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {

        String userEmail = jwt.getClaimAsString("email");
        log.info("CONTROLLER_GET_AVAILABLE_CATEGORIES | user={} | sportId={}", userEmail, sportId);

        ExerciseCategoryPageResponse response = categoryUseCase.getAvailableCategoriesPaginated(
                userEmail, sportId, filterRequest);

        log.info("CONTROLLER_GET_AVAILABLE_CATEGORIES_SUCCESS | user={} | totalElements={}",
                userEmail, response.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Crear una nueva categoría personal")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "409", description = "Nombre duplicado")
    })
    @PostMapping
    public ResponseEntity<ExerciseCategoryResponse> createCategory(
            @Valid @RequestBody ExerciseCategoryRequest categoryRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {

        String userEmail = jwt.getClaimAsString("email");
        log.info("CONTROLLER_CREATE_CATEGORY | user={} | name={}", userEmail, categoryRequest.getName());

        ExerciseCategoryResponse response = convertToResponse(
                categoryUseCase.createCategory(categoryRequest, userEmail));

        log.info("CONTROLLER_CREATE_CATEGORY_SUCCESS | id={} | user={}", response.getId(), userEmail);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener una categoría por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ExerciseCategoryResponse> getCategoryById(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {

        String userEmail = jwt.getClaimAsString("email");
        log.info("CONTROLLER_GET_CATEGORY_BY_ID | user={} | categoryId={}", userEmail, id);

        ExerciseCategoryResponse response = convertToResponse(
                categoryUseCase.getCategoryById(id, userEmail));

        log.info("CONTROLLER_GET_CATEGORY_BY_ID_SUCCESS | id={} | user={}", id, userEmail);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Actualizar una categoría personal")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ExerciseCategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody ExerciseCategoryRequest categoryRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {

        String userEmail = jwt.getClaimAsString("email");
        log.info("CONTROLLER_UPDATE_CATEGORY | user={} | categoryId={}", userEmail, id);

        ExerciseCategoryResponse response = convertToResponse(
                categoryUseCase.updateCategory(id, categoryRequest, userEmail));

        log.info("CONTROLLER_UPDATE_CATEGORY_SUCCESS | id={} | user={}", id, userEmail);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Eliminar una categoría personal")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoría eliminada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {

        String userEmail = jwt.getClaimAsString("email");
        log.info("CONTROLLER_DELETE_CATEGORY | user={} | categoryId={}", userEmail, id);

        categoryUseCase.deleteCategory(id, userEmail);

        log.info("CONTROLLER_DELETE_CATEGORY_SUCCESS | id={} | user={}", id, userEmail);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Verificar disponibilidad de nombre de categoría")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nombre disponible o no"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/check-name/{name}")
    public ResponseEntity<Boolean> checkCategoryName(
            @PathVariable String name,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        String userEmail = jwt.getClaimAsString("email");
        log.debug("CONTROLLER_CHECK_CATEGORY_NAME | user={} | name={}", userEmail, name);

        boolean isAvailable = categoryUseCase.isCategoryNameAvailable(name, userEmail);

        log.debug("CONTROLLER_CHECK_CATEGORY_NAME_RESULT | available={}", isAvailable);
        return ResponseEntity.ok(isAvailable);
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