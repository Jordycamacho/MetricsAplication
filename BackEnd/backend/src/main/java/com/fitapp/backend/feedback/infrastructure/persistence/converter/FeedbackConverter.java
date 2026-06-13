package com.fitapp.backend.feedback.infrastructure.persistence.converter;

import com.fitapp.backend.feedback.domain.model.FeedbackModel;
import com.fitapp.backend.feedback.infrastructure.persistence.entity.UserFeedbackEntity;
import org.springframework.stereotype.Component;

@Component
public class FeedbackConverter {

    public FeedbackModel toDomain(UserFeedbackEntity entity) {
        if (entity == null) {
            return null;
        }
        return FeedbackModel.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userEmail(entity.getUser() != null ? entity.getUser().getEmail() : null)
                .userFullName(entity.getUser() != null ? entity.getUser().getFullName() : null)
                .type(entity.getType())
                .category(entity.getCategory())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .stepsToReproduce(entity.getStepsToReproduce())
                .status(entity.getStatus())
                .isPublic(entity.getIsPublic())
                .technicalContext(entity.getTechnicalContext())
                .adminNotes(entity.getAdminNotes())
                .deletedAt(entity.getDeletedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public UserFeedbackEntity toEntity(FeedbackModel model) {
        if (model == null) {
            return null;
        }
        UserFeedbackEntity entity = new UserFeedbackEntity();
        entity.setId(model.getId());
        entity.setType(model.getType());
        entity.setCategory(model.getCategory());
        entity.setTitle(model.getTitle());
        entity.setMessage(model.getMessage());
        entity.setStepsToReproduce(model.getStepsToReproduce());
        entity.setStatus(model.getStatus() != null ? model.getStatus() : com.fitapp.backend.feedback.domain.model.FeedbackStatus.RECEIVED);
        entity.setIsPublic(model.getIsPublic() != null ? model.getIsPublic() : false);
        entity.setTechnicalContext(model.getTechnicalContext());
        entity.setAdminNotes(model.getAdminNotes());
        entity.setDeletedAt(model.getDeletedAt());
        return entity;
    }
}
