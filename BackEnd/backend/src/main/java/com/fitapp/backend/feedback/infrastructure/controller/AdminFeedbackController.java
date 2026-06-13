package com.fitapp.backend.feedback.infrastructure.controller;

import com.fitapp.backend.feedback.aplication.dto.request.AdminFeedbackFilterRequest;
import com.fitapp.backend.feedback.aplication.dto.request.UpdateFeedbackAdminRequest;
import com.fitapp.backend.feedback.aplication.dto.request.UpdateFeedbackStatusRequest;
import com.fitapp.backend.feedback.aplication.dto.response.FeedbackPageResponse;
import com.fitapp.backend.feedback.aplication.dto.response.FeedbackResponse;
import com.fitapp.backend.feedback.aplication.port.input.AdminFeedbackUseCase;
import com.fitapp.backend.feedback.domain.model.FeedbackCategory;
import com.fitapp.backend.feedback.domain.model.FeedbackStatus;
import com.fitapp.backend.feedback.domain.model.FeedbackType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/feedback")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Feedback", description = "Admin feedback management")
public class AdminFeedbackController {

    private final AdminFeedbackUseCase adminFeedbackUseCase;

    @GetMapping
    @Operation(summary = "List feedback with optional filters")
    public ResponseEntity<FeedbackPageResponse> listFeedback(
            @RequestParam(required = false) FeedbackType type,
            @RequestParam(required = false) FeedbackStatus status,
            @RequestParam(required = false) FeedbackCategory category,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        AdminFeedbackFilterRequest filter = new AdminFeedbackFilterRequest();
        filter.setType(type);
        filter.setStatus(status);
        filter.setCategory(category);
        filter.setPage(page);
        filter.setSize(size);
        return ResponseEntity.ok(adminFeedbackUseCase.list(filter));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get feedback detail")
    public ResponseEntity<FeedbackResponse> getFeedback(@PathVariable Long id) {
        return ResponseEntity.ok(adminFeedbackUseCase.getById(id));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update feedback status")
    public ResponseEntity<FeedbackResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFeedbackStatusRequest request) {
        log.info("ADMIN_UPDATE_FEEDBACK_STATUS | id={} | status={}", id, request.getStatus());
        return ResponseEntity.ok(adminFeedbackUseCase.updateStatus(id, request));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update admin notes or public flag")
    public ResponseEntity<FeedbackResponse> updateFeedback(
            @PathVariable Long id,
            @RequestBody UpdateFeedbackAdminRequest request) {
        log.info("ADMIN_UPDATE_FEEDBACK | id={}", id);
        return ResponseEntity.ok(adminFeedbackUseCase.update(id, request));
    }
}
