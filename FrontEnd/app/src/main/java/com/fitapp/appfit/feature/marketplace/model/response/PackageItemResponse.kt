package com.fitapp.appfit.feature.marketplace.model.response

import com.fitapp.appfit.feature.routine.model.rutine.response.RoutineResponse
import com.fitapp.appfit.feature.exercise.model.exercise.response.ExerciseResponse
import com.fitapp.appfit.feature.sport.model.response.SportResponse
import com.fitapp.appfit.feature.parameter.model.response.CustomParameterResponse
import com.google.gson.annotations.SerializedName

data class PackageItemResponse(
    @SerializedName("id")
    val id: Long,

    @SerializedName("itemType")
    val itemType: String, // SPORT, PARAMETER, ROUTINE, EXERCISE, CATEGORY

    @SerializedName("displayOrder")
    val displayOrder: Int,

    @SerializedName("notes")
    val notes: String?,

    @SerializedName("sport")
    val sport: SportResponse? = null,

    @SerializedName("parameter")
    val parameter: CustomParameterResponse? = null,

    @SerializedName("routine")
    val routine: RoutineResponse? = null,

    @SerializedName("exercise")
    val exercise: ExerciseResponse? = null
)