package com.fitapp.backend.notification.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class EmailMessage {
    private final String to;
    private final String subject;
    private final EmailType type;
    private final Map<String, Object> variables;
}
