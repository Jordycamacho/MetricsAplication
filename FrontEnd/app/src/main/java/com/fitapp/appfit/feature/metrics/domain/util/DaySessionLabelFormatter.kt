package com.fitapp.appfit.feature.metrics.domain.util

import com.fitapp.appfit.feature.metrics.domain.model.DaySessionLabel
import java.time.DayOfWeek
import java.util.Locale

object DaySessionLabelFormatter {

    private val DAY_NAMES_ES = mapOf(
        DayOfWeek.MONDAY.name to "Lunes",
        DayOfWeek.TUESDAY.name to "Martes",
        DayOfWeek.WEDNESDAY.name to "Miércoles",
        DayOfWeek.THURSDAY.name to "Jueves",
        DayOfWeek.FRIDAY.name to "Viernes",
        DayOfWeek.SATURDAY.name to "Sábado",
        DayOfWeek.SUNDAY.name to "Domingo"
    )

    fun from(dayOfWeek: String?, sessionNumber: Int?, dayLabel: String? = null): DaySessionLabel {
        if (!dayLabel.isNullOrBlank()) {
            return DaySessionLabel(dayOfWeek, sessionNumber, dayLabel)
        }
        val parts = mutableListOf<String>()
        dayOfWeek?.let { DAY_NAMES_ES[it] ?: it }?.let { parts.add(it) }
        sessionNumber?.takeIf { it > 0 }?.let { parts.add("Sesión $it") }
        val display = when {
            parts.isNotEmpty() -> parts.joinToString(" · ")
            else -> "Sesión completa"
        }
        return DaySessionLabel(dayOfWeek, sessionNumber, display)
    }

    fun shortLabel(dayOfWeek: String?, sessionNumber: Int?, dayLabel: String? = null): String =
        from(dayOfWeek, sessionNumber, dayLabel).displayLabel
}
