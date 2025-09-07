package com.fitapp.appfit.response.sport

data class SportRequest(
    val name: String,
    val parameterTemplate: Map<String, String>,
    val iconUrl: String? = null,
    val category: String
)