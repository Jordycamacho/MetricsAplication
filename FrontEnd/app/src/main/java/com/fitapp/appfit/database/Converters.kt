package com.fitapp.appfit.database

import androidx.room.TypeConverter
import com.fitapp.appfit.database.entities.enums.SyncStatus

class Converters {
    @TypeConverter
    fun fromSyncStatus(value: SyncStatus): String = value.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = enumValueOf(value)
}