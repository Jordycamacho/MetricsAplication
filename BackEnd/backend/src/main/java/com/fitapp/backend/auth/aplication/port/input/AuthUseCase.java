package com.fitapp.backend.auth.aplication.port.input;

import com.fitapp.backend.auth.aplication.dto.request.LoginRequest;
import com.fitapp.backend.auth.aplication.dto.request.RegisterRequest;
import com.fitapp.backend.auth.aplication.dto.request.ResetPasswordRequest;
import com.fitapp.backend.auth.aplication.dto.response.AuthResponse;
import com.fitapp.backend.auth.aplication.dto.response.RegisterResponse;
import com.fitapp.backend.auth.domain.model.CustomUserDetails;

public interface AuthUseCase {
    RegisterResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshAccessToken(String refreshToken);
    AuthResponse refreshToken(CustomUserDetails userDetails);

    /** Invalida el token actual añadiéndolo a la blacklist de Redis. */
    void logout(String bearerToken);

    /** Verifica el email usando el token enviado por correo. */
    void verifyEmail(String token);

    /** Reenvía el correo de verificación al usuario autenticado. */
    void resendVerificationEmail(Long userId);

    /** Reenvía verificación por email (público, rate limited). */
    void resendVerificationByEmail(String email);

    /** Solicita restablecimiento de contraseña (público, rate limited). */
    void forgotPassword(String email);

    /** Restablece la contraseña con token del correo. */
    void resetPassword(ResetPasswordRequest request);
}
