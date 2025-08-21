package com.fitapp.backend.domain.model;

import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class PremiumSubscriptionModel extends SubscriptionModel {
    private Integer maxRoutines;
    @Builder.Default
    private boolean advancedAnalyticsEnabled = true;
    
    @Override
    public SubscriptionType getType() {
        return SubscriptionType.PREMIUM;
    }
    
    @Override
    public Integer getMaxRoutines() {
        return maxRoutines;
    }
}