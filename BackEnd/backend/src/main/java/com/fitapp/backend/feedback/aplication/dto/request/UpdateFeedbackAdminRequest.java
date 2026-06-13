package com.fitapp.backend.feedback.aplication.dto.request;

import lombok.Data;

@Data
public class UpdateFeedbackAdminRequest {
    private String adminNotes;
    private Boolean isPublic;
}
