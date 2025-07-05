package com.fitapp.backend.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fitapp.backend.application.dto.user.PasswordUpdateRequest;
import com.fitapp.backend.application.dto.user.UserCreationRequest;
import com.fitapp.backend.application.dto.user.UserResponse;
import com.fitapp.backend.application.ports.input.UserUseCase;
import com.fitapp.backend.domain.model.UserModel;
import com.fitapp.backend.infrastructure.persistence.converter.UserConverter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserUseCase userUseCase;

    public UserController(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @PostMapping("/create")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserCreationRequest request) {
        UserModel createdUser = userUseCase.createUser(request);
        UserResponse response = UserConverter.toResponse(createdUser);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/password")
    @Operation(summary = "Actualiza la contraseña de un usuario")
    public ResponseEntity<Void> updatePassword(
            @Parameter(description = "ID del usuario") @PathVariable UUID id,
            @Valid @RequestBody PasswordUpdateRequest request) {

        userUseCase.updatePassword(id, request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

}