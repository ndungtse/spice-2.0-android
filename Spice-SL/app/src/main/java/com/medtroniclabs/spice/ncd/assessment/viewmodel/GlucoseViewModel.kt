package com.medtroniclabs.spice.ncd.assessment.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.APIResponse
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.mappingkey.Screening
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.ncd.data.BPBGListModel
import com.medtroniclabs.spice.ncd.assessment.repo.GlucoseRepo
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil
import com.medtroniclabs.spice.network.SingleLiveEvent
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GlucoseViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val glucoseRepo: GlucoseRepo
) : ViewModel() {
    var glucoseLogCreateResponseLiveData = SingleLiveEvent<Resource<APIResponse<HashMap<String, Any>>>>()
    var glucoseLogListResponseLiveData = SingleLiveEvent<Resource<BPBGListModel>>()

    fun glucoseLogCreate(hashMap: HashMap<String, Any>, patientDetails: PatientListRespModel) {
        hashMap.apply {
            with(patientDetails) {
                NCDMRUtil.getBioDataBioMetrics(hashMap, this, isGlucose = true)
                id?.let { requestRelatedPersonFhirId ->
                    put(DefinedParams.RelatedPersonFhirId, requestRelatedPersonFhirId)
                }
                patientId?.let { requestPatientId ->
                    put(DefinedParams.PATIENT_ID, requestPatientId)
                }
            }
            put(AssessmentDefinedParams.assessmentProcessType, CommonUtils.requestFrom())
            put(DefinedParams.AssessmentOrganizationId, SecuredPreference.getOrganizationFhirId())
            put(DefinedParams.Provenance, ProvanceDto())
        }
        viewModelScope.launch(dispatcherIO) {
            glucoseLogCreateResponseLiveData.postLoading()
            glucoseLogCreateResponseLiveData.postValue(glucoseRepo.glucoseLogCreate(hashMap))
        }
    }

    fun glucoseLogList(patientId: String) {
        val request = BPBGListModel().apply {
            limit = 10
            skip = 0
            memberId = patientId
            latestRequired = true
        }
        viewModelScope.launch(dispatcherIO) {
            glucoseLogListResponseLiveData.postLoading()
            glucoseLogListResponseLiveData.postValue(glucoseRepo.glucoseLogList(request))
        }
    }
}