package com.fitapp.appfit.feature.marketplace.model.request

import com.google.gson.annotations.SerializedName

data class ReorderPackageItemsRequest (
    @SerializedName("itemIds")
    val itemIds: List<Long>
)