package com.medtroniclabs.spice.db.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.medtroniclabs.spice.db.SpiceDataBase
import com.medtroniclabs.spice.db.entity.ClinicalWorkflowConditionEntity
import com.medtroniclabs.spice.db.entity.ClinicalWorkflowEntity
import com.medtroniclabs.spice.db.entity.FormEntity
import com.medtroniclabs.spice.db.entity.HealthFacilityEntity
import com.medtroniclabs.spice.db.entity.MenuEntity
import com.medtroniclabs.spice.db.entity.SignsAndSymptomsEntity
import com.medtroniclabs.spice.db.entity.UserProfileEntity
import com.medtroniclabs.spice.db.entity.VillageEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verify
@RunWith(AndroidJUnit4::class)
class MetaDataDAOTest {
    private lateinit var database: SpiceDataBase
    lateinit var metaDataDAO: MetaDataDAO

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), SpiceDataBase::class.java
        ).build()

        metaDataDAO = database.metaDataDAO()
    }

    @Test
    fun insertHealthFacility_checkInserted_returnsTrue() = runBlocking {
        val healthFacilityEntity = HealthFacilityEntity(
            id = 1,
            name = "Health Facility Name",
            tenantId = 1,
            districtId = 1,
            chiefdomId = 1,
            isDefault = false
        )
        metaDataDAO.insertHealthFacility(healthFacilityEntity)

        val result = metaDataDAO.getDefaultHealthFacility()
        if (result != null) {
            assert(result.name == "Health Facility Name")
        }
    }

    @Test
    fun deleteAllHealthFacility_checkDeleted_returnsTrue() = runBlocking {
        val healthFacilityEntity = HealthFacilityEntity(
            id = 1,
            name = "Health Facility Name",
            tenantId = 1,
            districtId = 1,
            chiefdomId = 1,
            isDefault = false
        )
        metaDataDAO.insertHealthFacility(healthFacilityEntity)

        metaDataDAO.deleteAllHealthFacility()

        val result = metaDataDAO.getDefaultHealthFacility()
        Assert.assertNull(result)
    }

    @Test
    fun insertVillages_checkInserted_returnsTrue() = runBlocking {
        val villageEntities = listOf(
            VillageEntity(1, "Village 1", "CODE1", 1, 1, 1),
            VillageEntity(2, "Village 2", "CODE2", 2, 2, 2),
            VillageEntity(3, "Village 3", "CODE3", 3, 3, 3)
        )
        metaDataDAO.insertVillages(villageEntities)
        var result = metaDataDAO.getVillageByID(2)

        assert(result.name == "Village 2")
    }

    @Test
    fun deleteVillages_checkDeleted_returnsTrue() = runBlocking {
        val villageEntities = listOf(
            VillageEntity(1, "Village 1", "CODE1", 1, 1, 1),
            VillageEntity(2, "Village 2", "CODE2", 2, 2, 2),
            VillageEntity(3, "Village 3", "CODE3", 3, 3, 3)
        )
        metaDataDAO.insertVillages(villageEntities)
        metaDataDAO.deleteAllVillages()
        val result = metaDataDAO.getAllVillageName()
        assert(result.isEmpty())
    }

    @Test
    fun insertMenus_checkInsertedMenu_returnsTrue() = runBlocking {

        val menuEntity = MenuEntity(1, "menuId1", "Role1", "Menu 1", 1)

        metaDataDAO.insertMenus(menuEntity)

        val insertedMenu = metaDataDAO.getMenus()
        assert(menuEntity == insertedMenu[0])
    }

    @Test
    fun deleteAllMenus_checkDeletedMenu_returnsTrue() = runBlocking {

        val menuEntity = MenuEntity(1, "menuId1", "Role1", "Menu 1", 1)

        metaDataDAO.insertMenus(menuEntity)

        metaDataDAO.deleteAllMenus()
        val insertedMenu = metaDataDAO.getMenus()
        assert(insertedMenu.isEmpty())
    }

    @Test
    fun insertUserProfileDetails_checkInsertedUserProfile_returnsTrue() = runBlocking {
        val userProfileEntity = UserProfileEntity(
            id = 1,
            profileData = "User profile data"
        )
        metaDataDAO.insertUserProfileDetails(userProfileEntity)
        val insertedUserProfile = metaDataDAO.getUserProfile()
        assertEquals(userProfileEntity, insertedUserProfile)
    }

    @Test
    fun deleteAllUserProfileDetails_checkDeletedUserProfile_returnsTrue() = runBlocking {
        val userProfileEntity = UserProfileEntity(
            id = 1,
            profileData = "User profile data"
        )
        metaDataDAO.insertUserProfileDetails(userProfileEntity)

        metaDataDAO.deleteAllUserProfileDetails()
        val insertedUserProfile = metaDataDAO.getUserProfile()
        Assert.assertNull(insertedUserProfile)
    }

    @Test
    fun saveClinicalWorkflows_checkSavedWorkflows_returnsTrue() = runBlocking {
        // Given
        val clinicalWorkflowList = listOf(
            ClinicalWorkflowEntity(
                id = 1,
                name = "Workflow 1",
                moduleType = "Type A",
                workflowName = "Workflow A",
                countryId = 1,
                displayOrder = 1
            ),
            ClinicalWorkflowEntity(
                id = 2,
                name = "Workflow 2",
                moduleType = "Type B",
                workflowName = "Workflow B",
                countryId = 2,
                displayOrder = 2
            ),
            ClinicalWorkflowEntity(
                id = 3,
                name = "Workflow 3",
                moduleType = "Type C",
                workflowName = "Workflow C",
                countryId = 3,
                displayOrder = 3
            )
        )
        metaDataDAO.saveClinicalWorkflows(clinicalWorkflowList)
        val savedWorkflows = metaDataDAO.getAllClinicalWorkflowIds()
        assert(clinicalWorkflowList.size == savedWorkflows.size)
    }

    @Test
    fun deleteAllClinicalWorkflow_checkSavedWorkflows_returnsTrue() = runBlocking {
        // Given
        val clinicalWorkflowList = listOf(
            ClinicalWorkflowEntity(
                id = 1,
                name = "Workflow 1",
                moduleType = "Type A",
                workflowName = "Workflow A",
                countryId = 1,
                displayOrder = 1
            ),
            ClinicalWorkflowEntity(
                id = 2,
                name = "Workflow 2",
                moduleType = "Type B",
                workflowName = "Workflow B",
                countryId = 2,
                displayOrder = 2
            ),
            ClinicalWorkflowEntity(
                id = 3,
                name = "Workflow 3",
                moduleType = "Type C",
                workflowName = "Workflow C",
                countryId = 3,
                displayOrder = 3
            )
        )
        metaDataDAO.saveClinicalWorkflows(clinicalWorkflowList)
        metaDataDAO.deleteAllClinicalWorkflow()
        val savedWorkflows = metaDataDAO.getAllClinicalWorkflowIds()
        assert(savedWorkflows.isEmpty())
    }

    @Test
    fun saveForms_checkSavedForms_returnsTrue() = runBlocking {
        val formsList = listOf(
            FormEntity(1, "Input1", "Type1"),
            FormEntity(2, "Input2", "Type2"),
            FormEntity(3, "Input3", "Type3")
        )
        metaDataDAO.saveForms(formsList)
        val savedForms = metaDataDAO.getFormData("Type1")
        assert(formsList[0].formInput == savedForms)
    }

    @Test
    fun deleteAllForms_checkNoFormsLeft() = runBlocking {
        // Given
        val formsList = listOf(
            FormEntity(1, "Input1", "Type1"),
            FormEntity(2, "Input2", "Type2"),
            FormEntity(3, "Input3", "Type3")
        )
        metaDataDAO.saveForms(formsList)

        // When
        metaDataDAO.deleteAllForms()

        // Then
        val formsCount = metaDataDAO.getFormData("Type1")
        Assert.assertNull(formsCount)
    }

    @Test
    fun getFormData_checkReturnedData() = runBlocking {
        // Given
        val formType = "Type1"
        val expectedFormData = "Input1"
        val formsList = listOf(
            FormEntity(1, "Input1", "Type1"),
            FormEntity(2, "Input2", "Type2"),
            FormEntity(3, "Input3", "Type3")
        )
        metaDataDAO.saveForms(formsList)

        // When
        val actualFormData = metaDataDAO.getFormData(formType)

        // Then
        assertEquals(expectedFormData, actualFormData)
    }

    @Test
    fun getChiefDomAndVillageCodeByVillageId_checkReturnedInfo() = runBlocking {
        // Given: Insert village entity into the database
        val villageEntities = listOf(
            VillageEntity(1, "Village 1", "CODE1", 1, 1, 1),
            VillageEntity(2, "Village 2", "CODE2", 2, 2, 2),
            VillageEntity(3, "Village 3", "CODE3", 3, 3, 3)
        )
        metaDataDAO.insertVillages(villageEntities)
        val returnedChiefdomId = metaDataDAO.getChiefDomAndVillageCodeByVillageId(1)
        assert(1.toLong() == returnedChiefdomId.chiefdomId)
    }

    @Test
    fun getMenuForClinicalWorkflows_checkReturnedMenu() = runBlocking {
        val clinicalWorkflowList = listOf(
            ClinicalWorkflowEntity(
                id = 1,
                name = "Workflow 1",
                moduleType = "Type A",
                workflowName = "Workflow A",
                countryId = 1,
                displayOrder = 1
            ),
            ClinicalWorkflowEntity(
                id = 2,
                name = "Workflow 2",
                moduleType = "Type B",
                workflowName = "Workflow B",
                countryId = 2,
                displayOrder = 2
            ),
            ClinicalWorkflowEntity(
                id = 3,
                name = "Workflow 3",
                moduleType = "Type C",
                workflowName = "Workflow C",
                countryId = 3,
                displayOrder = 3
            )
        )
        metaDataDAO.saveClinicalWorkflows(clinicalWorkflowList)
        val retrievedMenu = metaDataDAO.getMenuForClinicalWorkflows()
        assertEquals(clinicalWorkflowList, retrievedMenu)
    }
    @Test
    fun deleteClinicalWorkflowConditions_checkDeleted() = runBlocking {
        val clinicalWorkflowList = listOf(
            ClinicalWorkflowEntity(
                id = 1,
                name = "Workflow 1",
                moduleType = "Type A",
                workflowName = "Workflow A",
                countryId = 1,
                displayOrder = 1
            ),
            ClinicalWorkflowEntity(
                id = 2,
                name = "Workflow 2",
                moduleType = "Type B",
                workflowName = "Workflow B",
                countryId = 2,
                displayOrder = 2
            ),
            ClinicalWorkflowEntity(
                id = 3,
                name = "Workflow 3",
                moduleType = "Type C",
                workflowName = "Workflow C",
                countryId = 3,
                displayOrder = 3
            )
        )
        metaDataDAO.saveClinicalWorkflows(clinicalWorkflowList)
        metaDataDAO.deleteAllClinicalWorkflow()
        val retrievedMenu = metaDataDAO.getMenuForClinicalWorkflows()
        assert(retrievedMenu.isEmpty())
    }


}