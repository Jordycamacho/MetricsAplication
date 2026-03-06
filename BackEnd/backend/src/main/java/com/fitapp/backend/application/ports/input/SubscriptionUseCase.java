package com.fitapp.backend.application.ports.input;

import com.fitapp.backend.domain.model.SubscriptionModel;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionType;

public interface SubscriptionUseCase {

    /** Obtener suscripción del usuario autenticado */
    SubscriptionModel getMySubscription(Long userId);

    /** Cambiar de plan (sin pago por ahora, admin/manual) */
    SubscriptionModel changePlan(Long userId, SubscriptionType newType, String performedBy, String notes);

    /** Cancelar suscripción — vuelve a FREE al expirar */
    SubscriptionModel cancelSubscription(Long userId, String reason);

    /** Cron: expirar suscripciones vencidas → downgrade a FREE */
    void expireSubscriptions();

    /** Crear suscripción FREE inicial (llamado desde UserService al registrar) */
    SubscriptionModel createFreeSubscription(Long userId);

    /** Verificar si el usuario puede crear más rutinas */
    boolean canCreateRoutine(Long userId, int currentRoutineCount);
}