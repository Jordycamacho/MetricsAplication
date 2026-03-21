package com.fitapp.appfit.feature.exercise.model.exercise.response

import com.google.gson.annotations.SerializedName

enum class ExerciseType {
    @SerializedName("SIMPLE")
    SIMPLE,

    @SerializedName("WEIGHTED")
    WEIGHTED,

    @SerializedName("TIMED")
    TIMED,

    @SerializedName("MIXED")
    MIXED,

    @SerializedName("BODYWEIGHT")
    BODYWEIGHT,

    @SerializedName("DISTANCE")
    DISTANCE,

    @SerializedName("REPETITION")
    REPETITION,

    @SerializedName("DURATION")
    DURATION,

    @SerializedName("CIRCUIT")
    CIRCUIT,

    @SerializedName("AMRAP")
    AMRAP,

    @SerializedName("EMOM")
    EMOM,

    @SerializedName("TABATA")
    TABATA
}