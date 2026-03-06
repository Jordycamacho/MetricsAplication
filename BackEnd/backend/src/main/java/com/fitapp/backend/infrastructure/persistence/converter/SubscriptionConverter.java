package com.fitapp.backend.infrastructure.persistence.converter;

import com.fitapp.backend.domain.model.SubscriptionModel;
import com.fitapp.backend.infrastructure.persistence.entity.SubscriptionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubscriptionConverter {

    private final SubscriptionLimitsConverter limitsConverter;

    public SubscriptionModel toDomain(SubscriptionEntity entity) {
        if (entity == null) return null;

        return SubscriptionModel.builder()
                .id(entity.getId())
                .type(entity.getSubscriptionType())
                .status(entity.getStatus())
                .limits(limitsConverter.toDomain(entity.getLimits()))
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .trialEndsAt(entity.getTrialEndsAt())
                .autoRenew(entity.isAutoRenew())
                .paymentProvider(entity.getPaymentProvider())
                .externalSubscriptionId(entity.getExternalSubscriptionId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .cancelledAt(entity.getCancelledAt())
                .cancelReason(entity.getCancelReason())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public void updateEntityFromModel(SubscriptionEntity entity, SubscriptionModel model) {
        entity.setSubscriptionType(model.getType());
        entity.setStatus(model.getStatus());
        entity.setStartDate(model.getStartDate());
        entity.setEndDate(model.getEndDate());
        entity.setTrialEndsAt(model.getTrialEndsAt());
        entity.setAutoRenew(model.isAutoRenew());
        entity.setPaymentProvider(model.getPaymentProvider());
        entity.setExternalSubscriptionId(model.getExternalSubscriptionId());
        entity.setCancelledAt(model.getCancelledAt());
        entity.setCancelReason(model.getCancelReason());
    }
}