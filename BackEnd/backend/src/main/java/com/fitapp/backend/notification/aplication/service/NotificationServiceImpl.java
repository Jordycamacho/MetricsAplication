package com.fitapp.backend.notification.aplication.service;

import com.fitapp.backend.notification.aplication.port.input.NotificationUseCase;
import com.fitapp.backend.notification.aplication.port.output.EmailSenderPort;
import com.fitapp.backend.notification.aplication.port.output.EmailTemplateRendererPort;
import com.fitapp.backend.notification.domain.model.EmailMessage;
import com.fitapp.backend.notification.domain.model.EmailType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationUseCase {

    private final EmailSenderPort emailSenderPort;
    private final EmailTemplateRendererPort templateRenderer;

    @Value("${app.frontend.url}")
    private String baseUrl;

    @Value("${app.deep-link.scheme:fitapp}")
    private String deepLinkScheme;

    @Value("${app.feedback.admin-email:jordycamacho225@gmail.com}")
    private String feedbackAdminEmail;

    @Override
    public void sendVerificationEmail(String toEmail, String token) {
        String verifyUrl = baseUrl + "/api/auth/verify-email?token=" + token;
        dispatch(EmailType.VERIFY_EMAIL, toEmail, Map.of(
                "verifyUrl", verifyUrl,
                "expiryHours", 24
        ));
    }

    @Override
    public void sendWelcomeEmail(String toEmail, String fullName) {
        dispatch(EmailType.WELCOME, toEmail, Map.of(
                "fullName", fullName != null ? fullName : "",
                "appDeepLink", deepLinkScheme + "://auth/callback"
        ));
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String fullName, String token) {
        String resetUrl = baseUrl + "/api/auth/reset-password?token=" + token;
        dispatch(EmailType.PASSWORD_RESET, toEmail, Map.of(
                "fullName", fullName != null ? fullName : "",
                "resetUrl", resetUrl,
                "expiryHours", 1
        ));
    }

    @Override
    public void sendAccountDeletionConfirmation(String toEmail, String fullName) {
        dispatch(EmailType.ACCOUNT_DELETED, toEmail, Map.of(
                "fullName", fullName != null ? fullName : ""
        ));
    }

    @Override
    public void sendFeedbackReceivedUserEmail(String toEmail, String fullName, Long feedbackId, String feedbackType) {
        String typeLabel = "SUGGESTION".equals(feedbackType) ? "sugerencia" : "reporte";
        dispatch(EmailType.FEEDBACK_RECEIVED_USER, toEmail, Map.of(
                "fullName", fullName != null ? fullName : "",
                "feedbackId", feedbackId,
                "feedbackTypeLabel", typeLabel
        ));
    }

    @Override
    public void sendFeedbackReceivedAdminEmail(Long feedbackId, String feedbackType, String category,
            String userEmail, String userFullName, String messagePreview) {
        String preview = messagePreview != null && messagePreview.length() > 300
                ? messagePreview.substring(0, 300) + "..."
                : (messagePreview != null ? messagePreview : "");
        dispatch(EmailType.FEEDBACK_RECEIVED_ADMIN, feedbackAdminEmail, Map.of(
                "feedbackId", feedbackId,
                "feedbackType", feedbackType,
                "category", category,
                "userEmail", userEmail != null ? userEmail : "",
                "userFullName", userFullName != null ? userFullName : "",
                "messagePreview", preview,
                "adminPanelHint", "Revisa el panel de administración cuando esté disponible."
        ));
    }

    private void dispatch(EmailType type, String toEmail, Map<String, Object> variables) {
        String html = templateRenderer.render(type, variables);
        EmailMessage message = EmailMessage.builder()
                .to(toEmail)
                .subject(type.getDefaultSubject())
                .type(type)
                .variables(variables)
                .build();
        emailSenderPort.send(message, html);
    }
}
