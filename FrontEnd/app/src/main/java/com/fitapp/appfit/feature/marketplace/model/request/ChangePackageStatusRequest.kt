package com.fitapp.appfit.feature.marketplace.model.request

import com.google.gson.annotations.SerializedName

data class ChangePackageStatusRequest (

    @SerializedName("status")
    val status: String,

    @SerializedName("reason")
    val reason: String
)