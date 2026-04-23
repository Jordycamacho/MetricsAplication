package com.fitapp.backend.infrastructure.shared;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public void sendVerificationEmail(String toEmail, String token) {
        String link = frontendUrl + "/auth/verify-email?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Verifica tu correo - AppFit");
        message.setText(
                "Hola,\n\n" +
                "Por favor verifica tu correo haciendo clic en el siguiente enlace:\n\n" +
                link + "\n\n" +
                "El enlace expira en 24 horas.\n\n" +
                "Si no creaste esta cuenta, ignora este mensaje.\n\n" +
                "— El equipo de AppFit"
        );

        try {
            mailSender.send(message);
            log.info("Correo de verificación enviado a: {}", toEmail);
        } catch (Exception e) {
            log.error("Error enviando correo de verificación a {}: {}", toEmail, e.getMessage());
        }
    }

    public void sendAccountDeletionConfirmation(String toEmail, String fullName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Tu cuenta en AppFit ha sido eliminada");
        message.setText(
                "Hola " + (fullName != null ? fullName : "") + ",\n\n" +
                "Tu cuenta ha sido eliminada correctamente. " +
                "Todos tus datos serán eliminados permanentemente en 30 días.\n\n" +
                "Si fue un error, contacta a soporte dentro de ese plazo.\n\n" +
                "— El equipo de AppFit"
        );

        try {
            mailSender.send(message);
            log.info("Correo de eliminación de cuenta enviado a: {}", toEmail);
        } catch (Exception e) {
            log.error("Error enviando correo de eliminación a {}: {}", toEmail, e.getMessage());
        }
    }
}