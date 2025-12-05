package com.fitapp.backend.infrastructure.controller;

import com.fitapp.backend.application.dto.sport.SportResponse;
import com.fitapp.backend.application.ports.input.SportUseCase;
import com.fitapp.backend.domain.model.SportModel;
import com.fitapp.backend.infrastructure.persistence.converter.SportConverter;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sports")
@RequiredArgsConstructor
public class SportController {
    private final SportUseCase sportService;
    private final SportConverter sportConverter;

    @GetMapping
    public ResponseEntity<List<SportResponse>> getAllSports() {
        List<SportModel> sports = sportService.getAllSports();
        return ResponseEntity.ok(sports.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/predefined")
    public ResponseEntity<List<SportResponse>> getPredefinedSports() {
        List<SportModel> sports = sportService.getPredefinedSports();
        return ResponseEntity.ok(sports.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/custom")
    public ResponseEntity<List<SportResponse>> getUserSports(@AuthenticationPrincipal Jwt jwt) {
        String userEmail = jwt.getClaimAsString("email");
        List<SportModel> sports = sportService.getUserSports(userEmail);
        return ResponseEntity.ok(sports.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList()));
    }

    @PostMapping("/custom")
    public ResponseEntity<SportResponse> createCustomSport(
            @RequestBody SportModel sportModel,
            @AuthenticationPrincipal Jwt jwt) {
        String userEmail = jwt.getClaimAsString("email");
        SportModel createdSport = sportService.createCustomSport(sportModel, userEmail);
        return ResponseEntity.ok(convertToResponse(createdSport));
    }

    @DeleteMapping("/custom/{id}")
    public ResponseEntity<Void> deleteCustomSport(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        String userEmail = jwt.getClaimAsString("email");
        sportService.deleteCustomSport(id, userEmail);
        return ResponseEntity.noContent().build();
    }

    private SportResponse convertToResponse(SportModel model) {
        SportResponse response = new SportResponse();
        response.setId(model.getId());
        response.setName(model.getName());
        response.setIsPredefined(model.getIsPredefined());
        response.setParameterTemplate(model.getParameterTemplate());
        response.setIconUrl(model.getIconUrl());
        response.setCategory(model.getCategory());
        return response;
    }
}