package com.fitapp.backend.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fitapp.backend.application.dto.user.UserCreationRequest;
import com.fitapp.backend.application.ports.input.UserUseCase;
import com.fitapp.backend.domain.model.UserModel;

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
    public ResponseEntity<UserModel> registerUser(@Valid @RequestBody UserCreationRequest request) {
        UserModel createdUser = userUseCase.createUser(request);
        return ResponseEntity.ok(createdUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserModel> updateUser(
            @PathVariable UUID id,
            @RequestBody UserModel user) {
        return ResponseEntity.ok(userUseCase.updateUser(id, user));
    }

    /*
     * @DeleteMapping("/{id}")
     * public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
     * userUseCase.deleteUser(id);
     * return ResponseEntity.noContent().build();
     * }
     * 
     * // Endpoints adicionales
     * 
     * @PostMapping("/{id}/activate")
     * public ResponseEntity<UserModel> activateUser(@PathVariable UUID id) {
     * return ResponseEntity.ok(userUseCase.activateUser(id));
     * }
     */
}