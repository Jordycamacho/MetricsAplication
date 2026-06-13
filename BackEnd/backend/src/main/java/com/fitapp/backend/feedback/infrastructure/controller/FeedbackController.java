package com.fitapp.backend.feedback.infrastructure.controller;

import com.fitapp.backend.feedback.aplication.dto.request.CreateFeedbackRequest;
import com.fitapp.backend.feedback.aplication.dto.response.FeedbackSubmitResponse;
import com.fitapp.backend.feedback.aplication.port.input.FeedbackUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Feedback", description = "User feedback and suggestions")
public class FeedbackController {

    private final FeedbackUseCase feedbackUseCase;

    @PostMapping
    @Operation(summary = "Submit feedback (bug report or suggestion)")
    public ResponseEntity<FeedbackSubmitResponse> submitFeedback(
            @Valid @RequestBody CreateFeedbackRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = extractUserId(jwt);
        log.info("SUBMIT_FEEDBACK | userId={} | type={}", userId, request.getType());
        FeedbackSubmitResponse response = feedbackUseCase.submit(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete own feedback")
    public ResponseEntity<Void> deleteOwnFeedback(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = extractUserId(jwt);
        log.info("DELETE_FEEDBACK | userId={} | feedbackId={}", userId, id);
        feedbackUseCase.deleteOwn(id, userId);
        return ResponseEntity.noContent().build();
    }

    private Long extractUserId(Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token: userId missing");
        }
        return userId;
    }
}
