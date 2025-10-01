package com.fitapp.appfit.response.exercise

data class ExerciseResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val sportId: Long?,
    val sportName: String?,
    val isPredefined: Boolean,
    val parameterTemplates: Map<String, String>
)