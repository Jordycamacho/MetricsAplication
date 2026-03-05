package com.fitapp.appfit.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fitapp.appfit.database.entities.enums.SyncStatus

@Entity(
    tableName = "set_templates",
    foreignKeys = [ForeignKey(
        entity = RoutineExerciseEntity::class,
        parentColumns = ["id"],
        childColumns = ["routineExerciseId"],
        onDelete = ForeignKey.CASCADE
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