package com.fitapp.appfit.feature.marketplace.model.request

import com.google.gson.annotations.SerializedName

data class AddPackageItemRequest(
    @SerializedName("itemType")
    val itemType: String, // SPORT, PARAMETER, ROUTINE, EXERCISE, CATEGORY

    @SerializedName("sportId")
    val sportId: Long?,

    @SerializedName("parameterId")
    val parameterId: Long?,

    @SerializedName("routineId")
    val routineId: Long?,

    @SerializedName("exerciseId")
    val exerciseId: Long?,

    @SerializedName("categoryId")
    val categoryId: Long?,

    @SerializedName("displayOrder")
    val displayOrder: Int?,

    @SerializedName("notes")
    val notes: String?
)