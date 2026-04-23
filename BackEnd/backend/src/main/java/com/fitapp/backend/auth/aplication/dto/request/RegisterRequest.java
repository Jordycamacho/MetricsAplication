package com.fitapp.backend.auth.aplication.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Email es requerido")
    @Email(message = "Email debe ser válido")
    @JsonProperty("email")
    private String email;
    
    @NotBlank(message = "Contraseña es requerida")
    @Size(min = 6, message = "Contraseña debe tener al menos 6 caracteres")
    @JsonProperty("password")
    private String password;
    
    @NotBlank(message = "Nombre completo es requerido")
    @JsonProperty("fullName")
    private String fullName;
}