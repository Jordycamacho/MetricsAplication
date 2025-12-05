package com.fitapp.backend.infrastructure.persistence.entity;

import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@DiscriminatorValue("PREMIUM")
public class PremiumSubscriptionEntity extends SubscriptionEntity {
    @Override
    public SubscriptionType getType() {
        return SubscriptionType.PREMIUM;
    }
    
    @Column(name = "max_routines")
    private Integer maxRoutines = null;
    
    @Column(name = "advanced_analytics_enabled")
    private boolean advancedAnalyticsEnabled = true;
    
    @Column(name = "custom_parameters_allowed")
    private boolean customParametersAllowed = true;
}