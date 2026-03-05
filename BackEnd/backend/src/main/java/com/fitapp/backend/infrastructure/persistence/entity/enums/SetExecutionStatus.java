package com.fitapp.backend.infrastructure.persistence.entity.enums;

public enum SetExecutionStatus {
    COMPLETED,  // ejecutado completo
    SKIPPED,    // saltado intencionalmente
    FAILED,     // intentado pero no completado (ej: fallo muscular antes)
    PARTIAL     // completado parcialmente (menos reps/tiempo del objetivo)
}