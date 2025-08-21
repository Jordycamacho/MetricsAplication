package com.fitapp.backend.domain.model;

import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionType;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class FreeSubscriptionModel extends SubscriptionModel {
    @Builder.Default
    private Integer maxRoutines = 3;

    @Override
    public SubscriptionType getType() {
        return SubscriptionType.FREE;
    }
}