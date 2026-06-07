package com.fitapp.backend.notification.aplication.port.output;

import com.fitapp.backend.notification.domain.model.EmailType;

import java.util.Map;

public interface EmailTemplateRendererPort {
    String render(EmailType type, Map<String, Object> variables);
}
