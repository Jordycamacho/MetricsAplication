package com.fitapp.backend.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fitapp.backend.application.dto.user.PasswordUpdateRequest;
import com.fitapp.backend.application.dto.user.UserCreationRequest;
import com.fitapp.backend.application.dto.user.UserResponse;
import com.fitapp.backend.application.dto.user.UserUpdateRequest;
import com.fitapp.backend.application.ports.input.UserUseCase;
import com.fitapp.backend.domain.model.UserModel;
import com.fitapp.backend.infrastructure.persistence.converter.UserConverter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Tag(name = "User Management", description = "Endpoints para gestionar usuarios")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserUseCase userUseCase;

    public UserController(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @PostMapping("/create")
    @Operation(summary = "Crear un nuevo usuario")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserCreationRequest request) {
        UserModel createdUser = userUseCase.createUser(request);
        UserResponse response = UserConverter.toResponse(createdUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
 
    @PatchMapping("/{id}/password")
    @Operation(summary = "Actualiza la contraseña de un usuario")
    public ResponseEntity<Void> updatePassword(
            @Parameter(description = "ID del usuario") @PathVariable UUID id,
            @Valid @RequestBody PasswordUpdateRequest request) {

        userUseCase.updatePassword(id, request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Actualizar datos de un usuario")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "ID del usuario") @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequest updateRequest) {
        UserModel updatedUser = userUseCase.updateUser(id, updateRequest);
        return ResponseEntity.ok(UserConverter.toResponse(updatedUser));
    }

    @Operation(summary = "Obtener un usuario por ID")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "ID del usuario") @PathVariable UUID id) {
        UserModel user = userUseCase.getUserById(id);
        return ResponseEntity.ok(UserConverter.toResponse(user));
    }

    @Operation(summary = "Obtener todos los usuarios")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserModel> users = userUseCase.getAllUsers();
        List<UserResponse> responses = users.stream()
                .map(UserConverter::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Eliminar un usuario por ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID del usuario") @PathVariable UUID id) {
        userUseCase.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Activar un usuario")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(
            @Parameter(description = "ID del usuario") @PathVariable UUID id) {
        userUseCase.activateUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Desactivar un usuario")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(
            @Parameter(description = "ID del usuario") @PathVariable UUID id) {
        userUseCase.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtener usuario por email")
    @GetMapping("/by-email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(
            @Parameter(description = "Email del usuario") @PathVariable String email) {
        return userUseCase.findByEmail(email)
                .map(user -> ResponseEntity.ok(UserConverter.toResponse(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Obtener usuario por Supabase UID")
    @GetMapping("/by-supabase/{uid}")
    public ResponseEntity<UserResponse> getUserBySupabaseUid(
            @Parameter(description = "Supabase UID del usuario") @PathVariable String uid) {
        return userUseCase.findBySupabaseUid(uid)
                .map(user -> ResponseEntity.ok(UserConverter.toResponse(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}