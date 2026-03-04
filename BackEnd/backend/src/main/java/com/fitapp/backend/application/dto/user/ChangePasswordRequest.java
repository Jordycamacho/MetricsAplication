package com.fitapp.backend.application.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ChangePasswordRequest {

    @NotBlank(message = "La contraseña actual es obligatoria")
    @JsonProperty("currentPassword")
    private String currentPassword;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres")
    @JsonProperty("newPassword")
    private String newPassword;
}