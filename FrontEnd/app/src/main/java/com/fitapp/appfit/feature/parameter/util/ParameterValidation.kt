package com.fitapp.appfit.feature.parameter.util

object ParameterValidation {

    /**
     * Tipos de parámetros disponibles
     */
    enum class ParameterType(val value: String, val label: String) {
        NUMBER("NUMBER", "Número decimal"),
        INTEGER("INTEGER", "Número entero"),
        TEXT("TEXT", "Texto"),
        BOOLEAN("BOOLEAN", "Sí / No"),
        DURATION("DURATION", "Tiempo / Duración"),
        DISTANCE("DISTANCE", "Distancia"),
        PERCENTAGE("PERCENTAGE", "Porcentaje");

        companion object {
            fun fromString(value: String): ParameterType? {
                return values().find { it.value.equals(value, ignoreCase = true) }
            }

            fun getAllLabels(): List<String> = values().map { it.label }
            fun getAllValues(): List<String> = values().map { it.value }
        }
    }

    /**
     * Tipos de agregación de métricas
     */
    enum class MetricAggregation(val value: String, val label: String, val description: String) {
        MAX("MAX", "Máximo", "Registra el valor máximo alcanzado"),
        MIN("MIN", "Mínimo", "Registra el valor mínimo (mejor tiempo)"),
        AVG("AVG", "Promedio", "Calcula el promedio de valores"),
        SUM("SUM", "Suma total", "Suma todos los valores"),
        LAST("LAST", "Último valor", "Usa el valor más reciente");

        companion object {
            fun fromString(value: String): MetricAggregation? {
                return values().find { it.value.equals(value, ignoreCase = true) }
            }

            fun getAllLabels(): List<String> = values().map { it.label }
        }
    }

    /**
     * Valida el nombre del parámetro
     */
    fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "El nombre no puede estar vacío"
            name.length > 100 -> "El nombre no puede exceder 100 caracteres"
            !name.matches(Regex("^[a-z]+([A-Z][a-z]*)*$")) ->
                "Advertencia: Se recomienda formato camelCase"
            else -> null
        }
    }

    /**
     * Valida la descripción
     */
    fun validateDescription(description: String?): String? {
        return if (description != null && description.length > 500) {
            "La descripción no puede exceder 500 caracteres"
        } else null
    }

    /**
     * Valida la unidad
     */
    fun validateUnit(unit: String?): String? {
        return if (unit != null && unit.length > 20) {
            "La unidad no puede exceder 20 caracteres"
        } else null
    }

    /**
     * Normaliza la unidad según el tipo de parámetro
     */
    fun normalizeUnit(type: String, unit: String?): String? {
        return when (type.uppercase()) {
            "PERCENTAGE" -> "%" // Forzar % para porcentajes
            "BOOLEAN" -> null   // Boolean no tiene unidad
            else -> unit
        }
    }

    /**
     * Determina la agregación por defecto según el tipo
     */
    fun getDefaultAggregation(type: String): String? {
        return when (type.uppercase()) {
            "NUMBER", "INTEGER" -> "MAX"  // Peso, altura, etc
            "DURATION" -> "MIN"            // Mejor tiempo
            "DISTANCE" -> "MAX"            // Mayor distancia
            "PERCENTAGE" -> "AVG"          // Porcentaje promedio
            "BOOLEAN", "TEXT" -> null      // No trackeable
            else -> "MAX"
        }
    }

    /**
     * Determina si un tipo debe ser trackeable por defecto
     */
    fun isTrackableByDefault(type: String): Boolean {
        return type.uppercase() !in listOf("BOOLEAN", "TEXT")
    }

    /**
     * Obtiene el hint para la unidad según el tipo
     */
    fun getUnitHint(type: String): String {
        return when (type.uppercase()) {
            "NUMBER", "INTEGER" -> "ej. kg, lb, cm"
            "DURATION" -> "ej. s, min, h"
            "DISTANCE" -> "ej. m, km, mi"
            "PERCENTAGE" -> "% (automático)"
            "BOOLEAN" -> "(No aplica)"
            "TEXT" -> "(Opcional)"
            else -> "ej. kg, m, s"
        }
    }

    /**
     * Determina si la unidad debe estar deshabilitada según el tipo
     */
    fun isUnitDisabled(type: String): Boolean {
        return type.uppercase() in listOf("BOOLEAN", "PERCENTAGE")
    }

    /**
     * Obtiene mensaje de ayuda para el tipo seleccionado
     */
    fun getTypeHelp(type: String): String? {
        return when (type.uppercase()) {
            "PERCENTAGE" -> "La unidad será automáticamente '%'"
            "BOOLEAN" -> "Se mostrará como interruptor Sí/No"
            "TEXT" -> "Para notas o descripciones (no se calcula métrica)"
            "DURATION" -> "Para tiempos de ejecución o descanso"
            else -> null
        }
    }
}