package com.fitapp.backend.application.dto.subscription;

import lombok.Getter;
import lombok.Builder;
import java.time.LocalDate;

import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionType;

@Getter
@Builder
public class SubscriptionResponse {
    private Long id;
    private SubscriptionType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer maxRoutines;
    private boolean basicAnalyticsEnabled;
    private boolean advancedAnalyticsEnabled;
    private boolean customParametersAllowed;
}