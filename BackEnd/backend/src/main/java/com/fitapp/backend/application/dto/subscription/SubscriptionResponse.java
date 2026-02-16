package com.fitapp.backend.application.dto.subscription;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Builder;
import java.time.LocalDate;

import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionType;

@Getter
@Builder
public class SubscriptionResponse {
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("type")
    private SubscriptionType type;
    
    @JsonProperty("startDate")
    private LocalDate startDate;
    
    @JsonProperty("endDate")
    private LocalDate endDate;
    
    @JsonProperty("maxRoutines")
    private Integer maxRoutines;
    
    @JsonProperty("basicAnalyticsEnabled")
    private boolean basicAnalyticsEnabled;
    
    @JsonProperty("advancedAnalyticsEnabled")
    private boolean advancedAnalyticsEnabled;
    
    @JsonProperty("customParametersAllowed")
    private boolean customParametersAllowed;
}