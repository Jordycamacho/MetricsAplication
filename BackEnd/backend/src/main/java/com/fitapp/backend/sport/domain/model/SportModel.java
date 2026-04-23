package com.fitapp.backend.sport.domain.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import com.fitapp.backend.infrastructure.persistence.entity.enums.SportSourceType;


@Data
@Slf4j
public class SportModel {
    private Long id;
    private String name;
    private Boolean isPredefined;
    private SportSourceType sourceType;
    private Long createdBy;
    private Map<String, String> parameterTemplate;

    public void logModelData(String operation) {
        log.info("SPORT_MODEL_{} | id={} | name={} | sourceType={} | isPredefined={}", 
                operation.toUpperCase(), id, name, sourceType, isPredefined);
        if (parameterTemplate != null && !parameterTemplate.isEmpty()) {
            log.debug("SPORT_MODEL_PARAMETERS | template={}", parameterTemplate);
        }
    }
}