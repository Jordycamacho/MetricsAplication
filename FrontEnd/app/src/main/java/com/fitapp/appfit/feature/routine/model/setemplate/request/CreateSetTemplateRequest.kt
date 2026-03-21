package com.fitapp.appfit.feature.routine.model.setemplate.request

import com.fitapp.appfit.feature.routine.model.rutinexercise.request.SetParameterRequest
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