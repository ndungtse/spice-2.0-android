package com.medtroniclabs.spice.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.medtroniclabs.spice.data.CulturesEntity
import com.medtroniclabs.spice.BuildConfig
import com.medtroniclabs.spice.data.DiseaseCategoryItems
import com.medtroniclabs.spice.data.ExaminationListItems
import com.medtroniclabs.spice.data.LabourDeliveryMetaEntity
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.data.ProgramEntity
import com.medtroniclabs.spice.db.converters.OfflineStatusTypeConverter
import com.medtroniclabs.spice.db.dao.AboveFiveYearsDAO
import com.medtroniclabs.spice.db.dao.AssessmentDAO
import com.medtroniclabs.spice.db.dao.CallHistoryDao
import com.medtroniclabs.spice.db.dao.ConsentFormDao
import com.medtroniclabs.spice.db.dao.DiagnosisDAO
import com.medtroniclabs.spice.db.dao.ExaminationsComplaintsDAO
import com.medtroniclabs.spice.db.dao.ExaminationsDAO
import com.medtroniclabs.spice.db.dao.FollowUpCallsDao
import com.medtroniclabs.spice.db.dao.FollowUpDao
import com.medtroniclabs.spice.db.dao.FrequencyDAO
import com.medtroniclabs.spice.db.dao.HouseholdDAO
import com.medtroniclabs.spice.db.dao.LabourDeliveryDAO
import com.medtroniclabs.spice.db.dao.LinkHouseholdMemberDao
import com.medtroniclabs.spice.db.dao.MemberDAO
import com.medtroniclabs.spice.db.dao.MetaDataDAO
import com.medtroniclabs.spice.db.dao.PregnancyDetailDao
import com.medtroniclabs.spice.db.entity.AssessmentEntity
import com.medtroniclabs.spice.db.entity.CallHistory
import com.medtroniclabs.spice.db.entity.ClinicalWorkflowConditionEntity
import com.medtroniclabs.spice.db.entity.ClinicalWorkflowEntity
import com.medtroniclabs.spice.db.entity.ConsentForm
import com.medtroniclabs.spice.db.entity.ConsentEntity
import com.medtroniclabs.spice.db.entity.FollowUp
import com.medtroniclabs.spice.db.entity.FollowUpCall
import com.medtroniclabs.spice.db.entity.FormEntity
import com.medtroniclabs.spice.db.entity.FrequencyEntity
import com.medtroniclabs.spice.db.entity.HealthFacilityEntity
import com.medtroniclabs.spice.db.entity.HouseholdEntity
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.db.entity.LinkHouseholdMember
import com.medtroniclabs.spice.db.entity.MentalHealthEntity
import com.medtroniclabs.spice.db.entity.MenuEntity
import com.medtroniclabs.spice.db.entity.PregnancyDetail
import com.medtroniclabs.spice.db.entity.SignsAndSymptomsEntity
import com.medtroniclabs.spice.db.entity.UserProfileEntity
import com.medtroniclabs.spice.db.entity.VillageEntity
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(
    entities = [HouseholdEntity::class, HouseholdMemberEntity::class, SignsAndSymptomsEntity::class, AssessmentEntity::class, MenuEntity::class, UserProfileEntity::class,
        VillageEntity::class, HealthFacilityEntity::class, ClinicalWorkflowEntity::class, FormEntity::class, ClinicalWorkflowConditionEntity::class,
        MedicalReviewMetaItems::class, DiseaseCategoryItems::class, ExaminationListItems::class, LabourDeliveryMetaEntity::class, FollowUp::class, FollowUpCall::class,
        PregnancyDetail::class, FrequencyEntity::class, ConsentForm::class, LinkHouseholdMember::class, CallHistory::class,
        MedicalReviewMetaItems::class, DiseaseCategoryItems::class, ExaminationListItems::class, LabourDeliveryMetaEntity::class, FollowUp::class, FollowUpCall::class, PregnancyDetail::class, FrequencyEntity::class, ConsentForm::class,
        ProgramEntity::class, CulturesEntity::class, ConsentForm::class,
        MedicalReviewMetaItems::class, DiseaseCategoryItems::class, ExaminationListItems::class, LabourDeliveryMetaEntity::class, FollowUp::class, FollowUpCall::class, PregnancyDetail::class, FrequencyEntity::class,
        ProgramEntity::class, CulturesEntity::class, ConsentForm::class, ConsentEntity::class, MentalHealthEntity::class],
    version = 1
)
@TypeConverters(OfflineStatusTypeConverter::class)
abstract class SpiceDataBase : RoomDatabase() {
    abstract fun householdDAO(): HouseholdDAO
    abstract fun memberDAO(): MemberDAO
    abstract fun assessmentDAO(): AssessmentDAO
    abstract fun metaDataDAO(): MetaDataDAO
    abstract fun examinationsComplaintsDAO(): ExaminationsComplaintsDAO
    abstract fun diagnosisDAO(): DiagnosisDAO
    abstract fun aboveFiveYearsDAO(): AboveFiveYearsDAO
    abstract fun examinationsDAO(): ExaminationsDAO
    abstract fun labourDeliveryDAO(): LabourDeliveryDAO
    abstract fun followUpDao(): FollowUpDao

    abstract fun followUpCallsDao(): FollowUpCallsDao

    abstract fun pregnancyDetailDao(): PregnancyDetailDao

    abstract fun frequencyDao(): FrequencyDAO

    abstract fun consentFormDao(): ConsentFormDao

    abstract fun linkHouseholdMemberDao(): LinkHouseholdMemberDao

    abstract fun callHistoryDao(): CallHistoryDao

    companion object {
        private const val DATABASE_NAME = "SpiceDataBase"

        @Volatile
        private var INSTANCE: SpiceDataBase? = null

        fun getInstance(context: Context): SpiceDataBase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }


        private fun buildDatabase(context: Context): SpiceDataBase {
            System.loadLibrary("sqlcipher")
            val factory = SupportOpenHelperFactory(BuildConfig.DB_PASSWORD.toByteArray(Charsets.UTF_8))
            val db = Room.databaseBuilder(
                context.applicationContext,
                SpiceDataBase::class.java,
                DATABASE_NAME
            )
            if (!BuildConfig.DEBUG)
                db.openHelperFactory(factory)

            /*Migration Scripts*/
            db.addMigrations(SpiceSLMigration.MIGRATION_1_2)

            return db.build()
        }
    }
}