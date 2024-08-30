package com.medtroniclabs.spice.db.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.medtroniclabs.spice.db.SpiceDataBase
import com.medtroniclabs.spice.db.entity.AssessmentEntity
import com.medtroniclabs.spice.db.entity.SignsAndSymptomsEntity
import com.medtroniclabs.spice.ui.assessment.referrallogic.utils.ReferralStatus
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AssessmentDAOTest {
    private lateinit var database: SpiceDataBase
    private lateinit var assessmentDAO: AssessmentDAO
    private lateinit var dummyAssessmentData: AssessmentEntity
    private lateinit var dummySymptomsList: List<SignsAndSymptomsEntity>

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), SpiceDataBase::class.java
        ).build()

        assessmentDAO = database.assessmentDAO()
        dummyAssessmentData = AssessmentEntity(
            memberId = 1,
            householdId = 1,
            patientId = "123456",
            assessmentType = "Type 1",
            assessmentDetails = "Details for assessment 1",
            otherDetails = "Other details for assessment 1",
            createdAt = System.currentTimeMillis(),
            userId = 1,
            isReferred = false,
            referralStatus = ReferralStatus.Referred,
            referredReason = arrayListOf("Reason 1", "Reason 2")
        )
        dummySymptomsList = listOf(
            SignsAndSymptomsEntity(
                _id = 1,
                symptom = "Fever",
                type = "General",
                cultureValue = "High",
                displayOrder = 1
            ), SignsAndSymptomsEntity(
                _id = 2,
                symptom = "Cough",
                type = "Respiratory",
                cultureValue = "Mild",
                displayOrder = 2
            ), SignsAndSymptomsEntity(
                _id = 3,
                symptom = "Headache",
                type = "General",
                cultureValue = null,
                displayOrder = 3
            )
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAssessment_checkValueInserted_assertNotNull() = runBlocking {
        withContext(Dispatchers.IO) {
            assessmentDAO.insertAssessment(dummyAssessmentData)
        }
        val insertedAssessment = withContext(Dispatchers.IO) {
            assessmentDAO.getLatestAssessmentForMember(dummyAssessmentData.memberId)
        }
        assertNotNull(insertedAssessment)
    }


    @Test
    fun insertAssessment_checkValueInserted_assertCompare() = runBlocking {
        withContext(Dispatchers.IO) {
            assessmentDAO.insertAssessment(dummyAssessmentData)
        }
        val insertedAssessment = withContext(Dispatchers.IO) {
            assessmentDAO.getLatestAssessmentForMember(dummyAssessmentData.memberId)
        }
        if (insertedAssessment != null) {
            assert(dummyAssessmentData.patientId == insertedAssessment.patientId)
        }
    }


    @Test
    fun updateOtherAssessmentDetails_checkValueUpdated_assertCompare() = runBlocking {
        withContext(Dispatchers.IO) {
            assessmentDAO.insertAssessment(dummyAssessmentData)
        }
        val getMemberById = withContext(Dispatchers.IO) {
            assessmentDAO.getLatestAssessmentForMember(dummyAssessmentData.memberId)
        }
        val updatedOtherDetails = "Updated other details"
        val updatedAssessmentData = getMemberById!!.copy(otherDetails = updatedOtherDetails)
        val updatedAssessment = withContext(Dispatchers.IO) {
            assessmentDAO.updateOtherAssessmentDetails(updatedAssessmentData)
        }
        val getMemberByIdAfterUpdate = withContext(Dispatchers.IO) {
            assessmentDAO.getLatestAssessmentForMember(dummyAssessmentData.memberId)
        }
        if (getMemberByIdAfterUpdate != null) {
            assert(updatedOtherDetails == getMemberByIdAfterUpdate.otherDetails)
        }
    }

    @Test
    fun getSymptomListByType_verifyReturnedSymptoms_assertCompare() = runBlocking {
        assessmentDAO.insertSymptoms(dummySymptomsList)
        val symptomsByType = assessmentDAO.getSymptomListByType("General")
        val expectedSymptoms =
            dummySymptomsList.filter { it.type.equals("General", ignoreCase = true) }
        assert(symptomsByType.containsAll(expectedSymptoms))
    }
}