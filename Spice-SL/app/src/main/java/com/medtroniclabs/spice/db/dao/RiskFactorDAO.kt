package com.medtroniclabs.spice.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medtroniclabs.spice.db.entity.RiskFactorEntity

@Dao
interface RiskFactorDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRiskFactor(riskFactorEntity: RiskFactorEntity)

    @Query("SELECT * FROM RiskFactorEntity")
    fun getAllRiskFactorEntity(): LiveData<List<RiskFactorEntity>>

    @Query("SELECT * FROM RiskFactorEntity")
    suspend fun getAllRiskFactorEntityList(): List<RiskFactorEntity>

    @Query("Delete from RiskFactorEntity")
    suspend fun deleteRiskFactor()
}
