package com.medtroniclabs.spice.db.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.medtroniclabs.spice.data.DiseaseCategoryItems
import com.medtroniclabs.spice.data.DiseaseConditionItems
import com.medtroniclabs.spice.db.SpiceDataBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class DiagnosisDAOTest {

    private lateinit var database: SpiceDataBase
    private lateinit var diagnosisDao: DiagnosisDAO

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            SpiceDataBase::class.java
        ).build()

        diagnosisDao = database.diagnosisDAO()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun saveDiagnosisList_checkValueInserted_returnTrue() = runBlocking {
        // Insert some dummy data into the table
        val dummyDiseaseCategoryItems = arrayListOf(
            DiseaseCategoryItems(
                id = 1,
                name = "Category 1",
                displayOrder = 1,
                diseaseCondition = arrayListOf(
                    DiseaseConditionItems(1, 1, "Condition 1", 1),
                    DiseaseConditionItems(2, 1, "Condition 2", 2)
                )
            ),
            DiseaseCategoryItems(
                id = 2,
                name = "Category 2",
                displayOrder = 2,
                diseaseCondition = arrayListOf(
                    DiseaseConditionItems(3, 2, "Condition 3", 1),
                    DiseaseConditionItems(4, 2, "Condition 4", 2)
                )
            )
        )

    }
}