package com.fitapp.appfit.response.sport.response

data class SportResponse(
    val id: Long,
    val name: String,
    val isPredefined: Boolean,
    val parameterTemplate: Map<String, String>,
    val iconUrl: String?,
    val category: String
)