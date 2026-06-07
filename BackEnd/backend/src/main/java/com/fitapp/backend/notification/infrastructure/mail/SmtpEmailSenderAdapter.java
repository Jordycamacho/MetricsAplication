package com.fitapp.backend.notification.infrastructure.mail;

import com.fitapp.backend.notification.aplication.port.output.EmailSenderPort;
import com.fitapp.backend.notification.domain.exception.EmailDeliveryException;
import com.fitapp.backend.notification.domain.model.EmailMessage;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmtpEmailSenderAdapter implements EmailSenderPort {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Override
    @Async("emailTaskExecutor")
    public void send(EmailMessage message, String htmlBody) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(message.getTo());
            helper.setSubject(message.getSubject());
            helper.setText(htmlBody, true);
            mailSender.send(mimeMessage);
            log.info("Email {} enviado a {}", message.getType(), message.getTo());
        } catch (Exception e) {
            log.error("Error enviando email {} a {}: {}", message.getType(), message.getTo(), e.getMessage());
            throw new EmailDeliveryException("No se pudo enviar el correo", e);
        }
    }
}
