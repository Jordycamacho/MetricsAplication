package com.fitapp.backend.infrastructure.persistence.converter;

import com.fitapp.backend.domain.model.FreeSubscriptionModel;
import com.fitapp.backend.domain.model.PremiumSubscriptionModel;
import com.fitapp.backend.domain.model.StandardSubscriptionModel;
import com.fitapp.backend.domain.model.SubscriptionModel;
import com.fitapp.backend.infrastructure.persistence.entity.FreeSubscriptionEntity;
import com.fitapp.backend.infrastructure.persistence.entity.PremiumSubscriptionEntity;
import com.fitapp.backend.infrastructure.persistence.entity.StandardSubscriptionEntity;
import com.fitapp.backend.infrastructure.persistence.entity.SubscriptionEntity;
import com.fitapp.backend.infrastructure.persistence.entity.UserEntity;

public class SubscriptionConverter {

    public static SubscriptionModel toDomain(SubscriptionEntity entity) {
   
        SubscriptionModel model;
        Long userId = entity.getUser() != null ? entity.getUser().getId() : null;

        switch (entity.getType()) {
            case FREE:
                model = FreeSubscriptionModel.builder()
                        .id(entity.getId())
                        .userId(userId)
                        .startDate(entity.getStartDate())
                        .endDate(entity.getEndDate())
                        .build();
                break;
            case STANDARD:
                StandardSubscriptionEntity stdEntity = (StandardSubscriptionEntity) entity;
                model = StandardSubscriptionModel.builder()
                        .id(stdEntity.getId())
                        .userId(userId)
                        .startDate(stdEntity.getStartDate())
                        .endDate(stdEntity.getEndDate())
                        .basicAnalyticsEnabled(stdEntity.isBasicAnalyticsEnabled())
                        .build();
                break;
            case PREMIUM:
                PremiumSubscriptionEntity premEntity = (PremiumSubscriptionEntity) entity;
                model = PremiumSubscriptionModel.builder()
                        .id(premEntity.getId())
                        .userId(userId)
                        .startDate(premEntity.getStartDate())
                        .endDate(premEntity.getEndDate())
                        .advancedAnalyticsEnabled(premEntity.isAdvancedAnalyticsEnabled())
                        .maxRoutines(premEntity.getMaxRoutines())
                        .build();
                break;
            default:
                throw new IllegalArgumentException("Tipo de suscripción no soportado");
        }

        return model;
    }

    public static SubscriptionEntity toEntity(SubscriptionModel model, UserEntity user) {
        SubscriptionEntity entity;

        switch (model.getType()) {
            case FREE:
                entity = new FreeSubscriptionEntity();
                ((FreeSubscriptionEntity) entity).setMaxRoutines(((FreeSubscriptionModel) model).getMaxRoutines());
                break;
            case STANDARD:
                StandardSubscriptionModel stdModel = (StandardSubscriptionModel) model;
                StandardSubscriptionEntity stdEntity = new StandardSubscriptionEntity();
                stdEntity.setBasicAnalyticsEnabled(stdModel.isBasicAnalyticsEnabled());
                entity = stdEntity;
                break;
            case PREMIUM:
                PremiumSubscriptionModel premModel = (PremiumSubscriptionModel) model;
                PremiumSubscriptionEntity premEntity = new PremiumSubscriptionEntity();
                premEntity.setAdvancedAnalyticsEnabled(premModel.isAdvancedAnalyticsEnabled());
                premEntity.setMaxRoutines(premModel.getMaxRoutines());
                entity = premEntity;
                break;
            default:
                throw new IllegalArgumentException("Tipo de suscripción no soportado");
        }

        entity.setId(model.getId());
        entity.setUser(user);
        entity.setStartDate(model.getStartDate());
        entity.setEndDate(model.getEndDate());

        return entity;
    }
}