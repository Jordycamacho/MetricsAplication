package com.fitapp.appfit.database.dao

import androidx.room.*
import com.fitapp.appfit.database.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {

    // Lectura reactiva — la UI se actualiza automáticamente
    @Query("SELECT * FROM routines WHERE userId = :userId AND syncStatus != 'PENDING_DELETE' ORDER BY lastModifiedLocally DESC")
    fun observeRoutines(userId: String): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines WHERE id = :routineId AND userId = :userId AND syncStatus != 'PENDING_DELETE'")
    fun observeRoutine(routineId: Long, userId: String): Flow<RoutineEntity?>

    @Query("SELECT * FROM routines WHERE userId = :userId AND syncStatus != 'PENDING_DELETE'")
    suspend fun getRoutines(userId: String): List<RoutineEntity>

    @Query("SELECT * FROM routines WHERE id = :routineId")
    suspend fun getRoutineById(routineId: Long): RoutineEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutines(routines: List<RoutineEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity)

    @Update
    suspend fun updateRoutine(routine: RoutineEntity)

    @Query("DELETE FROM routines WHERE userId = :userId AND syncStatus = 'SYNCED'")
    suspend fun deleteSyncedRoutines(userId: String)

    // Marcar como pendiente de borrar (no borrar físicamente hasta sync)
    @Query("UPDATE routines SET syncStatus = 'PENDING_DELETE' WHERE id = :routineId")
    suspend fun markAsDeleted(routineId: Long)

    // Obtener todos los que necesitan sincronización
    @Query("SELECT * FROM routines WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingSync(): List<RoutineEntity>

    // Marcar como sincronizado tras subir al servidor
    @Query("UPDATE routines SET syncStatus = 'SYNCED', pendingPayload = NULL WHERE id = :routineId")
    suspend fun markAsSynced(routineId: Long)

    // Actualizar ID local a ID del servidor (tras CREATE exitoso)
    @Query("UPDATE routines SET id = :serverId, syncStatus = 'SYNCED' WHERE id = :localId")
    suspend fun updateServerId(localId: Long, serverId: Long)

    @Query("DELETE FROM routines WHERE id = :routineId")
    suspend fun deleteRoutine(routineId: Long)

    @Query("SELECT * FROM routines WHERE isActive = 1 AND userId = :userId AND syncStatus != 'PENDING_DELETE'")
    fun observeActiveRoutines(userId: String): Flow<List<RoutineEntity>>
}

// ─────────────────────────────────────────────
//  RoutineExerciseDao
// ─────────────────────────────────────────────
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

// ─────────────────────────────────────────────
//  SetTemplateDao
// ─────────────────────────────────────────────
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

// ─────────────────────────────────────────────
//  SetParameterDao
// ─────────────────────────────────────────────
@Dao
interface SetParameterDao {

    @Query("SELECT * FROM set_parameters WHERE setTemplateId = :setTemplateId")
    suspend fun getParameters(setTemplateId: Long): List<SetParameterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParameters(params: List<SetParameterEntity>)

    @Query("DELETE FROM set_parameters WHERE setTemplateId = :setTemplateId")
    suspend fun deleteBySetTemplate(setTemplateId: Long)
}

// ─────────────────────────────────────────────
//  PendingSyncDao
// ─────────────────────────────────────────────
@Dao
interface PendingSyncDao {

    @Insert
    suspend fun enqueue(operation: PendingSyncOperation)

    @Query("SELECT * FROM pending_sync_operations ORDER BY createdAt ASC")
    suspend fun getAllPending(): List<PendingSyncOperation>

    @Delete
    suspend fun delete(operation: PendingSyncOperation)

    @Query("UPDATE pending_sync_operations SET retryCount = retryCount + 1, lastError = :error WHERE operationId = :id")
    suspend fun incrementRetry(id: Long, error: String)

    @Query("DELETE FROM pending_sync_operations WHERE entityId = :entityId AND entityType = :entityType")
    suspend fun deleteByEntity(entityId: Long, entityType: String)

    @Query("SELECT COUNT(*) FROM pending_sync_operations")
    fun observePendingCount(): Flow<Int>
}