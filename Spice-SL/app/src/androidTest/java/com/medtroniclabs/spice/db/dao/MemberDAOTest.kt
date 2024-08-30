package com.medtroniclabs.spice.db.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.CountryModel
import com.medtroniclabs.spice.data.LoginResponse
import com.medtroniclabs.spice.data.OrganizationModel
import com.medtroniclabs.spice.data.TimeZoneModel
import com.medtroniclabs.spice.data.UserRole
import com.medtroniclabs.spice.db.SpiceDataBase
import com.medtroniclabs.spice.db.entity.HouseholdEntity
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.offlinesync.utils.OfflineSyncStatus
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date
import kotlin.math.sign

@RunWith(AndroidJUnit4::class)
class MemberDAOTest {
    private lateinit var database: SpiceDataBase
    lateinit var memberDAO: MemberDAO
    private lateinit var dummyHousehold: HouseholdMemberEntity

    @Before
    fun setUp() {
        // Setting user id
        // Generate dummy data for TimeZoneModel
        val dummyTimeZone = TimeZoneModel(
            id = 1,
            offset = "+05:00",
            description = "Central Standard Time"
        )

// Generate dummy data for CountryModel
        val dummyCountry = CountryModel(
            id = 1,
            name = "United States",
            countryCode = "US",
            phoneNumberCode = "+1",
            unitMeasurement = "Imperial",
            tenantId = 123
        )

// Generate dummy data for OrganizationModel
        val dummyOrganization = OrganizationModel(
            id = 1,
            name = "Organization A"
        )

// Generate dummy data for UserRole
        val dummyUserRole = UserRole(
            id = 1,
            name = "Admin",
            level = 1,
            authority = "ROLE_ADMIN"
        )

// Generate dummy data for LoginResponse
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

        memberDAO = database.memberDAO()

        dummyHousehold = HouseholdMemberEntity(
            name = "John Doe",
            phoneNumber = "1234567890",
            phoneNumberCategory = "Mobile",
            dateOfBirth = "1990-01-01",
            gender = "Male",
            householdHeadRelationship = "Son",
            householdId = 1,
            patientId = "5",
            isPregnant = false
        )
            .apply {
                fhirId = "1L"
                sync_status = OfflineSyncStatus.NotSynced
            }

// Set values for BaseEntity properties


    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertMember_checkInsertedId() = runBlocking {
        val insertedId = memberDAO.insertMember(dummyHousehold)
        assert(insertedId > 0)
    }
    @Test
    fun getAllHouseHoldMemberList_checkReturnedHouseholdMember() = runBlocking {
        memberDAO.insertMember(dummyHousehold)
        val retrievedHouseholdMember = memberDAO.getAllHouseHoldMemberList(1)
        assert(dummyHousehold.householdId== retrievedHouseholdMember[0].householdId)
    }
    @Test
    fun getMemberDetailsById_checkReturnedHouseholdMember() = runBlocking {
        memberDAO.insertMember(dummyHousehold)
        val retrievedHouseholdMember = memberDAO.getMemberDetailsById(1)
        assert(dummyHousehold.householdId== retrievedHouseholdMember.householdId)
    }
    @Test
    fun getMemberCountPerHouseHold_checkReturnedHouseholdMember() = runBlocking {
        memberDAO.insertMember(dummyHousehold)
        val retrievedHouseholdMemberCount = memberDAO.getMemberCountPerHouseHold(1)
        assert(1== retrievedHouseholdMemberCount)
    }
    @Test
    fun getLastPatientId_checkReturnedPatientId() = runBlocking {
        memberDAO.insertMember(dummyHousehold)
        val retrievedLastPatientId = memberDAO.getLastPatientId()
        if (retrievedLastPatientId!= null) {
            assert("5"== retrievedLastPatientId.lastPatientId)
        }
    }
    @Test
    fun getDobAndGenderById_checkReturnedDobAndGender() = runBlocking {
        memberDAO.insertMember(dummyHousehold)
        val retrievedDobAndGenderById = memberDAO.getDobAndGenderById(1)
            assert("Male"== retrievedDobAndGenderById.gender)
    }
    @Test
    fun getAllUnSyncedHouseHoldMembers_checkReturnedUnSyncedHouseHoldMembers() = runBlocking {
        memberDAO.insertMember(dummyHousehold)
        val retrievedUnSyncedHouseHoldMembers = memberDAO.getAllUnSyncedHouseHoldMembers(1,OfflineSyncStatus.NotSynced.name)
        assert(retrievedUnSyncedHouseHoldMembers.isEmpty())
    }
    @Test
    fun getUnSyncedCount_checkReturnedUnSyncedCount() = runBlocking {
        memberDAO.insertMember(dummyHousehold)
        val retrievedUnSyncedHouseHoldMembers = memberDAO.getUnSyncedCount()
        assert(1==retrievedUnSyncedHouseHoldMembers)
    }
    @Test
    fun getHouseholdMemberIdByFhirId_checkReturnedHouseholdMemberIdByFhirId() = runBlocking {
        memberDAO.insertMember(dummyHousehold)
        val retrievedMemberIdByFhirId = memberDAO.getHouseholdMemberIdByFhirId("1L")
        assert(1L==retrievedMemberIdByFhirId)
    }
}