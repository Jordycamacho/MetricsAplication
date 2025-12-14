package com.fitapp.backend.infrastructure.controller;

import com.fitapp.backend.application.dto.sport.SportRequest;
import com.fitapp.backend.application.dto.sport.SportResponse;
import com.fitapp.backend.application.ports.input.SportUseCase;
import com.fitapp.backend.domain.model.SportModel;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sports")
@RequiredArgsConstructor
@Tag(name = "Gestión de Deportes", description = "Endpoints para la gestión de deportes predefinidos y personalizados")
public class SportController {
    private final SportUseCase sportService;

    @Operation(summary = "Obtener todos los deportes", description = "Retorna todos los deportes disponibles (predefinidos y personalizados)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de deportes obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping
    public ResponseEntity<List<SportResponse>> getAllSports() {
        List<SportModel> sports = sportService.getAllSports();
        return ResponseEntity.ok(convertToResponseList(sports));
    }

    @Operation(summary = "Obtener deportes predefinidos", description = "Retorna solo los deportes predefinidos del sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deportes predefinidos obtenidos exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/predefined")
    public ResponseEntity<List<SportResponse>> getPredefinedSports() {
        List<SportModel> sports = sportService.getPredefinedSports();
        return ResponseEntity.ok(convertToResponseList(sports));
    }

    @Operation(summary = "Obtener deportes personalizados del usuario", description = "Retorna los deportes personalizados creados por el usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deportes personales obtenidos exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/custom")
    public ResponseEntity<List<SportResponse>> getUserSports(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        String userEmail = jwt.getClaimAsString("email");
        List<SportModel> sports = sportService.getUserSports(userEmail);
        return ResponseEntity.ok(convertToResponseList(sports));
    }

    @Operation(summary = "Crear un deporte personalizado", description = "Crea un nuevo deporte personalizado para el usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deporte creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping("/custom")
    public ResponseEntity<SportResponse> createCustomSport(
            @Valid @RequestBody SportRequest sportRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        String userEmail = jwt.getClaimAsString("email");
        SportModel createdSport = sportService.createCustomSport(sportRequest, userEmail);
        return ResponseEntity.ok(convertToResponse(createdSport));
    }

    @Operation(summary = "Eliminar un deporte personalizado", description = "Elimina un deporte personalizado del usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deporte eliminado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos para eliminar este deporte"),
            @ApiResponse(responseCode = "404", description = "Deporte no encontrado")
    })
    @DeleteMapping("/custom/{id}")
    public ResponseEntity<Void> deleteCustomSport(
            @Parameter(description = "ID del deporte a eliminar") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        String userEmail = jwt.getClaimAsString("email");
        sportService.deleteCustomSport(id, userEmail);
        return ResponseEntity.noContent().build();
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
        response.setParameterTemplate(model.getParameterTemplate());
        response.setCategory(model.getCategory());
        return response;
    }
}