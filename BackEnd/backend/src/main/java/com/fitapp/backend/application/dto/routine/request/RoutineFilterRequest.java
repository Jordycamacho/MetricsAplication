package com.fitapp.backend.application.dto.routine.request;

import lombok.Data;

@Data
public class RoutineFilterRequest {
    private Long sportId;
    private String name;
    private Boolean isActive;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}