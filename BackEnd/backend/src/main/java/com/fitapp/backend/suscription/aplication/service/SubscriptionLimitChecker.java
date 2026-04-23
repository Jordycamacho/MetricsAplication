package com.fitapp.backend.suscription.aplication.service;

import com.fitapp.backend.domain.exception.SubscriptionLimitException;
import com.fitapp.backend.suscription.aplication.port.input.SubscriptionUseCase;
import com.fitapp.backend.suscription.domain.model.SubscriptionLimitsModel;
import com.fitapp.backend.suscription.domain.model.SubscriptionModel;
import com.fitapp.backend.user.aplication.port.output.UserPersistencePort;
import com.fitapp.backend.user.domain.model.UserModel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionLimitChecker {

    private final SubscriptionUseCase subscriptionUseCase;
    private final UserPersistencePort userPersistencePort;

    private SubscriptionLimitsModel getLimits(String userEmail) {
        UserModel user = userPersistencePort.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + userEmail));
        SubscriptionModel subscription = subscriptionUseCase.getMySubscription(user.getId());
        return subscription.getLimits();
    }

    public void checkRoutineLimit(String userEmail, long currentCount) {
        SubscriptionLimitsModel limits = getLimits(userEmail);
        if (!limits.isWithinLimit((int) currentCount, limits.getMaxRoutines())) {
            log.warn("LIMIT_EXCEEDED | type=ROUTINES | user={} | current={} | max={}",
                    userEmail, currentCount, limits.getMaxRoutines());
            throw new SubscriptionLimitException(
                    "Has alcanzado el límite de " + limits.getMaxRoutines() +
                    " rutinas de tu plan. Actualiza tu suscripción para crear más.");
        }
    }

    public void checkExercisesPerRoutineLimit(String userEmail, long currentCount) {
        SubscriptionLimitsModel limits = getLimits(userEmail);
        if (!limits.isWithinLimit((int) currentCount, limits.getMaxExercisesPerRoutine())) {
            log.warn("LIMIT_EXCEEDED | type=EXERCISES_PER_ROUTINE | user={} | current={} | max={}",
                    userEmail, currentCount, limits.getMaxExercisesPerRoutine());
            throw new SubscriptionLimitException(
                    "Has alcanzado el límite de " + limits.getMaxExercisesPerRoutine() +
                    " ejercicios por rutina de tu plan. Actualiza tu suscripción para añadir más.");
        }
    }

    public void checkSetsPerExerciseLimit(String userEmail, long currentCount) {
        SubscriptionLimitsModel limits = getLimits(userEmail);
        if (!limits.isWithinLimit((int) currentCount, limits.getMaxSetsPerExercise())) {
            log.warn("LIMIT_EXCEEDED | type=SETS_PER_EXERCISE | user={} | current={} | max={}",
                    userEmail, currentCount, limits.getMaxSetsPerExercise());
            throw new SubscriptionLimitException(
                    "Has alcanzado el límite de " + limits.getMaxSetsPerExercise() +
                    " sets por ejercicio de tu plan. Actualiza tu suscripción para añadir más.");
        }
    }

    public void checkCustomExerciseLimit(String userEmail, long currentCount) {
        SubscriptionLimitsModel limits = getLimits(userEmail);
        if (!limits.isWithinLimit((int) currentCount, limits.getMaxCustomExercises())) {
            log.warn("LIMIT_EXCEEDED | type=CUSTOM_EXERCISES | user={} | current={} | max={}",
                    userEmail, currentCount, limits.getMaxCustomExercises());
            throw new SubscriptionLimitException(
                    "Has alcanzado el límite de " + limits.getMaxCustomExercises() +
                    " ejercicios personalizados de tu plan. Actualiza tu suscripción para crear más.");
        }
    }

    public void checkCustomParameterLimit(String userEmail, long currentCount) {
        SubscriptionLimitsModel limits = getLimits(userEmail);
        if (!limits.isWithinLimit((int) currentCount, limits.getMaxCustomParameters())) {
            log.warn("LIMIT_EXCEEDED | type=CUSTOM_PARAMETERS | user={} | current={} | max={}",
                    userEmail, currentCount, limits.getMaxCustomParameters());
            throw new SubscriptionLimitException(
                    "Has alcanzado el límite de " + limits.getMaxCustomParameters() +
                    " parámetros personalizados de tu plan. Actualiza tu suscripción para crear más.");
        }
    }

    public void checkCustomSportLimit(String userEmail, long currentCount) {
        SubscriptionLimitsModel limits = getLimits(userEmail);
        if (!limits.isWithinLimit((int) currentCount, limits.getMaxCustomSports())) {
            log.warn("LIMIT_EXCEEDED | type=CUSTOM_SPORTS | user={} | current={} | max={}",
                    userEmail, currentCount, limits.getMaxCustomSports());
            throw new SubscriptionLimitException(
                    "Has alcanzado el límite de " + limits.getMaxCustomSports() +
                    " deportes personalizados de tu plan. Actualiza tu suscripción para crear más.");
        }
    }

    public void checkCustomCategoryLimit(String userEmail, long currentCount) {
        SubscriptionLimitsModel limits = getLimits(userEmail);
        if (!limits.isWithinLimit((int) currentCount, limits.getMaxCustomCategories())) {
            log.warn("LIMIT_EXCEEDED | type=CUSTOM_CATEGORIES | user={} | current={} | max={}",
                    userEmail, currentCount, limits.getMaxCustomParameters());
            throw new SubscriptionLimitException(
                    "Has alcanzado el límite de " + limits.getMaxCustomCategories() +
                    " categorías personalizadas de tu plan. Actualiza tu suscripción para crear más.");
        }
    }
}