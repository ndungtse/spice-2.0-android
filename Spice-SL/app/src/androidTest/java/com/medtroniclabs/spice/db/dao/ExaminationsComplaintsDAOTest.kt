package com.medtroniclabs.spice.db.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.medtroniclabs.spice.data.ExaminationsComplaintItems
import com.medtroniclabs.spice.db.SpiceDataBase
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ExaminationsComplaintsDAOTest {
    private lateinit var database: SpiceDataBase
    lateinit var examinationsComplaintsDAO: ExaminationsComplaintsDAO
    lateinit var dummyComplaintItemsList: List<ExaminationsComplaintItems>

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), SpiceDataBase::class.java
        ).build()

        examinationsComplaintsDAO = database.examinationsComplaintsDAO()
        dummyComplaintItemsList = listOf(
            ExaminationsComplaintItems(
                itemId = 1,
                id = 1001,
                name = "Headache",
                ageCondition = "Above 12 years",
                type = "General",
                displayOrder = 1
            ),
            ExaminationsComplaintItems(
                itemId = 2,
                id = 1002,
                name = "Fever",
                ageCondition = "Above 6 months",
                type = "General",
                displayOrder = 2
            )
        )
    }

    @Test
    fun insertExaminationsComplaints_checkInserted() = runBlocking {
        var result = examinationsComplaintsDAO.insertExaminationsComplaints(dummyComplaintItemsList)
        Assert.assertNotNull(result)
    }
}