package com.fitapp.appfit.response.sport.request

data class SportRequest(
    val name: String,
    val parameterTemplate: Map<String, String>,
    val iconUrl: String? = null,
    val category: String
)