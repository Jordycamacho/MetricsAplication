package com.fitapp.appfit.feature.workout.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.fitapp.appfit.core.database.Converters
import com.fitapp.appfit.shared.enums.SyncStatus
import com.fitapp.appfit.feature.routine.database.entity.RoutineEntity

@Entity(
    tableName = "workout_sessions",
    foreignKeys = [ForeignKey(
        entity = RoutineEntity::class,
        parentColumns = ["id"],
        childColumns = ["routineId"],
        onDelete = ForeignKey.Companion.CASCADE
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

    val startedAt: Long = System.currentTimeMillis(),

    val finishedAt: Long = System.currentTimeMillis(),

    val notes: String? = null,

    val syncStatus: SyncStatus = SyncStatus.PENDING_CREATE,

    val lastModifiedLocally: Long = System.currentTimeMillis()
)