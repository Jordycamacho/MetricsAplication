package com.fitapp.backend.feedback.aplication.dto.response;

import com.fitapp.backend.feedback.domain.model.FeedbackCategory;
import com.fitapp.backend.feedback.domain.model.FeedbackStatus;
import com.fitapp.backend.feedback.domain.model.FeedbackType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class FeedbackResponse {
    private Long id;
    private Long userId;
    private String userEmail;
    private String userFullName;
    private FeedbackType type;
    private FeedbackCategory category;
    private String title;
    private String message;
    private String stepsToReproduce;
    private FeedbackStatus status;
    private Boolean isPublic;
    private Map<String, String> technicalContext;
    private String adminNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
