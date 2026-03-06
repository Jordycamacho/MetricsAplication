package com.fitapp.backend.application.dto.subscription.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionStatus;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionType;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("type")
    private SubscriptionType type;

    @JsonProperty("status")
    private SubscriptionStatus status;

    @JsonProperty("maxRoutines")
    private Integer maxRoutines;

    @JsonProperty("startDate")
    private LocalDate startDate;

    @JsonProperty("endDate")
    private LocalDate endDate;

    @JsonProperty("canExportRoutines")
    private boolean canExportRoutines;

    @JsonProperty("canAccessMarketplace")
    private boolean canAccessMarketplace;

    @JsonProperty("canSellOnMarketplace")
    private boolean canSellOnMarketplace;

    @JsonProperty("advancedAnalytics")
    private boolean advancedAnalytics;

    @JsonProperty("autoRenew")
    private boolean autoRenew;
}