package com.fitapp.appfit.core.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object DateUtils {

    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun parseDateTime(dateTimeString: String?): LocalDateTime? {
        return if (dateTimeString != null && dateTimeString.isNotBlank()) {
            try {
                LocalDateTime.parse(dateTimeString, dateTimeFormatter)
            } catch (e: DateTimeParseException) {
                null
            }
        } else {
            null
        }
    }

    fun formatDateTime(dateTime: LocalDateTime?): String {
        return dateTime?.format(dateTimeFormatter) ?: ""
    }

    fun formatForDisplay(dateTimeString: String?): String {
        val dateTime = parseDateTime(dateTimeString)
        return if (dateTime != null) {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            dateTime.format(formatter)
        } else {
            "Nunca"
        }
    }
}