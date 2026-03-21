package com.fitapp.appfit.feature.routine.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fitapp.appfit.shared.enums.SyncStatus

@Entity(
    tableName = "set_templates",
    foreignKeys = [ForeignKey(
        entity = RoutineExerciseEntity::class,
        parentColumns = ["id"],
        childColumns = ["routineExerciseId"],
        onDelete = ForeignKey.Companion.CASCADE
    )],
    indices = [Index("routineExerciseId")]
)
data class SetTemplateEntity(
    @PrimaryKey
    val id: Long,
    val routineExerciseId: Long,
    val position: Int,
    val subSetNumber: Int?,
    val groupId: String?,
    val setType: String?,
    val restAfterSet: Int?,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val lastModifiedLocally: Long = System.currentTimeMillis()
)