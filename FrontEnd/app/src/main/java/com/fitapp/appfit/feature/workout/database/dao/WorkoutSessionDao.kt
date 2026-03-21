package com.fitapp.appfit.feature.workout.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fitapp.appfit.feature.workout.database.entity.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions WHERE userId = :userId ORDER BY finishedAt DESC")
    fun observeSessionsByUser(userId: String): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE routineId = :routineId ORDER BY finishedAt DESC")
    fun observeSessionsByRoutine(routineId: Long): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE syncStatus = 'PENDING_CREATE' ORDER BY finishedAt ASC")
    suspend fun getPendingSync(): List<WorkoutSessionEntity>

    @Query("UPDATE workout_sessions SET syncStatus = 'SYNCED' WHERE id = :sessionId")
    suspend fun markAsSynced(sessionId: Long)

    @Query("DELETE FROM workout_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)
}