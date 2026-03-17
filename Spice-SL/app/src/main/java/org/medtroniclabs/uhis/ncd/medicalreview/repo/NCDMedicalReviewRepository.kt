package org.medtroniclabs.uhis.ncd.medicalreview.repo

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.medtroniclabs.uhis.common.SecuredPreference
import org.medtroniclabs.uhis.data.ErrorResponse
import org.medtroniclabs.uhis.data.history.HistoryEntity
import org.medtroniclabs.uhis.data.history.NCDMedicalReviewHistory
import org.medtroniclabs.uhis.db.entity.NCDMedicalReviewMetaEntity
import org.medtroniclabs.uhis.db.local.RoomHelper
import org.medtroniclabs.uhis.model.ReferralDetailRequest
import org.medtroniclabs.uhis.ncd.data.BadgeNotificationModel
import org.medtroniclabs.uhis.ncd.data.LifeStyleRequest
import org.medtroniclabs.uhis.ncd.data.LifeStyleResponse
import org.medtroniclabs.uhis.ncd.data.MRSummaryResponse
import org.medtroniclabs.uhis.ncd.data.MedicalReviewRequestResponse
import org.medtroniclabs.uhis.ncd.data.MedicalReviewResponse
import org.medtroniclabs.uhis.ncd.data.NCDDiagnosisGetRequest
import org.medtroniclabs.uhis.ncd.data.NCDDiagnosisGetResponse
import org.medtroniclabs.uhis.ncd.data.NCDDiagnosisRequestResponse
import org.medtroniclabs.uhis.ncd.data.NCDInstructionModel
import org.medtroniclabs.uhis.ncd.data.NCDMRSummaryRequestResponse
import org.medtroniclabs.uhis.ncd.data.NCDMedicalReviewUpdateModel
import org.medtroniclabs.uhis.ncd.data.NCDMentalHealthMedicalReviewDetails
import org.medtroniclabs.uhis.ncd.data.NCDMentalHealthStatusRequest
import org.medtroniclabs.uhis.ncd.data.NCDPatientStatusRequest
import org.medtroniclabs.uhis.ncd.data.NCDPregnancyRiskUpdate
import org.medtroniclabs.uhis.ncd.medicalreview.NCDMRUtil
import org.medtroniclabs.uhis.ncd.medicalreview.NCDMRUtil.Comorbidity
import org.medtroniclabs.uhis.ncd.medicalreview.NCDMRUtil.Complaints
import org.medtroniclabs.uhis.ncd.medicalreview.NCDMRUtil.Complications
import org.medtroniclabs.uhis.ncd.medicalreview.NCDMRUtil.CurrentMedication
import org.medtroniclabs.uhis.ncd.medicalreview.NCDMRUtil.FrequencyTypes
import org.medtroniclabs.uhis.ncd.medicalreview.NCDMRUtil.Lifestyle
import org.medtroniclabs.uhis.ncd.medicalreview.NCDMRUtil.PatientLifestyle
import org.medtroniclabs.uhis.ncd.medicalreview.NCDMRUtil.PhysicalExamination
import org.medtroniclabs.uhis.network.ApiHelper
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.network.resource.ResourceState
import okhttp3.ResponseBody
import javax.inject.Inject

