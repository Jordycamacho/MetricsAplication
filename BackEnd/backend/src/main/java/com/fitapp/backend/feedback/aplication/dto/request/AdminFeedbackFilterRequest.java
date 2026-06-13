package com.fitapp.backend.feedback.aplication.dto.request;

import com.fitapp.backend.feedback.domain.model.FeedbackCategory;
import com.fitapp.backend.feedback.domain.model.FeedbackStatus;
import com.fitapp.backend.feedback.domain.model.FeedbackType;
import lombok.Data;

@Data
public class AdminFeedbackFilterRequest {
    private FeedbackType type;
    private FeedbackStatus status;
    private FeedbackCategory category;
    private Integer page = 0;
    private Integer size = 20;
}
