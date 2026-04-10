package com.fitapp.appfit.feature.workout.database.dao

import androidx.room.*
import com.fitapp.appfit.feature.workout.database.entity.WorkoutSetResultEntity
import com.fitapp.appfit.shared.enums.SyncStatus

@Dao
interface WorkoutSetResultDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: WorkoutSetResultEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResults(results: List<WorkoutSetResultEntity>)

    @Update
    suspend fun updateResult(result: WorkoutSetResultEntity)

    @Query("DELETE FROM workout_set_results WHERE id = :resultId")
    suspend fun deleteResult(resultId: Long)

    @Query("DELETE FROM workout_set_results WHERE workoutSessionId = :sessionId")
    suspend fun deleteResultsBySession(sessionId: Long)

    @Query("SELECT * FROM workout_set_results WHERE id = :resultId")
    suspend fun getResultById(resultId: Long): WorkoutSetResultEntity?

    @Query("SELECT * FROM workout_set_results WHERE workoutSessionId = :sessionId")
    suspend fun getResultsBySession(sessionId: Long): List<WorkoutSetResultEntity>

    @Query("SELECT * FROM workout_set_results WHERE setTemplateId = :setTemplateId")
    suspend fun getResultsBySetTemplate(setTemplateId: Long): List<WorkoutSetResultEntity>

    @Query("SELECT * FROM workout_set_results WHERE syncStatus = :status")
    suspend fun getResultsByStatus(status: SyncStatus): List<WorkoutSetResultEntity>

    @Query("SELECT * FROM workout_set_results WHERE syncStatus IN ('PENDING_CREATE', 'PENDING_UPDATE', 'PENDING_DELETE')")
    suspend fun getPendingSync(): List<WorkoutSetResultEntity>

    @Query("UPDATE workout_set_results SET syncStatus = :status WHERE id = :resultId")
    suspend fun updateSyncStatus(resultId: Long, status: SyncStatus)

    @Query("UPDATE workout_set_results SET syncStatus = 'SYNCED' WHERE workoutSessionId = :sessionId")
    suspend fun markSessionResultsAsSynced(sessionId: Long)

    @Query("SELECT COUNT(*) FROM workout_set_results")
    suspend fun getResultCount(): Int

    @Query("SELECT COUNT(*) FROM workout_set_results WHERE workoutSessionId = :sessionId")
    suspend fun getResultCountBySession(sessionId: Long): Int

    @Query("DELETE FROM workout_set_results")
    suspend fun deleteAllResults()

    @Query("""
        SELECT wsr.*
        FROM workout_set_results wsr
        INNER JOIN workout_sessions ws ON wsr.workoutSessionId = ws.id
        WHERE ws.routineId = :routineId
        AND ws.id = (
            SELECT id FROM workout_sessions 
            WHERE routineId = :routineId 
            ORDER BY finishedAt DESC 
            LIMIT 1
        )
    """)
    suspend fun getLastWorkoutResults(routineId: Long): List<WorkoutSetResultEntity>

    @Query("""
        SELECT wsr.* 
        FROM workout_set_results wsr
        INNER JOIN workout_sessions ws ON wsr.workoutSessionId = ws.id
        WHERE wsr.setTemplateId = :setTemplateId 
        AND ws.routineId = :routineId 
        AND ws.id = (
            SELECT id FROM workout_sessions 
            WHERE routineId = :routineId 
            ORDER BY finishedAt DESC 
            LIMIT 1
        )
    """)
    suspend fun getLastResultsForSet(setTemplateId: Long, routineId: Long): List<WorkoutSetResultEntity>

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM workout_sessions 
            WHERE routineId = :routineId 
            LIMIT 1
        )
    """)
    suspend fun hasLastWorkout(routineId: Long): Boolean
}