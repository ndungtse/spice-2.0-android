package com.medtroniclabs.spice.db.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.CountryModel
import com.medtroniclabs.spice.data.LoginResponse
import com.medtroniclabs.spice.data.OrganizationModel
import com.medtroniclabs.spice.data.TimeZoneModel
import com.medtroniclabs.spice.data.UserRole
import com.medtroniclabs.spice.db.SpiceDataBase
import com.medtroniclabs.spice.db.entity.HouseholdEntity
import com.medtroniclabs.spice.offlinesync.utils.OfflineSyncStatus
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.verify
import java.util.Date

@RunWith(AndroidJUnit4::class)
class HouseholdDAOTest {

    private lateinit var database: SpiceDataBase
    private lateinit var householdDAO: HouseholdDAO
    private lateinit var dummyHousehold: HouseholdEntity

    @Before
    fun setUp() {
        val dummyTimeZone = TimeZoneModel(
            id = 1, offset = "+05:00", description = "Central Standard Time"
        )
        val dummyCountry = CountryModel(
            id = 1,
            name = "United States",
            countryCode = "US",
            phoneNumberCode = "+1",
            unitMeasurement = "Imperial",
            tenantId = 123
        )
        val dummyOrganization = OrganizationModel(
            id = 1, name = "Organization A"
        )
        val dummyUserRole = UserRole(
            id = 1, name = "Admin", level = 1, authority = "ROLE_ADMIN"
        )
        val loginResponse = LoginResponse(
            username = "user",
            isActive = true,
            roles = listOf(dummyUserRole),
            id = 123,
            authorization = "Bearer token",
            deviceInfoId = null,
            countryCode = "US",
            country = dummyCountry,
            currentDate = Date().time,
            timezone = dummyTimeZone,
            tenantId = 456,
            cultureId = null,
            organizations = arrayListOf(dummyOrganization),
            isSuperUser = true,
            suiteAccess = listOf("suite1", "suite2"),
            client = "Android"
        )

        SecuredPreference.putUserDetails(loginResponse)
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), SpiceDataBase::class.java
        ).build()

        householdDAO = database.householdDAO()

        dummyHousehold = HouseholdEntity(
            id = 1,
            householdNo = 10L,
            name = "Test Household",
            villageId = 123L,
            landmark = "Nearby landmark",
            headPhoneNumber = "1234567890",
            noOfPeople = 5,
            isOwnedAnImprovedLatrine = true,
            isOwnedHandWashingFacilityWithSoap = false,
            isOwnedATreatedBedNet = true,
            bedNetCount = 2,
            latitude = 12.345,
            longitude = 67.890
        ).apply {
            fhirId = "1L"
            sync_status = OfflineSyncStatus.NotSynced
        }
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertHouseHold_verifyInserted_rowGreaterThen0() = runBlocking {
        val insertedRowId = householdDAO.insertHouseHold(dummyHousehold)
        assert(insertedRowId > 0)
    }

    @Test
    fun getLastHouseholdNo_returnsMaxHouseholdNoForVillage() = runBlocking {
        val villageId = 123L
        val expectedMaxHouseholdNo = 10L
        householdDAO.insertHouseHold(dummyHousehold)
        val maxHouseholdNo = householdDAO.getLastHouseholdNo(villageId)
        assert(maxHouseholdNo == expectedMaxHouseholdNo)
    }

    @Test
    fun getHouseHoldDetailsById_checkHouseHoldId_returnsTrue() {
        runBlocking {
            val houseHoldId = 1L
            householdDAO.insertHouseHold(dummyHousehold)
            val result = householdDAO.getHouseHoldDetailsById(houseHoldId)
            assert(dummyHousehold == result)
        }
    }
    @Test
    fun updateHeadCount_checkUpdateHeadCount_returnsTrue()= runBlocking {
        val householdId = 25L
        val newNoOfPeople = 5
        val syncStatus = "Synced"
        val updatedAt = 123456789L
       householdDAO.updateHeadCount(householdId, newNoOfPeople, syncStatus, updatedAt)
        val result = householdDAO.getHouseHoldDetailsById(householdId)
       Assert.assertNull(result)
    }
    @Test
    fun getHouseHoldDetailsById_checkHouseHoldId_returnsFalse() {
        runBlocking {
            val houseHoldId = 2L
            householdDAO.insertHouseHold(dummyHousehold)
            val result = householdDAO.getHouseHoldDetailsById(houseHoldId)
            assert(dummyHousehold != result)
        }
    }

    @Test
    fun getHouseholdIdByFhirId_fetchBasedFhirId_returnsTrue() = runBlocking {
        householdDAO.insertHouseHold(dummyHousehold)
        val result = dummyHousehold.fhirId?.let { householdDAO.getHouseholdIdByFhirId(it) }
        assert(result == 1L)
    }

    @Test
    fun getHouseholdIdByFhirId_fetchBasedFhirId_returnsFalse() = runBlocking {
        householdDAO.insertHouseHold(dummyHousehold)
        val result = dummyHousehold.fhirId?.let { householdDAO.getHouseholdIdByFhirId(it) }
        assert(result != 2L)
    }

    @Test
    fun getUnSyncedCount_checkUnSyncedStatusCount_returnsTrue() = runBlocking {
        val dummy2 = HouseholdEntity(
            id = 5,
            householdNo = 70L,
            name = "Test Household",
            villageId = 723L,
            landmark = "Nearby landmark",
            headPhoneNumber = "1234567890",
            noOfPeople = 5,
            isOwnedAnImprovedLatrine = true,
            isOwnedHandWashingFacilityWithSoap = false,
            isOwnedATreatedBedNet = true,
            bedNetCount = 2,
            latitude = 12.345,
            longitude = 67.890
        ).apply {
            fhirId = "7L"
            sync_status = OfflineSyncStatus.Success
        }
        householdDAO.insertHouseHold(dummyHousehold)
        householdDAO.insertHouseHold(dummy2)
        val result = householdDAO.getUnSyncedCount()

        // Two data inserted one is notSync and another is success so we fetch only one
        assert(result == 1)
    }


}
