package com.medtroniclabs.spice.ncd.medicalreview.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.APIResponse
import com.medtroniclabs.spice.data.PregnancyDetailsModel
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.ncd.assessment.repo.NCDPregnancyRepo
import com.medtroniclabs.spice.network.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NCDPregnancyViewModel @Inject constructor(
    private val ncdPregnancyRepo: NCDPregnancyRepo,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {
    var isPregnancyAncEnabledSite: Boolean = false

    val resultDiabetesHashMap = HashMap<String, Any>()
    val resultHypertensionHashMap = HashMap<String, Any>()
    val resultPregnantHashMap = HashMap<String, Any>()

    val ncdPregnancyCreateResponse = MutableLiveData<Resource<APIResponse<HashMap<String, Any>>>>()
    val ncdPregnancyDetailsResponse = MutableLiveData<Resource<PregnancyDetailsModel>>()

    var relatedPersonFhirId: String? = null

    var ncdPregnancyCreateModel: PregnancyDetailsModel = PregnancyDetailsModel()

    init {
        isPregnancyAncEnabledSite =
            !SecuredPreference.getBoolean(SecuredPreference.EnvironmentKey.PREGNANCY_ANC_ENABLED_SITE.name)
    }

    fun ncdPregnancyCreate(requestModel: PregnancyDetailsModel) {
        viewModelScope.launch(dispatcherIO) {
            ncdPregnancyCreateResponse.postLoading()
            ncdPregnancyCreateResponse.postValue(ncdPregnancyRepo.ncdPregnancyCreate(requestModel))
        }
    }

    fun ncdPregnancyDetails(id: String) {
        viewModelScope.launch(dispatcherIO) {
            val request = HashMap<String, Any>().apply {
                put(DefinedParams.id, id)
            }
            ncdPregnancyDetailsResponse.postLoading()
            ncdPregnancyDetailsResponse.postValue(
                ncdPregnancyRepo.ncdPregnancyDetails(request)
            )
        }
    }
}