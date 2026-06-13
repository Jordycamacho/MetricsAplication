package com.fitapp.backend.feedback.aplication.dto.request;

import com.fitapp.backend.feedback.domain.model.FeedbackCategory;
import com.fitapp.backend.feedback.domain.model.FeedbackType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

@Data
public class CreateFeedbackRequest {

    @NotNull(message = "type is required")
    private FeedbackType type;

    private FeedbackCategory category;

    @Size(max = 200, message = "title must be at most 200 characters")
    private String title;

    @NotBlank(message = "message is required")
    @Size(min = 10, message = "message must be at least 10 characters")
    private String message;

    private String stepsToReproduce;

    private Boolean includeTechnicalContext;

    private Map<String, String> technicalContext;
}
