package com.medtroniclabs.spice.db.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.CountryModel
import com.medtroniclabs.spice.data.LoginResponse
import com.medtroniclabs.spice.data.OrganizationModel
import com.medtroniclabs.spice.data.TimeZoneModel
import com.medtroniclabs.spice.data.UserRole
import com.medtroniclabs.spice.db.SpiceDataBase
import com.medtroniclabs.spice.db.entity.HouseholdEntity
import com.medtroniclabs.spice.db.entity.MemberClinicalEntity
import com.medtroniclabs.spice.offlinesync.utils.OfflineSyncStatus
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.Date

class MemberClinicalDAOTest {
    private lateinit var database: SpiceDataBase
    private lateinit var memberClinicalDAO: MemberClinicalDAO
    private lateinit var memberClinicalEntity: MemberClinicalEntity

    @Before
    fun setUp() {


        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), SpiceDataBase::class.java
        ).build()

        memberClinicalDAO = database.memberClinicalDAO()
        memberClinicalEntity = MemberClinicalEntity(
            patientId = "123",
            type = "Checkup",
            visitCount = 1,
            clinicalDate = "2024-04-05"
        )
    }




    @After
    fun tearDown() {
        database.close()
    }
    @Test
    fun savePatientVisitCountByType_checkInserted() = runBlocking {
        val insertedId = memberClinicalDAO.savePatientVisitCountByType(memberClinicalEntity)
        Assert.assertNotNull(insertedId)
    }
    @Test
    fun getPatientVisitCountByType_PatientVisitCountByType() = runBlocking {
        val insertedId = memberClinicalDAO.getPatientVisitCountByType("Checkup","123")
        if (insertedId != null) {
            assert(1.toLong() ==insertedId.visitCount)
        }
    }



}