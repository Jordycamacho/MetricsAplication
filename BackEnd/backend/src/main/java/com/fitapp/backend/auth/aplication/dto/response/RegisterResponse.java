package com.fitapp.backend.auth.aplication.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    @JsonProperty("email")
    private String email;

    @JsonProperty("message")
    private String message;

    @JsonProperty("requiresEmailVerification")
    private boolean requiresEmailVerification;
}
