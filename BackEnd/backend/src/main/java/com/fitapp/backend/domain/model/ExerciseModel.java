package com.fitapp.backend.domain.model;

import lombok.Data;

@Data
public class ExerciseModel {
    private Long id;
    private String name;
    private String description;
    private Long sportId;
    private Long userId;
}