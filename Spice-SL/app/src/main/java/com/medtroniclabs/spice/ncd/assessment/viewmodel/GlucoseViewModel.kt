package com.medtroniclabs.spice.ncd.assessment.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.APIResponse
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.mappingkey.Screening
import com.medtroniclabs.spice.ncd.data.BPBGListModel
import com.medtroniclabs.spice.ncd.assessment.repo.GlucoseRepo
import com.medtroniclabs.spice.network.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GlucoseViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val glucoseRepo: GlucoseRepo
) : ViewModel() {
    var glucoseLogCreateResponseLiveData = MutableLiveData<Resource<APIResponse<HashMap<String, Any>>>>()
    var glucoseLogListResponseLiveData = MutableLiveData<Resource<BPBGListModel>>()

    fun glucoseLogCreate(
        hashMap: HashMap<String, Any>,
        relatedPersonFhirId: String?,
        identityValue: String?,
        patientId: String?
    ) {
        hashMap.apply {
            patientId?.let { requestPatientId ->
                put(DefinedParams.PATIENT_ID, requestPatientId)
            }
            relatedPersonFhirId?.let { requestRelatedPersonFhirId ->
                put(DefinedParams.RelatedPersonFhirId, requestRelatedPersonFhirId)
            }
            identityValue?.let { idValue ->
                put(Screening.identityValue, idValue)
            }
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