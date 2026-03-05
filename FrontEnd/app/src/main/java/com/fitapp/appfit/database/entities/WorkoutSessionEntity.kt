package com.fitapp.appfit.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.fitapp.appfit.database.Converters
import com.fitapp.appfit.database.entities.enums.SyncStatus

/**
 * Representa una sesión de entrenamiento ejecutada por el usuario.
 *
 * Una sesión está ligada a una rutina específica. Cuando el usuario
 * pulsa "Guardar" en WorkoutFragment, se crea una WorkoutSessionEntity.
 *
 * syncStatus:
 *  - PENDING_CREATE → guardada offline, aún no enviada al back
 *  - SYNCED         → ya persistida en el servidor
 */
@Entity(
    tableName = "workout_sessions",
    foreignKeys = [ForeignKey(
        entity = RoutineEntity::class,
        parentColumns = ["id"],
        childColumns = ["routineId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index("routineId"),
        Index("syncStatus")
    ]
)
@TypeConverters(Converters::class)
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val routineId: Long,

    val userId: String,

    /** Timestamp de inicio del entrenamiento (epoch ms) */
    val startedAt: Long = System.currentTimeMillis(),

    /** Timestamp de fin — se rellena al guardar */
    val finishedAt: Long = System.currentTimeMillis(),

    /** Notas libres del usuario (opcional, para v2) */
    val notes: String? = null,

    val syncStatus: SyncStatus = SyncStatus.PENDING_CREATE,

    val lastModifiedLocally: Long = System.currentTimeMillis()
)