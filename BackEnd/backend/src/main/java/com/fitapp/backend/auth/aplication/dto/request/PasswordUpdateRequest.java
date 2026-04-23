package com.fitapp.backend.auth.aplication.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class PasswordUpdateRequest {
    @NotBlank
    @Size(min = 8)
    @JsonProperty("newPassword")
    private String newPassword;
}