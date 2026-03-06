package com.fitapp.backend.infrastructure.config.data;

import com.fitapp.backend.infrastructure.persistence.entity.SubscriptionLimitsEntity;
import com.fitapp.backend.infrastructure.persistence.repository.SpringDataSubscriptionLimitsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionLimitsDataLoader implements ApplicationRunner {

    private final SpringDataSubscriptionLimitsRepository limitsRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (limitsRepository.count() > 0) {
            log.info("SubscriptionLimits ya inicializados, saltando seed.");
            return;
        }

        log.info("Inicializando subscription_limits...");

        limitsRepository.save(SubscriptionLimitsEntity.builder()
                .tier("FREE")
                .maxRoutines(2)
                .maxExercisesPerRoutine(30)
                .maxSetsPerExercise(4)
                .maxCustomSports(1)
                .maxCustomParameters(3)
                .maxCustomCategories(2)
                .maxCustomExercises(10)
                .historyDays(30)
                .basicAnalytics(true)
                .advancedAnalytics(false)
                .canExportRoutines(false)
                .canImportRoutines(false)
                .marketplaceRead(false)
                .marketplaceSell(false)
                .freePacksOnly(true)
                .build());

        limitsRepository.save(SubscriptionLimitsEntity.builder()
                .tier("STANDARD")
                .maxRoutines(10)
                .maxExercisesPerRoutine(50)
                .maxSetsPerExercise(10)
                .maxCustomSports(5)
                .maxCustomParameters(10)
                .maxCustomCategories(10)
                .maxCustomExercises(50)
                .historyDays(180)
                .basicAnalytics(true)
                .advancedAnalytics(false)
                .canExportRoutines(true)
                .canImportRoutines(true)
                .marketplaceRead(true)
                .marketplaceSell(false)
                .freePacksOnly(false)
                .build());

        limitsRepository.save(SubscriptionLimitsEntity.builder()
                .tier("PREMIUM")
                .maxRoutines(null)
                .maxExercisesPerRoutine(null)
                .maxSetsPerExercise(null)
                .maxCustomSports(null)
                .maxCustomParameters(null)
                .maxCustomCategories(null)
                .maxCustomExercises(null)
                .historyDays(null)
                .basicAnalytics(true)
                .advancedAnalytics(true)
                .canExportRoutines(true)
                .canImportRoutines(true)
                .marketplaceRead(true)
                .marketplaceSell(true)
                .freePacksOnly(false)
                .build());

        log.info("SubscriptionLimits inicializados correctamente.");
    }
}