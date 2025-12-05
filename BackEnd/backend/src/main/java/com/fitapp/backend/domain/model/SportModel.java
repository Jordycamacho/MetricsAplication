package com.fitapp.backend.domain.model;

import lombok.Data;
import java.util.Map;

@Data
public class SportModel {
    private Long id;
    private String name;
    private Boolean isPredefined;
    private Long createdBy;
    private Map<String, String> parameterTemplate;
    private String iconUrl;
    private String category;
}