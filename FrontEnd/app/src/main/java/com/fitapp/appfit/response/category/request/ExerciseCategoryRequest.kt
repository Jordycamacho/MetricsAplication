package com.fitapp.appfit.response.category.request

import com.google.gson.annotations.SerializedName

data class ExerciseCategoryRequest(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("isPublic") val isPublic: Boolean? = false,
    @SerializedName("sportId") val sportId: Long? = null,
    @SerializedName("parentCategoryId") val parentCategoryId: Long? = null,
    @SerializedName("displayOrder") val displayOrder: Int? = null
)