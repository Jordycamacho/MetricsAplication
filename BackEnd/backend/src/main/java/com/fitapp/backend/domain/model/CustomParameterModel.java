package com.fitapp.backend.domain.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;

import com.fitapp.backend.infrastructure.persistence.entity.enums.ParameterType;

@Data
@Slf4j
public class CustomParameterModel {
    private Long id;
    private String name;
    private String displayName;
    private String description;
    private ParameterType parameterType;
    private String unit;
    private Map<String, String> validationRules;
    private Boolean isGlobal;
    private Boolean isActive;
    private Long ownerId;
    private Long sportId;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer usageCount;

    public void logModelData(String operation) {
        log.info("PARAMETER_MODEL_{} | id={} | name={} | type={} | isGlobal={} | sportId={}", 
                operation.toUpperCase(), id, name, parameterType, isGlobal, sportId);
        log.debug("PARAMETER_MODEL_DETAILS | displayName={} | unit={} | category={} | usageCount={}", 
                displayName, unit, category, usageCount);
    }
    
    public void validateFormat() {
        // Validar formato camelCase del nombre
        if (name != null && !name.matches("^[a-z]+([A-Z][a-z]*)*$")) {
            log.warn("PARAMETER_NAME_FORMAT_WARNING | name={} | format may cause frontend issues", name);
        }
    }
}