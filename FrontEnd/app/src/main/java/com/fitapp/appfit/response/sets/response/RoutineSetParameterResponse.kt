package com.fitapp.appfit.response.sets.response

data class RoutineSetParameterResponse(
    val id: Long,
    val setTemplateId: Long,
    val parameterId: Long,
    val parameterName: String?,
    val parameterType: String?,
    val unit: String?,
    val numericValue: Double?,
    val durationValue: Long?,
    val integerValue: Int?,
    val minValue: Double?,
    val maxValue: Double?,
)