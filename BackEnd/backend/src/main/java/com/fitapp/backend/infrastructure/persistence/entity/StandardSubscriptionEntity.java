package com.fitapp.backend.infrastructure.persistence.entity;

import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionType;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;

@Entity
@Data
@DiscriminatorValue("STANDARD")
public class StandardSubscriptionEntity extends SubscriptionEntity {
    @Override
    public SubscriptionType getType() {
        return SubscriptionType.STANDARD;
    }

    @Column(name = "max_routines")
    private Integer maxRoutines = 10;

    @Column(name = "basic_analytics_enabled")
    private boolean basicAnalyticsEnabled = true;
}