class NCDMedicalReviewRepository @Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper,
) {
    fun getComorbiditiesBasedOnType(
        type: String? = null,
        category: String,
    ) = roomHelper.getComorbidities(type, category)

    fun getLifeStyle() = roomHelper.getLifeStyle()

    fun getNCDDiagnosisList(
        types: List<String>,
        gender: String,
        isPregnant: Boolean,
    ) = roomHelper.getNCDDiagnosisList(types, gender, isPregnant)

    suspend fun getNcdMedicalReviewStaticData(): Resource<Boolean> =
        try {
            val response = apiHelper.getNcdMRStaticData()
            if (response.isSuccessful) {
                response.body()?.entity?.let {
                    roomHelper.deleteNCDMedicalReviewMeta()
                    val allData = mutableListOf<NCDMedicalReviewMetaEntity>()
                    // Add all categories to the list
                    allData.addAll(
                        it.comorbidity.map { comorbidity ->
                            comorbidity.apply { category = Comorbidity }
                        },
                    )
                    allData.addAll(
                        it.complications.map { complications ->
                            complications.apply { category = Complications }
                        },
                    )
                    allData.addAll(
                        it.complaints.map { complaints ->
                            complaints.apply { category = Complaints }
                        },
                    )
                    allData.addAll(
                        it.physicalExamination.map { physicalExamination ->
                            physicalExamination.apply { category = PhysicalExamination }
                        },
                    )
                    allData.addAll(
                        it.currentMedication.map { currentMedication ->
                            currentMedication.apply { category = CurrentMedication }
                        },
                    )
                    allData.addAll(
                        it.frequencyTypes.map { frequencyTypes ->
                            frequencyTypes.apply { category = FrequencyTypes }
                        },
                    )
                    allData.addAll(
                        it.frequencyTypes.map { lifestyle ->
                            lifestyle.apply { category = Lifestyle }
                        },
                    )
                    allData.addAll(
                        it.nutritionLifestyles.map { frequencyTypes ->
                            frequencyTypes.apply { category = PatientLifestyle }
                        },
                    )

                    // Insert everything at once into the table
                    roomHelper.insertNCDMedicalReviewMeta(allData)

                    // Handle Treatment Plan Separately
                    roomHelper.deleteTreatmentPlan()
                    roomHelper.insertTreatmentPlan(
                        it.frequencies.filter { freq ->
                            freq.type.equals(
                                NCDMRUtil.DEFAULT,
                            )
                        },
                    )

                    // Handle lifestyle separately
                    roomHelper.deleteLifestyle()
                    roomHelper.insertLifestyle(it.lifestyle)

                    // Handle Dosage Duration separately
                    roomHelper.deleteDosageDurations()
                    it.dosageDuration?.let { dd ->
                        roomHelper.insertDosageDurations(dd)
                    }
                }
                SecuredPreference.putBoolean(
                    SecuredPreference.EnvironmentKey.IS_NCD_MEDICAL_REVIEW_LOADED.name,
                    true,
                )
                Resource(state = ResourceState.SUCCESS, true)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            SecuredPreference.putBoolean(
                SecuredPreference.EnvironmentKey.IS_NCD_MEDICAL_REVIEW_LOADED.name,
                false,
            )
            Resource(state = ResourceState.ERROR)
        }

    suspend fun createNCDMedicalReview(request: MedicalReviewRequestResponse): Resource<MedicalReviewResponse> =
        try {
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

    suspend fun fetchNCDMRSummary(request: MedicalReviewResponse): Resource<MRSummaryResponse> =
        try {
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

    suspend fun createConfirmDiagonsis(request: NCDDiagnosisRequestResponse): Resource<HashMap<String, Any>> =
        try {
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

    suspend fun getConfirmDiagonsis(request: NCDDiagnosisGetRequest): Resource<NCDDiagnosisGetResponse> =
        try {
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

    suspend fun createNCDPatientStatus(request: NCDPatientStatusRequest): Resource<HashMap<String, Any>> =
        try {
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

    suspend fun createNCDMRSummaryCreate(request: NCDMRSummaryRequestResponse): Resource<HashMap<String, Any>> =
        try {
            val response = apiHelper.createNCDMRSummaryCreate(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(state = ResourceState.ERROR)
        }

    suspend fun getNCDMedicalReviewHistory(request: ReferralDetailRequest): Resource<NCDMedicalReviewHistory> =
        try {
            val response = apiHelper.getNCDMedicalReviewHistory(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun getPrescription(request: ReferralDetailRequest): Resource<HistoryEntity> =
        try {
            val response = apiHelper.getPrescription(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun ncdGetInstructions(): Resource<NCDInstructionModel> =
        try {
            val response = apiHelper.ncdGetInstructions()
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(state = ResourceState.ERROR)
        }

    suspend fun ncdUpdatePregnancyRisk(request: NCDPregnancyRiskUpdate): Resource<Boolean> =
        try {
            val response = apiHelper.ncdUpdatePregnancyRisk(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(state = ResourceState.ERROR)
        }

    suspend fun getNCDInvestigation(request: ReferralDetailRequest): Resource<HistoryEntity> =
        try {
            val response = apiHelper.getInvestigation(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun getBadgeNotifications(request: BadgeNotificationModel): Resource<BadgeNotificationModel> =
        try {
            val response = apiHelper.getBadgeNotifications(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(state = ResourceState.ERROR)
        }

    suspend fun updateBadgeNotifications(request: BadgeNotificationModel): Resource<Boolean> =
        try {
            val response = apiHelper.updateBadgeNotifications(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(state = ResourceState.ERROR)
        }

    suspend fun getNcdLifeStyleDetails(request: LifeStyleRequest): Resource<ArrayList<LifeStyleResponse>> =
        try {
            val response = apiHelper.getNcdLifeStyleDetails(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, response.body()?.entity ?: ArrayList())
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(state = ResourceState.ERROR)
        }

    suspend fun getNCDShortageReason(type: String) = roomHelper.getNCDShortageReason(type)

    suspend fun ncdUpdateNextVisitDate(request: NCDMedicalReviewUpdateModel): Resource<HashMap<String, Any>> =
        try {
            val response = apiHelper.ncdUpdateNextVisitDate(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(state = ResourceState.ERROR)
        }

    suspend fun createMentalHealthStatus(request: NCDMentalHealthStatusRequest): Resource<HashMap<String, Any>>? =
        try {
            val response = apiHelper.createMentalHealthStatus(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(state = ResourceState.ERROR)
        }

    suspend fun ncdMentalHealthMedicalReviewCreate(
        request: JsonObject,
        isAssessment: Boolean,
    ): Resource<String> =
        try {
            val response =
                if (isAssessment) {
                    apiHelper.ncdMentalHealthMedicalReviewCreateA(request)
                } else {
                    apiHelper.ncdMentalHealthMedicalReviewCreateS(request)
                }
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, response.body()?.message)
            } else {
                Resource(
                    state = ResourceState.ERROR,
                    message = getErrorMessage(response.errorBody()),
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(state = ResourceState.ERROR)
        }

    suspend fun ncdMentalHealthMedicalReviewDetails(
        request: NCDMentalHealthMedicalReviewDetails,
        isAssessment: Boolean,
    ): Resource<HashMap<String, Any>>? =
        try {
            val response =
                if (isAssessment) {
                    apiHelper.ncdMentalHealthMedicalReviewDetailsA(request)
                } else {
                    apiHelper.ncdMentalHealthMedicalReviewDetailsS(request)
                }
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(state = ResourceState.ERROR)
        }

    private fun getErrorMessage(errorBody: ResponseBody?): String? {
        if (errorBody == null) {
            return null
        }
        return try {
            val errorResponse = Gson().fromJson(errorBody.string(), ErrorResponse::class.java)
            return errorResponse.message
        } catch (e: Exception) {
            null
        }
    }

    suspend fun ncdPatientDiagnosisStatus(request: HashMap<String, Any>): Resource<HashMap<String, Any>>? =
        try {
            val response = apiHelper.ncdPatientDiagnosisStatus(request)
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
