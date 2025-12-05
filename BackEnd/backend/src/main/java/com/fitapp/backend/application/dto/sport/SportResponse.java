package com.fitapp.backend.application.dto.sport;

import java.util.Map;

import lombok.Data;

@Data
public class SportResponse {
    private Long id;
    private String name;
    private Boolean isPredefined;
    private Map<String, String> parameterTemplate;
    private String iconUrl;
    private String category;
}