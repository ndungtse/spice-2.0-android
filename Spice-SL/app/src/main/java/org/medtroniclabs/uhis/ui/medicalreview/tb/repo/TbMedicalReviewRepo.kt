package org.medtroniclabs.uhis.ui.medicalreview.tb.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.medtroniclabs.uhis.appextensions.postError
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.appextensions.postSuccess
import org.medtroniclabs.uhis.common.SecuredPreference
import org.medtroniclabs.uhis.data.MedicalReviewMetaItems
import org.medtroniclabs.uhis.data.model.BpAndWeightRequestModel
import org.medtroniclabs.uhis.data.model.BpAndWeightResponse
import org.medtroniclabs.uhis.data.model.MotherNeonateAncRequest
import org.medtroniclabs.uhis.data.model.PatientEncounterResponse
import org.medtroniclabs.uhis.data.model.PatientTypeCreateRequest
import org.medtroniclabs.uhis.data.model.TbHistory
import org.medtroniclabs.uhis.data.model.TbMedicalReviewCreateRequest
import org.medtroniclabs.uhis.db.local.RoomHelper
import org.medtroniclabs.uhis.network.ApiHelper
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.network.resource.ResourceState
import org.medtroniclabs.uhis.ui.medicalreview.motherneonate.anc.MotherNeonateUtil.TB_ORGAN_AFFECTED
import org.medtroniclabs.uhis.ui.medicalreview.motherneonate.anc.MotherNeonateUtil.TB_SITE_OF_DISEASE
import org.medtroniclabs.uhis.ui.medicalreview.utils.MedicalReviewTypeEnums
import javax.inject.Inject

