package com.fitapp.appfit.feature.marketplace.model.enums

enum class PackageStatus(val displayName: String, val color: String) {
    DRAFT("Borrador", "#FF9800"),
    PUBLISHED("Publicado", "#4CAF50"),
    DEPRECATED("Deprecado", "#9C27B0"),
    SUSPENDED("Suspendido", "#F44336");

    companion object {
        fun fromString(value: String?): PackageStatus? {
            return try {
                valueOf(value?.uppercase() ?: return null)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}