package com.fitapp.backend.application.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class PasswordUpdateRequest {
    @NotBlank
    @Size(min = 8)
    private String newPassword;
}
