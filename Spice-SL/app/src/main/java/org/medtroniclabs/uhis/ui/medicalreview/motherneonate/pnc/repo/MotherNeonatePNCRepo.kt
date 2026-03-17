package org.medtroniclabs.uhis.ui.medicalreview.motherneonate.pnc.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.medtroniclabs.uhis.appextensions.postError
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.appextensions.postSuccess
import org.medtroniclabs.uhis.common.SecuredPreference
import org.medtroniclabs.uhis.data.MedicalReviewMetaItems
import org.medtroniclabs.uhis.data.MedicalReviewSummarySubmitRequest
import org.medtroniclabs.uhis.data.MotherNeonatePncSummaryRequest
import org.medtroniclabs.uhis.data.MotherNeonatePncSummaryResponse
import org.medtroniclabs.uhis.data.model.MotherNeonatePncRequest
import org.medtroniclabs.uhis.data.model.PncSubmitResponse
import org.medtroniclabs.uhis.db.local.RoomHelper
import org.medtroniclabs.uhis.network.ApiHelper
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.network.resource.ResourceState
import org.medtroniclabs.uhis.ui.medicalreview.utils.MedicalReviewTypeEnums
import javax.inject.Inject

class MotherNeonatePNCRepo @Inject constructor(
    private val apiHelper: ApiHelper,
    private val roomHelper: RoomHelper,
) {
    suspend fun getMotherPncStaticData(motherNeonateMetaResponse: MutableLiveData<Resource<Boolean>>) {
        try {
            motherNeonateMetaResponse.postLoading()
            val response = apiHelper.getMotherPncStaticData()
            if (response.isSuccessful) {
                response.body()?.entity?.let { data ->
                    roomHelper.apply {
                        roomHelper.deleteDiagnosisList(
                            MedicalReviewTypeEnums.PNC_MOTHER_REVIEW.name,
                        )
                        roomHelper.saveDiagnosisList(data.diseaseCategories)
                        deleteExaminationsComplaintsForAnc(MedicalReviewTypeEnums.PNC_MOTHER_REVIEW.name)
                        insertExaminationsComplaint(
                            generateChipItemByType(
                                data.presentingComplaints,
                                data.systemicExaminations,
                                data.patientStatus,
                            ),
                        )
                        SecuredPreference.putBoolean(
                            SecuredPreference.EnvironmentKey.IS_MOTHER_LOADED_PNC.name,
                            true,
                        )
                    }
                    motherNeonateMetaResponse.postSuccess()
                } ?: run {
                    motherNeonateMetaResponse.postError()
                }
            } else {
                motherNeonateMetaResponse.postError()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            motherNeonateMetaResponse.postError()
        }
    }

    private fun generateChipItemByType(
        presentingComplaints: List<MedicalReviewMetaItems>,
        obstetricExaminations: List<MedicalReviewMetaItems>,
        patientStatus: List<MedicalReviewMetaItems>,
    ): List<MedicalReviewMetaItems> {
        val chipItemList = mutableListOf<MedicalReviewMetaItems>()
        chipItemList.addAll(
            presentingComplaints.map {
                it.apply {
                    type = MedicalReviewTypeEnums.PNC_MOTHER_REVIEW.name
                    category = MedicalReviewTypeEnums.PresentingComplaints.name
                }
            },
        )
        chipItemList.addAll(
            obstetricExaminations.map {
                it.apply {
                    type = MedicalReviewTypeEnums.PNC_MOTHER_REVIEW.name
                    category = MedicalReviewTypeEnums.SystemicExaminations.name
                }
            },
        )
        chipItemList.addAll(
            patientStatus.map {
                it.apply {
                    type = MedicalReviewTypeEnums.PNC_MOTHER_REVIEW.name
                    category = MedicalReviewTypeEnums.patient_status.name
                }
            },
        )
        return chipItemList
    }

    suspend fun getNeonatePncStaticData(motherNeonateMetaResponse: MutableLiveData<Resource<Boolean>>) {
        try {
            motherNeonateMetaResponse.postLoading()
            val response = apiHelper.getNeonatePncStaticData()
            if (response.isSuccessful) {
                response.body()?.entity?.let { data ->
                    roomHelper.apply {
                        deleteExaminationsComplaintsForAnc(MedicalReviewTypeEnums.PNC_CHILD_REVIEW.name)
                        insertExaminationsComplaint(
                            generateNeonateChipItemByType(
                                data.presentingComplaints,
                                data.obstetricExaminations,
                                data.patientStatus,
                            ),
                        )
                        SecuredPreference.putBoolean(
                            SecuredPreference.EnvironmentKey.IS_NEONATE_LOADED_PNC.name,
                            true,
                        )
                    }
                    motherNeonateMetaResponse.postSuccess()
                } ?: run {
                    motherNeonateMetaResponse.postError()
                }
            } else {
                motherNeonateMetaResponse.postError()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            motherNeonateMetaResponse.postError()
        }
    }

    private fun generateNeonateChipItemByType(
        presentingComplaints: List<MedicalReviewMetaItems>,
        obstetricExaminations: List<MedicalReviewMetaItems>,
        patientStatus: List<MedicalReviewMetaItems>,
    ): List<MedicalReviewMetaItems> {
        val chipItemList = mutableListOf<MedicalReviewMetaItems>()
        chipItemList.addAll(
            presentingComplaints.map {
                it.apply {
                    type = MedicalReviewTypeEnums.PNC_CHILD_REVIEW.name
                    category = MedicalReviewTypeEnums.PresentingComplaints.name
                }
            },
        )
        chipItemList.addAll(
            obstetricExaminations.map {
                it.apply {
                    type = MedicalReviewTypeEnums.PNC_CHILD_REVIEW.name
                    category = MedicalReviewTypeEnums.ObstetricExaminations.name
                }
            },
        )
        chipItemList.addAll(
            patientStatus.map {
                it.apply {
                    type = MedicalReviewTypeEnums.PNC_CHILD_REVIEW.name
                    category = MedicalReviewTypeEnums.patient_status.name
                }
            },
        )
        return chipItemList
    }

    fun getExaminationsComplaintsForPnc(
        category: String,
        type: String,
    ): LiveData<List<MedicalReviewMetaItems>> = roomHelper.getExaminationsComplaintsForPnc(category, type)

    suspend fun getPncSummaryDetails(motherNeonatePncSummaryRequest: MotherNeonatePncSummaryRequest): Resource<MotherNeonatePncSummaryResponse> =
        try {
            val response = apiHelper.getPncSummaryDetails(motherNeonatePncSummaryRequest)
            if (response.isSuccessful) {
                val res = response.body()
                if (res?.status == true) {
                    Resource(state = ResourceState.SUCCESS, response.body()?.entity)
                } else {
                    Resource(state = ResourceState.ERROR)
                }
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun saveMotherNeonatePncData(motherNeonatePncRequest: MotherNeonatePncRequest): Resource<PncSubmitResponse> =
        try {
            val response = apiHelper.saveMotherNeonatePnc(motherNeonatePncRequest)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(state = ResourceState.ERROR)
        }

    suspend fun summaryCreatePncData(summaryCreateRequest: MedicalReviewSummarySubmitRequest): Resource<HashMap<String, Any>> =
        try {
            val response = apiHelper.createSummarySubmit(summaryCreateRequest)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(state = ResourceState.ERROR)
        }
}
