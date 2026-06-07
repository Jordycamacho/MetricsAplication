package com.fitapp.backend.notification.aplication.port.output;

import com.fitapp.backend.notification.domain.model.EmailMessage;

public interface EmailSenderPort {
    void send(EmailMessage message, String htmlBody);
}
