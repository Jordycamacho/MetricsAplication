package com.fitapp.appfit.response.parameter.response

import com.google.gson.annotations.SerializedName

data class CustomParameterResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("displayName") val displayName: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("parameterType") val parameterType: String,
    @SerializedName("unit") val unit: String?,
    @SerializedName("validationRules") val validationRules: Map<String, String>?,
    @SerializedName("isGlobal") val isGlobal: Boolean,
    @SerializedName("isActive") val isActive: Boolean,
    @SerializedName("ownerId") val ownerId: Long?,
    @SerializedName("ownerName") val ownerName: String?,
    @SerializedName("sportId") val sportId: Long?,
    @SerializedName("sportName") val sportName: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?,
    @SerializedName("usageCount") val usageCount: Int = 0
)