package com.fitapp.appfit.feature.routine.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fitapp.appfit.feature.routine.database.entity.RoutineExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineExerciseDao {

    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId AND syncStatus != 'PENDING_DELETE' ORDER BY position ASC")
    fun observeExercises(routineId: Long): Flow<List<RoutineExerciseEntity>>

    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId AND syncStatus != 'PENDING_DELETE' ORDER BY position ASC")
    suspend fun getExercises(routineId: Long): List<RoutineExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<RoutineExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: RoutineExerciseEntity)

    @Update
    suspend fun updateExercise(exercise: RoutineExerciseEntity)

    @Query("UPDATE routine_exercises SET syncStatus = 'PENDING_DELETE' WHERE id = :exerciseId")
    suspend fun markAsDeleted(exerciseId: Long)

    @Query("DELETE FROM routine_exercises WHERE routineId = :routineId AND syncStatus = 'SYNCED'")
    suspend fun deleteSyncedByRoutine(routineId: Long)

    @Query("SELECT * FROM routine_exercises WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingSync(): List<RoutineExerciseEntity>

    @Query("UPDATE routine_exercises SET syncStatus = 'SYNCED' WHERE id = :exerciseId")
    suspend fun markAsSynced(exerciseId: Long)
}