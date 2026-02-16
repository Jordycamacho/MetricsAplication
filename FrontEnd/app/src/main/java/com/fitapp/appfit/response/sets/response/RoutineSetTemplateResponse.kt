package com.fitapp.appfit.response.sets.response

import com.google.gson.annotations.SerializedName

data class RoutineSetTemplateResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("routineExerciseId") val routineExerciseId: Long,
    @SerializedName("position") val position: Int,
    @SerializedName("subSetNumber") val subSetNumber: Int?,
    @SerializedName("groupId") val groupId: String?,
    @SerializedName("setType") val setType: String?,
    @SerializedName("restAfterSet") val restAfterSet: Int?,
    @SerializedName("parameters") val parameters: List<RoutineSetParameterResponse>?
)