package com.fitapp.backend.infrastructure.persistence.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("FREE")
public class FreeSubscriptionEntity extends SubscriptionEntity {
    // Limitaciones específicas del plan free
}