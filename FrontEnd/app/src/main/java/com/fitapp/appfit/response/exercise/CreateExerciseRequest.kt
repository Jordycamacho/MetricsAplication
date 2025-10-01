package com.fitapp.appfit.response.exercise

data class CreateExerciseRequest(
    val name: String,
    val description: String? = null,
    val sportId: Long? = null,
    val parameterTemplates: Map<String, String> = emptyMap()
)