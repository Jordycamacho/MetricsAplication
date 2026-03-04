package com.fitapp.backend.infrastructure.persistence.entity.enums;

public enum SubscriptionChangeReason {
    UPGRADE,
    DOWNGRADE,
    TRIAL_START,
    TRIAL_END,
    CANCELLATION,
    REACTIVATION,
    PAYMENT_FAILED,
    ADMIN_OVERRIDE,
    PROMO_APPLIED
}