class TbMedicalReviewRepo @Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper,
) {
    suspend fun getTbStaticData(tbMetaResponse: MutableLiveData<Resource<Boolean>>) {
        try {
            tbMetaResponse.postLoading()
            val response = apiHelper.getTbStaticData()
            if (response.isSuccessful) {
                response.body()?.entity?.let { data ->
                    roomHelper.apply {
                        deleteExaminationsComplaintsForAnc(MedicalReviewTypeEnums.TB.name)
                        insertExaminationsComplaint(
                            generateChipItemByType(
                                data.presentingComplaints,
                                data.systemicExaminations,
                                data.comorbidities,
                                data.patientStatus,
                                data.patientType,
                                data.treatmentOutcome,
                            ),
                        )
                        roomHelper.deleteDiagnosisList(MedicalReviewTypeEnums.TB.name)
                        roomHelper.deleteDiagnosisList(TB_SITE_OF_DISEASE)
                        roomHelper.deleteDiagnosisList(TB_ORGAN_AFFECTED)
                        roomHelper.saveDiagnosisList(data.diseaseCategories)
                        SecuredPreference.putBoolean(SecuredPreference.EnvironmentKey.IS_TB_LOADED.name, true)
                    }
                    tbMetaResponse.postSuccess()
                } ?: run {
                    tbMetaResponse.postError()
                }
            } else {
                tbMetaResponse.postError()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            tbMetaResponse.postError()
        }
    }

    private fun generateChipItemByType(
        presentingComplaints: List<MedicalReviewMetaItems>,
        systemicExaminations: List<MedicalReviewMetaItems>,
        comorbidities: List<MedicalReviewMetaItems>,
        patientStatus: List<MedicalReviewMetaItems>,
        patientType: List<MedicalReviewMetaItems>,
        treatmentOutcome: List<MedicalReviewMetaItems>,
    ): List<MedicalReviewMetaItems> {
        val chipItemList = mutableListOf<MedicalReviewMetaItems>()
        chipItemList.addAll(
            presentingComplaints.map {
                it.apply {
                    category = MedicalReviewTypeEnums.PresentingComplaints.name
                }
            },
        )
        chipItemList.addAll(
            systemicExaminations.map {
                it.apply {
                    category = MedicalReviewTypeEnums.SystemicExaminations.name
                }
            },
        )
        chipItemList.addAll(
            comorbidities.map {
                it.apply {
                    type = MedicalReviewTypeEnums.TB.name
                    category = MedicalReviewTypeEnums.comorbidities.name
                }
            },
        )
        chipItemList.addAll(
            patientStatus.map {
                it.apply {
                    type = MedicalReviewTypeEnums.TB.name
                    category = MedicalReviewTypeEnums.patient_status.name
                }
            },
        )
        chipItemList.addAll(
            patientType.map {
                it.apply {
                    category = MedicalReviewTypeEnums.patient_type.name
                }
            },
        )
        chipItemList.addAll(
            treatmentOutcome.map {
                it.apply {
                    category = MedicalReviewTypeEnums.treatment_outcome.name
                }
            },
        )
        return chipItemList
    }

    suspend fun fetchHeight(motherNeonateAncRequest: MotherNeonateAncRequest): Resource<BpAndWeightResponse> =
        try {
            val response = apiHelper.fetchHeight(motherNeonateAncRequest)
            if (response.isSuccessful) {
                val res = response.body()
                if (res?.status == true) {
                    Resource(state = ResourceState.SUCCESS, data = res.entity)
                } else {
                    Resource(state = ResourceState.ERROR)
                }
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun createHeight(bpAndWeightRequestModel: BpAndWeightRequestModel): Resource<HashMap<String, Any>> =
        try {
            val response = apiHelper.createHeight(bpAndWeightRequestModel)
            if (response.isSuccessful) {
                val res = response.body()
                if (res?.status == true) {
                    Resource(state = ResourceState.SUCCESS, data = res.entity)
                } else {
                    Resource(state = ResourceState.ERROR)
                }
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun fetchBmi(motherNeonateAncRequest: MotherNeonateAncRequest): Resource<BpAndWeightResponse> =
        try {
            val response = apiHelper.fetchBmi(motherNeonateAncRequest)
            if (response.isSuccessful) {
                val res = response.body()
                if (res?.status == true) {
                    Resource(state = ResourceState.SUCCESS, data = res.entity)
                } else {
                    Resource(state = ResourceState.ERROR)
                }
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun fetchBmiList(motherNeonateAncRequest: MotherNeonateAncRequest): Resource<List<BpAndWeightResponse>> =
        try {
            apiHelper.fetchList(motherNeonateAncRequest).let { response ->
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        if (body.status) {
                            Resource(ResourceState.SUCCESS, body.entity ?: emptyList())
                        } else {
                            Resource(ResourceState.ERROR)
                        }
                    } ?: Resource(ResourceState.ERROR)
                } else {
                    Resource(ResourceState.ERROR)
                }
            }
        } catch (e: Exception) {
            Resource(ResourceState.ERROR)
        }

    suspend fun fetchTbAssessmentDetails(motherNeonateAncRequest: MotherNeonateAncRequest): Resource<TbHistory> =
        try {
            apiHelper.fetchTbAssessmentDetails(motherNeonateAncRequest).let { response ->
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        if (body.status) {
                            Resource(ResourceState.SUCCESS, body.entity)
                        } else {
                            Resource(ResourceState.ERROR)
                        }
                    } ?: Resource(ResourceState.ERROR)
                } else {
                    Resource(ResourceState.ERROR)
                }
            }
        } catch (e: Exception) {
            Resource(ResourceState.ERROR)
        }

    suspend fun saveTbMedicalReview(request: TbMedicalReviewCreateRequest): Resource<PatientEncounterResponse> =
        try {
            val response = apiHelper.saveTbMedicalReview(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(state = ResourceState.ERROR)
        }

    suspend fun createPatientType(request: PatientTypeCreateRequest): Resource<HashMap<String, Any>> =
        try {
            val response = apiHelper.createPatientType(request)
            if (response.isSuccessful) {
                val res = response.body()
                if (res?.status == true) {
                    Resource(state = ResourceState.SUCCESS, data = res.entity)
                } else {
                    Resource(state = ResourceState.ERROR)
                }
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun getPatientType(request: MotherNeonateAncRequest): Resource<HashMap<String, Any>> =
        try {
            val response = apiHelper.getPatientType(request)
            if (response.isSuccessful) {
                val res = response.body()
                if (res?.status == true) {
                    Resource(state = ResourceState.SUCCESS, data = res.entity)
                } else {
                    Resource(state = ResourceState.ERROR)
                }
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    fun getExaminationsComplaints(
        category: String,
        type: String,
    ): LiveData<List<MedicalReviewMetaItems>> = roomHelper.getExaminationsComplaintsForAnc(category, type)

    suspend fun createBMI(bpAndWeightRequestModel: BpAndWeightRequestModel): Resource<HashMap<String, Any>> =
        try {
            val response = apiHelper.createBMI(bpAndWeightRequestModel)
            if (response.isSuccessful) {
                val res = response.body()
                if (res?.status == true) {
                    Resource(state = ResourceState.SUCCESS, data = res.entity)
                } else {
                    Resource(state = ResourceState.ERROR)
                }
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
}
