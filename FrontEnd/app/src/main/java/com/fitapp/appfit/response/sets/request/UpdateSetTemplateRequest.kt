package com.fitapp.appfit.response.sets.request

import com.google.gson.annotations.SerializedName

data class UpdateSetTemplateRequest(
    @SerializedName("position") val position: Int? = null,
    @SerializedName("subSetNumber") val subSetNumber: Int? = null,
    @SerializedName("groupId") val groupId: String? = null,
    @SerializedName("setType") val setType: String? = null,
    @SerializedName("restAfterSet") val restAfterSet: Int? = null,
    @SerializedName("parameters") val parameters: List<UpdateSetParameterRequest>? = null
)