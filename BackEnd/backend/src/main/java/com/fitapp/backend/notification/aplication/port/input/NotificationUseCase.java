package com.fitapp.backend.notification.aplication.port.input;

public interface NotificationUseCase {
    void sendVerificationEmail(String toEmail, String token);

    void sendWelcomeEmail(String toEmail, String fullName);

    void sendPasswordResetEmail(String toEmail, String fullName, String token);

    void sendAccountDeletionConfirmation(String toEmail, String fullName);

    void sendFeedbackReceivedUserEmail(String toEmail, String fullName, Long feedbackId, String feedbackType);

    void sendFeedbackReceivedAdminEmail(Long feedbackId, String feedbackType, String category,
            String userEmail, String userFullName, String messagePreview);
}
