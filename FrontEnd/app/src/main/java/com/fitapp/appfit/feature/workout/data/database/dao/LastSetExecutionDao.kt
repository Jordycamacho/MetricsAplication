package com.fitapp.appfit.feature.workout.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fitapp.appfit.feature.workout.data.database.entity.LastSetExecutionEntity

/**
 * DAO para gestionar los últimos valores de ejecución de sets.
 *
 * Todas las operaciones son locales en SQLite, sin consultas a servidor.
 */
@Dao
interface LastSetExecutionDao {

    /**
     * Inserta o actualiza el último valor de un parámetro de un set.
     * Si ya existe (por clave primaria), lo sobrescribe.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(execution: LastSetExecutionEntity)

    /**
     * Inserta o actualiza múltiples registros de una sola vez.
     * Útil cuando se guarda un entrenamiento completamente.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBatch(executions: List<LastSetExecutionEntity>)

    /**
     * Obtiene todos los últimos valores de ejecución para una rutina.
     * Devuelve un Map: setTemplateId -> parameterId -> entity
     */
    @Query("SELECT * FROM last_set_executions WHERE routineId = :routineId")
    suspend fun getLastExecutionsByRoutine(routineId: Long): List<LastSetExecutionEntity>

    /**
     * Obtiene los últimos valores para un set específico de una rutina.
     * Útil para pre-cargar un set individual bajo demanda.
     */
    @Query(
        "SELECT * FROM last_set_executions " +
                "WHERE routineId = :routineId AND setTemplateId = :setTemplateId"
    )
    suspend fun getLastExecutionsForSet(routineId: Long, setTemplateId: Long): List<LastSetExecutionEntity>

    /**
     * Obtiene el último valor de un parámetro específico de un set.
     */
    @Query(
        "SELECT * FROM last_set_executions " +
                "WHERE routineId = :routineId AND setTemplateId = :setTemplateId AND parameterId = :parameterId"
    )
    suspend fun getLastExecution(
        routineId: Long,
        setTemplateId: Long,
        parameterId: Long
    ): LastSetExecutionEntity?

    /**
     * Elimina todos los registros de una rutina.
     * Útil si el usuario elimina la rutina o quiere reset.
     */
    @Query("DELETE FROM last_set_executions WHERE routineId = :routineId")
    suspend fun deleteByRoutine(routineId: Long)

    /**
     * Elimina registros de un set específico.
     */
    @Query(
        "DELETE FROM last_set_executions " +
                "WHERE routineId = :routineId AND setTemplateId = :setTemplateId"
    )
    suspend fun deleteBySet(routineId: Long, setTemplateId: Long)

    @Query(
        "DELETE FROM last_set_executions " +
                "WHERE routineId = :routineId AND setTemplateId = :setTemplateId AND parameterId = :parameterId"
    )
    suspend fun deleteBySetAndParameter(routineId: Long, setTemplateId: Long, parameterId: Long)

    /**
     * Comprueba si hay registros para una rutina.
     */
    @Query("SELECT COUNT(*) FROM last_set_executions WHERE routineId = :routineId")
    suspend fun countByRoutine(routineId: Long): Int

    /**
     * Obtiene el timestamp del último registro de una rutina.
     * Útil para saber cuándo fue la última ejecución.
     */
    @Query("SELECT MAX(recordedAt) FROM last_set_executions WHERE routineId = :routineId")
    suspend fun getLastRecordedTime(routineId: Long): Long?
}