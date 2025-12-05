package com.fitapp.backend.domain.model;

import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class StandardSubscriptionModel extends SubscriptionModel {
    @Builder.Default
    private Integer maxRoutines = 1;
    @Builder.Default
    private boolean basicAnalyticsEnabled = true;
    
    @Override
    public SubscriptionType getType() {
        return SubscriptionType.STANDARD;
    }
}