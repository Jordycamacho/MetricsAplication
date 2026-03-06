package com.fitapp.backend.application.dto.subscription.request;

import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePlanRequest {
    @NotNull
    private SubscriptionType newType;
    private String notes;
}