package com.fitapp.appfit.database.dao

import androidx.room.*
import com.fitapp.appfit.database.entities.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions WHERE userId = :userId ORDER BY finishedAt DESC")
    fun observeSessionsByUser(userId: String): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE routineId = :routineId ORDER BY finishedAt DESC")
    fun observeSessionsByRoutine(routineId: Long): Flow<List<WorkoutSessionEntity>>

    /** Devuelve todas las sesiones pendientes de sincronizar */
    @Query("SELECT * FROM workout_sessions WHERE syncStatus = 'PENDING_CREATE' ORDER BY finishedAt ASC")
    suspend fun getPendingSync(): List<WorkoutSessionEntity>

    @Query("UPDATE workout_sessions SET syncStatus = 'SYNCED' WHERE id = :sessionId")
    suspend fun markAsSynced(sessionId: Long)

    @Query("DELETE FROM workout_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)
}