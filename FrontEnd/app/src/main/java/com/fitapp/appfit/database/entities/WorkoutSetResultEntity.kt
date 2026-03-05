package com.fitapp.appfit.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.fitapp.appfit.database.Converters
import com.fitapp.appfit.database.entities.enums.SyncStatus

/**
 * Almacena los valores REALES que ejecutó el usuario en un set concreto
 * durante una sesión de entrenamiento.
 *
 * Relación:
 *  WorkoutSession (1) → (N) WorkoutSetResult
 *
 * Cada fila representa un parámetro de un set template:
 *  - setTemplateId  → referencia al set de la rutina (RoutineSetTemplateEntity)
 *  - parameterId    → qué parámetro se modificó (peso, reps, duración...)
 *  - Los campos de valor replican la estructura de RoutineSetParameterEntity
 *    para mantener consistencia con el back y el BulkUpdateSetParametersRequest
 *
 * syncStatus:
 *  - PENDING_CREATE → pendiente de enviar
 *  - SYNCED         → ya sincronizado con el servidor
 */
@Entity(
    tableName = "workout_set_results",
    foreignKeys = [ForeignKey(
        entity = WorkoutSessionEntity::class,
        parentColumns = ["id"],
        childColumns = ["workoutSessionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index("workoutSessionId"),
        Index("setTemplateId"),
        Index("syncStatus")
    ]
)
@TypeConverters(Converters::class)
data class WorkoutSetResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val workoutSessionId: Long,

    /** ID del set template de la rutina que se ejecutó */
    val setTemplateId: Long,

    /** ID del parámetro (peso, reps, duración, etc.) */
    val parameterId: Long,

    // ── Valores ejecutados (misma estructura que RoutineSetParameterEntity) ──

    val repetitions: Int? = null,
    val numericValue: Double? = null,
    val durationValue: Long? = null,
    val integerValue: Int? = null,

    val syncStatus: SyncStatus = SyncStatus.PENDING_CREATE
)