package com.fitapp.appfit.feature.workout.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fitapp.appfit.feature.workout.database.entity.WorkoutSetResultEntity

@Dao
interface WorkoutSetResultDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertResults(results: List<WorkoutSetResultEntity>)

    @Query("SELECT * FROM workout_set_results WHERE workoutSessionId = :sessionId")
    suspend fun getResultsBySession(sessionId: Long): List<WorkoutSetResultEntity>

    @Query("""
        SELECT * FROM workout_set_results WHERE workoutSessionId = :sessionId ORDER BY setTemplateId ASC
        """)
    suspend fun getResultsGroupedBySet(sessionId: Long): List<WorkoutSetResultEntity>

    @Query("UPDATE workout_set_results SET syncStatus = 'SYNCED' WHERE workoutSessionId = :sessionId")
    suspend fun markSessionResultsAsSynced(sessionId: Long)

    @Query("DELETE FROM workout_set_results WHERE workoutSessionId = :sessionId")
    suspend fun deleteBySession(sessionId: Long)
}