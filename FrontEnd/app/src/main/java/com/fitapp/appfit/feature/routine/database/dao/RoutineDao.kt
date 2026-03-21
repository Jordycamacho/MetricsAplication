package com.fitapp.appfit.feature.routine.database.dao

import androidx.room.*
import com.fitapp.appfit.core.database.dao.PendingSyncOperation
import com.fitapp.appfit.feature.routine.database.entity.RoutineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
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

    @Query("UPDATE routines SET syncStatus = 'PENDING_DELETE' WHERE id = :routineId")
    suspend fun markAsDeleted(routineId: Long)

    @Query("SELECT * FROM routines WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingSync(): List<RoutineEntity>

    @Query("UPDATE routines SET syncStatus = 'SYNCED', pendingPayload = NULL WHERE id = :routineId")
    suspend fun markAsSynced(routineId: Long)

    @Query("UPDATE routines SET id = :serverId, syncStatus = 'SYNCED' WHERE id = :localId")
    suspend fun updateServerId(localId: Long, serverId: Long)

    @Query("DELETE FROM routines WHERE id = :routineId")
    suspend fun deleteRoutine(routineId: Long)

    @Query("SELECT * FROM routines WHERE isActive = 1 AND userId = :userId AND syncStatus != 'PENDING_DELETE'")
    fun observeActiveRoutines(userId: String): Flow<List<RoutineEntity>>
}

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