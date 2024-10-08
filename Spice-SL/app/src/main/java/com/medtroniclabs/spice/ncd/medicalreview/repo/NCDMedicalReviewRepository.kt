package com.medtroniclabs.spice.ncd.medicalreview.repo

import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.db.entity.NCDMedicalReviewMetaEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.ncd.data.MRSummaryResponse
import com.medtroniclabs.spice.ncd.data.MedicalReviewRequestResponse
import com.medtroniclabs.spice.ncd.data.MedicalReviewResponse
import com.medtroniclabs.spice.ncd.data.NCDDiagnosisGetRequest
import com.medtroniclabs.spice.ncd.data.NCDDiagnosisGetResponse
import com.medtroniclabs.spice.ncd.data.NCDDiagnosisRequestResponse
import com.medtroniclabs.spice.ncd.data.NCDPatientStatusRequest
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.Comorbidity
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.Complaints
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.Complications
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.CurrentMedication
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.FrequencyTypes
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.PhysicalExamination
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import javax.inject.Inject

class NCDMedicalReviewRepository @Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper
) {
    fun getComorbiditiesBasedOnType(type: String? = null, category: String) =
        roomHelper.getComorbidities(type, category)

    fun getLifeStyle() = roomHelper.getLifeStyle()
    fun getNCDDiagnosisList(types: List<String>,gender: String) = roomHelper.getNCDDiagnosisList(types,gender)

    suspend fun getNcdMedicalReviewStaticData(): Resource<Boolean> {
        return try {
            val response = apiHelper.getNcdMRStaticData()
            if (response.isSuccessful) {
                response.body()?.entity?.let {
                    roomHelper.deleteNCDMedicalReviewMeta()
                    val allData = mutableListOf<NCDMedicalReviewMetaEntity>()
                    // Add all categories to the list
                    allData.addAll(it.comorbidity.map { comorbidity ->
                        comorbidity.apply { category = Comorbidity }
                    })
                    allData.addAll(it.complications.map { complications ->
                        complications.apply { category = Complications }
                    })
                    allData.addAll(it.complaints.map { complaints ->
                        complaints.apply { category = Complaints }
                    })
                    allData.addAll(it.physicalExamination.map { physicalExamination ->
                        physicalExamination.apply { category = PhysicalExamination }
                    })
                    allData.addAll(it.currentMedication.map { currentMedication ->
                        currentMedication.apply { category = CurrentMedication }
                    })
                    allData.addAll(it.frequencyTypes.map { frequencyTypes ->
                        frequencyTypes.apply { category = FrequencyTypes }
                    })

                    // Insert everything at once into the table
                    roomHelper.insertNCDMedicalReviewMeta(allData.map { item ->
                        item.apply {
                            id = 0
                        }
                    })

                    //Handle Treatment Plan Separately
                    roomHelper.deleteTreatmentPlan()
                    roomHelper.insertTreatmentPlan(it.frequencies.filter { freq ->
                        freq.type.equals(
                            NCDMRUtil.DEFAULT
                        )
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

    suspend fun createNCDMedicalReview(request: MedicalReviewRequestResponse): Resource<MedicalReviewResponse> {
        return try {
            val response = apiHelper.createNCDMedicalReview(request)
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

    suspend fun fetchNCDMRSummary(request: MedicalReviewResponse): Resource<MRSummaryResponse> {
        return try {
            val response = apiHelper.fetchNCDMRSummary(request)
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

    suspend fun createConfirmDiagonsis(request: NCDDiagnosisRequestResponse): Resource<HashMap<String, Any>> {
        return try {
            val response = apiHelper.createConfirmDiagonsis(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun getConfirmDiagonsis(request: NCDDiagnosisGetRequest): Resource<NCDDiagnosisGetResponse> {
        return try {
            val response = apiHelper.getConfirmDiagonsis(request)
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

    suspend fun createNCDPatientStatus(request: NCDPatientStatusRequest): Resource<HashMap<String, Any>> {
        return try {
            val response = apiHelper.createNCDPatientStatus(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(state = ResourceState.ERROR)
        }
    }
}