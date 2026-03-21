package com.fitapp.appfit.feature.routine.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fitapp.appfit.feature.routine.database.entity.SetTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SetTemplateDao {

    @Query("SELECT * FROM set_templates WHERE routineExerciseId = :exerciseId AND syncStatus != 'PENDING_DELETE' ORDER BY position ASC")
    fun observeSets(exerciseId: Long): Flow<List<SetTemplateEntity>>

    @Query("SELECT * FROM set_templates WHERE routineExerciseId = :exerciseId AND syncStatus != 'PENDING_DELETE' ORDER BY position ASC")
    suspend fun getSets(exerciseId: Long): List<SetTemplateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(sets: List<SetTemplateEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: SetTemplateEntity)

    @Update
    suspend fun updateSet(set: SetTemplateEntity)

    @Query("UPDATE set_templates SET syncStatus = 'PENDING_DELETE' WHERE id = :setId")
    suspend fun markAsDeleted(setId: Long)

    @Query("DELETE FROM set_templates WHERE routineExerciseId IN (SELECT id FROM routine_exercises WHERE routineId = :routineId) AND syncStatus = 'SYNCED'")
    suspend fun deleteSyncedByRoutine(routineId: Long)

    @Query("SELECT * FROM set_templates WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingSync(): List<SetTemplateEntity>

    @Query("UPDATE set_templates SET syncStatus = 'SYNCED' WHERE id = :setId")
    suspend fun markAsSynced(setId: Long)
}