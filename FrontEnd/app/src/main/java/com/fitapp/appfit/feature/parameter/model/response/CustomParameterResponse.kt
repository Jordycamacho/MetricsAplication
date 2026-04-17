package com.fitapp.appfit.feature.parameter.model.response

import com.google.gson.annotations.SerializedName

data class CustomParameterResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("parameterType") val parameterType: String,
    @SerializedName("unit") val unit: String?,
    @SerializedName("isGlobal") val isGlobal: Boolean,
    @SerializedName("isActive") val isActive: Boolean,
    @SerializedName("ownerId") val ownerId: Long?,
    @SerializedName("ownerName") val ownerName: String?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?,
    @SerializedName("usageCount") val usageCount: Int = 0,
    @SerializedName("isFavorite") val isFavorite: Boolean = false,
    @SerializedName("metricAggregation") val metricAggregation: String?,
    @SerializedName("isTrackable") val isTrackable: Boolean = true
) {
    /**
     * Retorna un label legible para el tipo de agregación
     */
    fun getAggregationLabel(): String? = when (metricAggregation?.uppercase()) {
        "MAX" -> "Máximo"
        "MIN" -> "Mínimo"
        "AVG" -> "Promedio"
        "SUM" -> "Suma"
        "LAST" -> "Último valor"
        else -> null
    }

    /**
     * Determina si este parámetro puede usarse para calcular métricas
     */
    fun isMetricCalculable(): Boolean {
        return isTrackable &&
                metricAggregation != null &&
                parameterType !in listOf("TEXT", "BOOLEAN")
    }
}