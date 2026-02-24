package com.fitapp.backend.domain.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import com.fitapp.backend.infrastructure.persistence.entity.enums.ParameterType;

@Data
@Slf4j
public class CustomParameterModel {
    private Long id;
    private String name;
    private String description;
    private ParameterType parameterType;
    private String unit;
    private Boolean isGlobal;
    private Boolean isActive;
    private Long ownerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer usageCount;
    private boolean isFavorite;

    public void logModelData(String operation) {
        log.info("PARAMETER_MODEL_{} | id={} | name={} | type={} | isGlobal={}", 
                operation.toUpperCase(), id, name, parameterType, isGlobal);
        log.debug("PARAMETER_MODEL_DETAILS | unit={} | usageCount={} | isFavorite={}", 
                unit, usageCount, isFavorite);
    }
    
    public void validateFormat() {
        if (name != null && !name.matches("^[a-z]+([A-Z][a-z]*)*$")) {
            log.warn("PARAMETER_NAME_FORMAT_WARNING | name={} | format may cause frontend issues", name);
        }
    }
}