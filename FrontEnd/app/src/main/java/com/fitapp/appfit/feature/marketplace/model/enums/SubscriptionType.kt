package com.fitapp.appfit.feature.marketplace.model.enums

enum class SubscriptionType(val displayName: String) {
    FREE("Gratuito"),
    STANDARD("Estándar"),
    PREMIUM("Premium");

    companion object {
        fun fromString(value: String?): SubscriptionType? {
            return try {
                valueOf(value?.uppercase() ?: return null)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}