package com.fitapp.backend.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("PREMIUM")
public class PremiumSubscriptionEntity extends SubscriptionEntity {
    @Column(name = "advanced_analytics_enabled")
    private boolean advancedAnalyticsEnabled = true;
}