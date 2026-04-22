package com.fitapp.appfit.feature.routine.model.rutinexercise.request

import com.google.gson.annotations.SerializedName

data class AddExerciseToRoutineRequest(
    @SerializedName("exerciseId") val exerciseId: Long,
    @SerializedName("sessionNumber") val sessionNumber: Int? = 1,
    @SerializedName("dayOfWeek") val dayOfWeek: String? = null,
    @SerializedName("sessionOrder") val sessionOrder: Int? = null,
    @SerializedName("restAfterExercise") val restAfterExercise: Int? = null,
    @SerializedName("targetParameters") val targetParameters: List<ExerciseParameterRequest>? = null,
    @SerializedName("sets") val sets: List<SetTemplateRequest>? = null,

    // ── v2: Agrupación ────────────────────────────────────────────────────────
    @SerializedName("circuitGroupId") val circuitGroupId: String? = null,
    @SerializedName("circuitRoundCount") val circuitRoundCount: Int? = null,
    @SerializedName("superSetGroupId") val superSetGroupId: String? = null,

    // ── v2: Modos especiales ──────────────────────────────────────────────────
    @SerializedName("amrapDurationSeconds") val amrapDurationSeconds: Int? = null,
    @SerializedName("emomIntervalSeconds") val emomIntervalSeconds: Int? = null,
    @SerializedName("emomTotalRounds") val emomTotalRounds: Int? = null,
    @SerializedName("tabataWorkSeconds") val tabataWorkSeconds: Int? = null,
    @SerializedName("tabataRestSeconds") val tabataRestSeconds: Int? = null,
    @SerializedName("tabataRounds") val tabataRounds: Int? = null,

    // ── v2: Notas ─────────────────────────────────────────────────────────────
    @SerializedName("notes") val notes: String? = null
)

data class ExerciseParameterRequest(
    @SerializedName("parameterId") val parameterId: Long,
    @SerializedName("numericValue") val numericValue: Double? = null,
    @SerializedName("integerValue") val integerValue: Int? = null,
    @SerializedName("durationValue") val durationValue: Long? = null,
    @SerializedName("stringValue") val stringValue: String? = null,
    @SerializedName("minValue") val minValue: Double? = null,
    @SerializedName("maxValue") val maxValue: Double? = null,
    @SerializedName("defaultValue") val defaultValue: Double? = null
)

data class SetTemplateRequest(
    @SerializedName("position") val position: Int,
    @SerializedName("subSetNumber") val subSetNumber: Int? = null,
    @SerializedName("groupId") val groupId: String? = null,
    @SerializedName("setType") val setType: String? = null,
    @SerializedName("restAfterSet") val restAfterSet: Int? = null,
    @SerializedName("parameters") val parameters: List<SetParameterRequest>? = null
)

data class SetParameterRequest(
    @SerializedName("parameterId") val parameterId: Long,
    @SerializedName("repetitions") val repetitions: Int? = null,
    @SerializedName("numericValue") val numericValue: Double? = null,
    @SerializedName("durationValue") val durationValue: Long? = null,
    @SerializedName("integerValue") val integerValue: Int? = null
)