package com.fitapp.backend.auth.aplication.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResendVerificationByEmailRequest {
    @NotBlank
    @Email
    private String email;
}
