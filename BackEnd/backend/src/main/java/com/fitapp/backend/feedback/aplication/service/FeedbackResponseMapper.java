package com.fitapp.backend.feedback.aplication.service;

import com.fitapp.backend.feedback.aplication.dto.response.FeedbackResponse;
import com.fitapp.backend.feedback.domain.model.FeedbackModel;

public final class FeedbackResponseMapper {

    private FeedbackResponseMapper() {
    }

    public static FeedbackResponse toResponse(FeedbackModel model) {
        return FeedbackResponse.builder()
                .id(model.getId())
                .userId(model.getUserId())
                .userEmail(model.getUserEmail())
                .userFullName(model.getUserFullName())
                .type(model.getType())
                .category(model.getCategory())
                .title(model.getTitle())
                .message(model.getMessage())
                .stepsToReproduce(model.getStepsToReproduce())
                .status(model.getStatus())
                .isPublic(model.getIsPublic())
                .technicalContext(model.getTechnicalContext())
                .adminNotes(model.getAdminNotes())
                .createdAt(model.getCreatedAt())
                .updatedAt(model.getUpdatedAt())
                .build();
    }
}
