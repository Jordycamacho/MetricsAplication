package com.fitapp.appfit.response.sets.request

import com.fitapp.appfit.response.routine.request.SetParameterRequest
import com.google.gson.annotations.SerializedName

data class CreateSetTemplateRequest(
    @SerializedName("routineExerciseId") val routineExerciseId: Long,
    @SerializedName("position") val position: Int,
    @SerializedName("subSetNumber") val subSetNumber: Int? = null,
    @SerializedName("groupId") val groupId: String? = null,
    @SerializedName("setType") val setType: String? = "NORMAL",
    @SerializedName("restAfterSet") val restAfterSet: Int? = null,
    @SerializedName("parameters") val parameters: List<SetParameterRequest>? = null
)