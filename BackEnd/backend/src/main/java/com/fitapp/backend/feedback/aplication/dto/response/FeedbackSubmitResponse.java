package com.fitapp.backend.feedback.aplication.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FeedbackSubmitResponse {
    private Long id;
    private String message;
}
