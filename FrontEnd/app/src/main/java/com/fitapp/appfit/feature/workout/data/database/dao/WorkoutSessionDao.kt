package com.fitapp.appfit.feature.workout.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fitapp.appfit.feature.workout.data.database.entity.WorkoutSessionEntity
import com.fitapp.appfit.shared.enums.SyncStatus

@Dao
interface WorkoutSessionDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertSessions(sessions: List<WorkoutSessionEntity>)

    @Update
    suspend fun updateSession(session: WorkoutSessionEntity)

    @Query("DELETE FROM workout_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)

    @Query("UPDATE workout_sessions SET remoteId = :remoteId, syncStatus = 'SYNCED' WHERE id = :localId")
    suspend fun updateRemoteId(localId: Long, remoteId: Long)

    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions WHERE routineId = :routineId ORDER BY startedAt DESC")
    suspend fun getSessionsByRoutine(routineId: Long): List<WorkoutSessionEntity>

    @Query("SELECT * FROM workout_sessions ORDER BY startedAt DESC")
    suspend fun getAllSessions(): List<WorkoutSessionEntity>

    @Query("SELECT * FROM workout_sessions ORDER BY startedAt DESC LIMIT :limit")
    suspend fun getRecentSessions(limit: Int): List<WorkoutSessionEntity>

    @Query("SELECT * FROM workout_sessions WHERE syncStatus = :status")
    suspend fun getSessionsByStatus(status: SyncStatus): List<WorkoutSessionEntity>

    @Query("SELECT * FROM workout_sessions WHERE syncStatus IN ('PENDING_CREATE', 'PENDING_UPDATE', 'PENDING_DELETE')")
    suspend fun getPendingSync(): List<WorkoutSessionEntity>

    @Query("UPDATE workout_sessions SET syncStatus = :status WHERE id = :sessionId")
    suspend fun updateSyncStatus(sessionId: Long, status: SyncStatus)

    @Query("UPDATE workout_sessions SET syncStatus = 'SYNCED' WHERE id = :sessionId")
    suspend fun markAsSynced(sessionId: Long)

    @Query("SELECT COUNT(*) FROM workout_sessions")
    suspend fun getSessionCount(): Int

    @Query("SELECT COUNT(*) FROM workout_sessions WHERE routineId = :routineId")
    suspend fun getSessionCountByRoutine(routineId: Long): Int

    @Query("DELETE FROM workout_sessions")
    suspend fun deleteAllSessions()
}