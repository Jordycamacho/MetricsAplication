package com.fitapp.backend.infrastructure.persistence.converter;

import com.fitapp.backend.domain.model.SubscriptionLimitsModel;
import com.fitapp.backend.infrastructure.persistence.entity.SubscriptionLimitsEntity;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionLimitsConverter {

    public SubscriptionLimitsModel toDomain(SubscriptionLimitsEntity entity) {
        if (entity == null) return null;

        return SubscriptionLimitsModel.builder()
                .id(entity.getId())
                .tier(entity.getTier())
                .maxRoutines(entity.getMaxRoutines())
                .maxCustomSports(entity.getMaxCustomSports())
                .maxCustomParameters(entity.getMaxCustomParameters())
                .maxCustomCategories(entity.getMaxCustomCategories())
                .maxCustomExercises(entity.getMaxCustomExercises())
                .historyDays(entity.getHistoryDays())
                .basicAnalytics(entity.isBasicAnalytics())
                .advancedAnalytics(entity.isAdvancedAnalytics())
                .canExportRoutines(entity.isCanExportRoutines())
                .canImportRoutines(entity.isCanImportRoutines())
                .marketplaceRead(entity.isMarketplaceRead())
                .marketplaceSell(entity.isMarketplaceSell())
                .freePacksOnly(entity.isFreePacksOnly())
                .build();
    }
}