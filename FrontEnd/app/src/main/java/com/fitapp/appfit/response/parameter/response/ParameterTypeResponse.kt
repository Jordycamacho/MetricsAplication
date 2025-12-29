package com.fitapp.appfit.response.parameter.response

import com.google.gson.annotations.SerializedName

data class ParameterTypeResponse(
    @SerializedName("typeName") val typeName: String,
    @SerializedName("dataType") val dataType: String
)