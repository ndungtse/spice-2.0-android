package com.medtroniclabs.spice.ui.mypatients.repo

import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.db.entity.NCDMedicalReviewMetaEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.ncd.data.PatientVisitRequest
import com.medtroniclabs.spice.ncd.data.PatientVisitResponse
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import javax.inject.Inject

class NCDMedicalReviewRepo @Inject constructor(
    private val apiHelper: ApiHelper,
    private val roomHelper: RoomHelper
) {
    suspend fun getNcdMedicalReviewStaticData(): Resource<Boolean> {
        return try {
            val response = apiHelper.getNcdMRStaticData()
            if (response.isSuccessful) {
                response.body()?.entity?.let {
                    roomHelper.deleteNCDMedicalReviewMeta()
                    val allData = mutableListOf<NCDMedicalReviewMetaEntity>()
                    // Add all categories to the list
                    allData.addAll(it.comorbidity.map { comorbidity ->
                        comorbidity.apply { category = "Comorbidity" }
                    })
                    allData.addAll(it.complications.map { complications ->
                        complications.apply { category = "Complications" }
                    })
                    allData.addAll(it.complaints.map { complaints ->
                        complaints.apply { category = "Complaints" }
                    })
                    allData.addAll(it.physicalExamination.map { physicalExamination ->
                        physicalExamination.apply { category = "PhysicalExamination" }
                    })
                    allData.addAll(it.currentMedication.map { currentMedication ->
                        currentMedication.apply { category = "CurrentMedication" }
                    })
                    allData.addAll(it.frequencyTypes.map { frequencyTypes ->
                        frequencyTypes.apply { category = "FrequencyTypes" }
                    })
                    allData.addAll(it.frequencies.map { frequencies ->
                        frequencies.apply { category = "Frequencies" }
                    })

                    // Insert everything at once into the table
                    roomHelper.insertNCDMedicalReviewMeta(allData.map { item ->
                        item.apply {
                            id = 0
                        }
                    })

                    // Handle lifestyle separately
                    roomHelper.deleteLifestyle()
                    roomHelper.insertLifestyle(it.lifestyle)
                }
                SecuredPreference.putBoolean(
                    SecuredPreference.EnvironmentKey.IS_NCD_MEDICAL_REVIEW_LOADED.name,
                    true
                )
                Resource(state = ResourceState.SUCCESS, true)
            } else {
                Resource(state = ResourceState.ERROR)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            SecuredPreference.putBoolean(
                SecuredPreference.EnvironmentKey.IS_NCD_MEDICAL_REVIEW_LOADED.name,
                false
            )
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun createPatientVisit(request: PatientVisitRequest): Resource<PatientVisitResponse> {
        return try {
            val response = apiHelper.createPatientVisit(request)
            if (response.isSuccessful) {
                response.body()?.entity?.let {
                    Resource(state = ResourceState.SUCCESS, it)
                } ?: Resource(state = ResourceState.ERROR)

            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(state = ResourceState.ERROR)
        }
    }
}