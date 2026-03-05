package com.fitapp.backend.infrastructure.persistence.entity.enums;

public enum MetricAggregation {
    SUM,    // suma total — ej: volumen total, reps totales, distancia total
    MAX,    // máximo — ej: peso máximo (PR), mejor tiempo, mayor distancia
    MIN,    // mínimo — ej: tiempo mínimo en ejercicio de velocidad
    AVG,    // media — ej: RPE medio, velocidad media
    LAST    // último valor registrado — ej: 1RM estimado
}