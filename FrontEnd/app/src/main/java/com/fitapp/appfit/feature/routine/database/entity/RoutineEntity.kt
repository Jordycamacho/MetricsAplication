package com.fitapp.appfit.feature.routine.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.fitapp.appfit.core.database.Converters
import com.fitapp.appfit.shared.enums.SyncStatus

@Entity(tableName = "routines")
@TypeConverters(Converters::class)
data class RoutineEntity(
    @PrimaryKey
    val id: Long,
    val userId: String,
    val name: String,
    val description: String?,
    val sportId: Long?,
    val sportName: String?,
    val isActive: Boolean,
    val goal: String?,
    val sessionsPerWeek: Int?,
    val trainingDays: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val lastUsedAt: String?,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val lastModifiedLocally: Long = System.currentTimeMillis(),
    val pendingPayload: String? = null
)