package com.medtroniclabs.spice.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medtroniclabs.spice.db.entity.MemberClinicalEntity

interface MemberClinicalDAO {


   /* @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClinicalInfos(list: List<MemberClinicalEntity>) // Initial download

    @Query("DELETE from MemberClinical")
    suspend fun deleteAllMemberClinical()*/

}