package com.fitapp.appfit.feature.routine.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fitapp.appfit.shared.enums.SyncStatus

@Entity(
    tableName = "set_parameters",
    foreignKeys = [ForeignKey(
        entity = SetTemplateEntity::class,
        parentColumns = ["id"],
        childColumns = ["setTemplateId"],
        onDelete = ForeignKey.Companion.CASCADE
    )],
    indices = [Index("setTemplateId")]
)
data class SetParameterEntity(
    @PrimaryKey
    val id: Long,
    val setTemplateId: Long,
    val parameterId: Long?,
    val parameterName: String?,
    val parameterType: String?,
    val unit: String?,
    val numericValue: Double?,
    val durationValue: Long?,
    val integerValue: Int?,
    val repetitions: Int?,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)