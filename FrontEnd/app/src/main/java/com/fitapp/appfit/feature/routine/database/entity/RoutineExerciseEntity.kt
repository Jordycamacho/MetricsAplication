package com.fitapp.appfit.feature.routine.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fitapp.appfit.shared.enums.SyncStatus

@Entity(
    tableName = "routine_exercises",
    foreignKeys = [ForeignKey(
        entity = RoutineEntity::class,
        parentColumns = ["id"],
        childColumns = ["routineId"],
        onDelete = ForeignKey.Companion.CASCADE
    )],
    indices = [Index("routineId")]
)
data class RoutineExerciseEntity(
    @PrimaryKey
    val id: Long,
    val routineId: Long,
    val exerciseId: Long,
    val exerciseName: String,
    val position: Int,
    val sessionNumber: Int?,
    val dayOfWeek: String?,
    val sessionOrder: Int?,
    val restAfterExercise: Int?,
    val sets: Int?,

    // ── v2: Agrupación ────────────────────────────────────────────────────────
    val circuitGroupId: String? = null,
    val circuitRoundCount: Int? = null,
    val superSetGroupId: String? = null,

    // ── v2: Modos especiales ──────────────────────────────────────────────────
    val amrapDurationSeconds: Int? = null,
    val emomIntervalSeconds: Int? = null,
    val emomTotalRounds: Int? = null,
    val tabataWorkSeconds: Int? = null,
    val tabataRestSeconds: Int? = null,
    val tabataRounds: Int? = null,

    // ── v2: Notas ─────────────────────────────────────────────────────────────
    val notes: String? = null,

    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val lastModifiedLocally: Long = System.currentTimeMillis()
)