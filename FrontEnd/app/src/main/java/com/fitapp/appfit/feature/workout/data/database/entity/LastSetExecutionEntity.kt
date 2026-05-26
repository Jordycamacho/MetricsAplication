package com.fitapp.appfit.feature.workout.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Almacena el ÚLTIMO resultado de ejecución de cada set en una rutina.
 *
 * Se actualiza cada vez que el usuario guarda un entrenamiento.
 * Se utiliza para pre-cargar valores en la siguiente sesión SIN hacer consultas al servidor.
 *
 * Clave primaria: (routineId, setTemplateId, parameterId)
 */
@Entity(
    tableName = "last_set_executions",
    primaryKeys = ["routineId", "setTemplateId", "parameterId"],
    foreignKeys = [
        ForeignKey(
            entity = com.fitapp.appfit.feature.routine.database.entity.RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("routineId"),
        Index("setTemplateId"),
        Index("routineId", "setTemplateId")
    ]
)
data class LastSetExecutionEntity(
    val routineId: Long,           // FK a routine
    val setTemplateId: Long,        // ID del template del set
    val parameterId: Long,          // ID del parámetro
    val parameterName: String?,     // Nombre del parámetro (ej: "Repeticiones", "Peso")
    val parameterType: String?,     // Tipo: NUMBER, INTEGER, DURATION, etc.
    val unit: String?,              // Unidad (KG, M, %, etc.)

    // Último valor usado por el usuario
    val lastRepetitions: Int?,
    val lastNumericValue: Double?,
    val lastDurationValue: Long?,
    val lastIntegerValue: Int?,

    // Timestamp de cuándo se registró este valor
    val recordedAt: Long = System.currentTimeMillis()
)