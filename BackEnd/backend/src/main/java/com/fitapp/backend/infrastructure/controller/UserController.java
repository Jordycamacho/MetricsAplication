package com.fitapp.backend.infrastructure.controller;

import com.fitapp.backend.application.dto.subscription.SubscriptionResponse;
import com.fitapp.backend.application.dto.user.UserResponse;
import com.fitapp.backend.application.dto.user.UserUpdateRequest;
import com.fitapp.backend.application.ports.input.UserUseCase;
import com.fitapp.backend.domain.model.SubscriptionModel;
import com.fitapp.backend.domain.model.UserModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Management", description = "Endpoints para gestión de usuarios")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserUseCase userUseCase;
    
    @Operation(
        summary = "Obtener todos los usuarios",
        description = "Retorna una lista paginada de usuarios (solo para administradores)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuarios obtenidos exitosamente"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "No tiene permisos de administrador")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        Page<UserModel> users = userUseCase.findAll(pageable);
        Page<UserResponse> response = users.map(this::convertToResponse);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Obtener usuario por ID",
        description = "Retorna un usuario específico por su ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuario obtenido exitosamente"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.userId")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "ID del usuario") @PathVariable Long id) {
        UserModel user = userUseCase.findById(id);
        return ResponseEntity.ok(convertToResponse(user));
    }
    
    @Operation(
        summary = "Actualizar usuario",
        description = "Actualiza la información de un usuario existente"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.userId")
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "ID del usuario") @PathVariable Long id,
            @RequestBody UserUpdateRequest updateRequest) {
        UserModel updatedUser = userUseCase.updateUser(id, updateRequest);
        return ResponseEntity.ok(convertToResponse(updatedUser));
    }
    
    @Operation(
        summary = "Eliminar usuario",
        description = "Elimina un usuario del sistema (solo para administradores)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Usuario eliminado exitosamente"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "No tiene permisos de administrador"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID del usuario") @PathVariable Long id) {
        userUseCase.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(
        summary = "Activar usuario",
        description = "Activa un usuario desactivado (solo para administradores)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Usuario activado exitosamente"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "No tiene permisos de administrador"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activateUser(
            @Parameter(description = "ID del usuario") @PathVariable Long id) {
        userUseCase.activateUser(id);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(
        summary = "Desactivar usuario",
        description = "Desactiva un usuario activo (solo para administradores)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Usuario desactivado exitosamente"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "No tiene permisos de administrador"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateUser(
            @Parameter(description = "ID del usuario") @PathVariable Long id) {
        userUseCase.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(
        summary = "Alternar estado de usuario",
        description = "Alterna el estado activo/inactivo de un usuario (solo para administradores)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Estado alternado exitosamente"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "No tiene permisos de administrador"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PostMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> toggleUserStatus(
            @Parameter(description = "ID del usuario") @PathVariable Long id,
            @RequestParam boolean isActive) {
        userUseCase.toggleUserStatus(id, isActive);
        return ResponseEntity.noContent().build();
    }
    
    private UserResponse convertToResponse(UserModel userModel) {
        UserResponse.UserResponseBuilder builder = UserResponse.builder()
                .id(userModel.getId())
                .email(userModel.getEmail())
                .fullName(userModel.getFullName())
                .role(userModel.getRole())
                .isActive(userModel.isActive())
                .maxRoutines(userModel.getMaxRoutines())
                .createdAt(userModel.getCreatedAt())
                .updatedAt(userModel.getUpdatedAt());
        
        if (userModel.getSubscription() != null) {
            builder.subscription(convertSubscriptionToResponse(userModel.getSubscription()));
        }
        
        return builder.build();
    }
    
    private SubscriptionResponse convertSubscriptionToResponse(SubscriptionModel subscriptionModel) {
        return SubscriptionResponse.builder()
                .id(subscriptionModel.getId())
                .type(subscriptionModel.getType())
                .startDate(subscriptionModel.getStartDate())
                .endDate(subscriptionModel.getEndDate())
                .maxRoutines(subscriptionModel.getMaxRoutines())
                .build();
    }
}