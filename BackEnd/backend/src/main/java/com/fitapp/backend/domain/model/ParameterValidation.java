package com.fitapp.backend.domain.model;

import lombok.Data;

@Data
public class ParameterValidation {
    private Double minValue;
    private Double maxValue;
    private String pattern; 
    private Integer minLength;
    private Integer maxLength;
    private Boolean required;
    private String defaultValue;
}