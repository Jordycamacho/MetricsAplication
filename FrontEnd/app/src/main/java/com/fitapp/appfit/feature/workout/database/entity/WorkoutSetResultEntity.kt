package com.fitapp.appfit.feature.workout.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.fitapp.appfit.core.database.Converters
import com.fitapp.appfit.shared.enums.SyncStatus

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
        onDelete = ForeignKey.Companion.CASCADE
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
    val setTemplateId: Long,
    val parameterId: Long,
    val repetitions: Int? = null,
    val numericValue: Double? = null,
    val durationValue: Long? = null,
    val integerValue: Int? = null,

    val syncStatus: SyncStatus = SyncStatus.PENDING_CREATE
)