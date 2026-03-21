package com.fitapp.appfit.core.database

import androidx.room.TypeConverter
import com.fitapp.appfit.shared.enums.SyncStatus

class Converters {
    @TypeConverter
    fun fromSyncStatus(value: SyncStatus): String = value.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = enumValueOf(value)
}