package com.fitapp.appfit.feature.routine.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fitapp.appfit.feature.routine.database.entity.SetParameterEntity

@Dao
interface SetParameterDao {

    @Query("SELECT * FROM set_parameters WHERE setTemplateId = :setTemplateId")
    suspend fun getParameters(setTemplateId: Long): List<SetParameterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParameters(params: List<SetParameterEntity>)

    @Query("DELETE FROM set_parameters WHERE setTemplateId = :setTemplateId")
    suspend fun deleteBySetTemplate(setTemplateId: Long)
}