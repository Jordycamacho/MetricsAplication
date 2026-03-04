package com.fitapp.appfit.response.sets.request

import com.google.gson.annotations.SerializedName

data class BulkUpdateSetParametersRequest(
    @SerializedName("setResults") val setResults: List<SetResultRequest>
) {
    data class SetResultRequest(
        @SerializedName("setTemplateId") val setTemplateId: Long,
        @SerializedName("parameters") val parameters: List<ParameterResultRequest>
    )

    data class ParameterResultRequest(
        @SerializedName("parameterId") val parameterId: Long,
        @SerializedName("repetitions") val repetitions: Int?,
        @SerializedName("numericValue") val numericValue: Double?,
        @SerializedName("durationValue") val durationValue: Long?,
        @SerializedName("integerValue") val integerValue: Int?
    )
}