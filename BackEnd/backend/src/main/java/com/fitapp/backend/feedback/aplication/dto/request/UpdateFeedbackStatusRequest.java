package com.fitapp.backend.feedback.aplication.dto.request;

import com.fitapp.backend.feedback.domain.model.FeedbackStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateFeedbackStatusRequest {

    @NotNull(message = "status is required")
    private FeedbackStatus status;
}
