package com.fitapp.backend.feedback.aplication.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FeedbackPageResponse {
    private List<FeedbackResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
