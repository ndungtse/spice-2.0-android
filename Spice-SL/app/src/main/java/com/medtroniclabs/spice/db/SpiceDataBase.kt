package com.medtroniclabs.spice.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.medtroniclabs.spice.db.dao.AssessmentDAO
import com.medtroniclabs.spice.db.dao.HouseholdDAO
import com.medtroniclabs.spice.db.dao.MemberDAO
import com.medtroniclabs.spice.db.dao.MetaDataDAO
import com.medtroniclabs.spice.db.entity.AssessmentEntity
import com.medtroniclabs.spice.db.entity.ClinicalWorkflowEntity
import com.medtroniclabs.spice.db.entity.FormEntity
import com.medtroniclabs.spice.db.entity.HealthFacilityEntity
import com.medtroniclabs.spice.db.entity.HouseholdEntity
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.db.entity.MenuEntity
import com.medtroniclabs.spice.db.entity.SignsAndSymptomsEntity
import com.medtroniclabs.spice.db.entity.SymptomEntity
import com.medtroniclabs.spice.db.entity.UserProfileEntity
import com.medtroniclabs.spice.db.entity.VillageEntity

@Database(
    entities = [HouseholdEntity::class, HouseholdMemberEntity::class, SignsAndSymptomsEntity::class, AssessmentEntity::class, MenuEntity::class, UserProfileEntity::class,
        VillageEntity::class, HealthFacilityEntity::class, ClinicalWorkflowEntity::class, FormEntity::class],
    version = 1
)
abstract class SpiceDataBase : RoomDatabase() {
    abstract fun householdDAO(): HouseholdDAO
    abstract fun memberDAO(): MemberDAO
    abstract fun assessmentDAO(): AssessmentDAO
    abstract fun metaDataDAO(): MetaDataDAO

    companion object {
        private const val DATABASE_NAME = "SpiceDataBase"

        @Volatile
        private var INSTANCE: SpiceDataBase? = null

        fun getInstance(context: Context): SpiceDataBase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }


        private fun buildDatabase(context: Context): SpiceDataBase {
            val db = Room.databaseBuilder(
                context.applicationContext,
                SpiceDataBase::class.java,
                DATABASE_NAME
            )
            return db.build()
        }
    }

}