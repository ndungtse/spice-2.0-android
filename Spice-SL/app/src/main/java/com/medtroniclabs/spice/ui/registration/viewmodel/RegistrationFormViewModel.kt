package com.medtroniclabs.spice.ui.registration.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.LocalSpinnerResponse
import com.medtroniclabs.spice.data.model.RegistrationResponse
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.model.FormResponse
import com.medtroniclabs.spice.mappingkey.Screening
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.registration.repo.RegistrationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistrationFormViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var registrationRepository: RegistrationRepository
) : ViewModel() {
    val registrationFormLayoutsLiveData = MutableLiveData<Resource<FormResponse>>()
    var registrationResponseLiveData = MutableLiveData<Resource<RegistrationResponse>>()
    var validatePatientResponseLiveDate =
        MutableLiveData<Resource<Pair<HashMap<String, Any>, List<FormLayout?>?>>>()

    val countrySpinnerLiveData = MutableLiveData<Resource<LocalSpinnerResponse>>()
    val districtSpinnerLiveData = MutableLiveData<Resource<LocalSpinnerResponse>>()
    val chiefdomSpinnerLiveData = MutableLiveData<Resource<LocalSpinnerResponse>>()
    val villageSpinnerLiveData = MutableLiveData<Resource<LocalSpinnerResponse>>()
    val programsSpinnerLiveData = MutableLiveData<Resource<LocalSpinnerResponse>>()

    fun getFormData(formType: String) {
        viewModelScope.launch(dispatcherIO) {
            registrationFormLayoutsLiveData.postLoading()
            registrationFormLayoutsLiveData.postValue(registrationRepository.getFormData(formType))
        }
    }

    fun loadDataCacheByType(type: String, tag: String, selectedParent: Long?) {
        viewModelScope.launch(dispatcherIO) {
            when (type) {
                DefinedParams.Country -> {
                    countrySpinnerLiveData.postLoading()
                    countrySpinnerLiveData.postValue(
                        registrationRepository.getCountries(tag)
                    )
                }

                DefinedParams.District -> {
                    selectedParent?.let {
                        districtSpinnerLiveData.postLoading()
                        districtSpinnerLiveData.postValue(
                            registrationRepository.getCounties(tag, selectedParent)
                        )
                    }
                }

                DefinedParams.Chiefdom -> {
                    selectedParent?.let {
                        chiefdomSpinnerLiveData.postLoading()
                        chiefdomSpinnerLiveData.postValue(
                            registrationRepository.getSubCounties(tag, it)
                        )
                    }
                }

                DefinedParams.Village -> {
                    villageSpinnerLiveData.postLoading()
                    villageSpinnerLiveData.postValue(
                        registrationRepository.getAllVillages(tag)
                    )
                }

                DefinedParams.Program -> {
                    programsSpinnerLiveData.postLoading()
                    programsSpinnerLiveData.postValue(
                        registrationRepository.getAllPrograms(tag)
                    )
                }
            }
        }
    }

    fun registerPatient(hashMap: HashMap<String, Any>, id: Long?, patientId: Long?) {
        hashMap.apply {
            id?.let { requestId ->
                put(DefinedParams.ID, requestId)
            }
            patientId?.let { requestPatientId ->
                put(DefinedParams.PATIENT_ID, requestPatientId)
            }
            put(DefinedParams.TenantId, SecuredPreference.getTenantId())
            put(DefinedParams.HealthFacilityId, SecuredPreference.getOrganizationId())
            put(DefinedParams.HealthFacilityFhirId, SecuredPreference.getOrganizationFhirId())
            put(DefinedParams.Provenance, ProvanceDto())
        }
        viewModelScope.launch(dispatcherIO) {
            registrationResponseLiveData.postLoading()
            registrationResponseLiveData.postValue(registrationRepository.registerPatient(hashMap))
        }
    }

    fun isPatientAlreadyRegistered(hashMap: HashMap<String, Any>, serverData: List<FormLayout?>?) {
        if (hashMap.contains(Screening.identityValue)) {
            hashMap[Screening.identityValue]?.let {
                val reqMap = HashMap<String, Any>()
                reqMap[Screening.identityValue] = it

                viewModelScope.launch(dispatcherIO) {
                    validatePatientResponseLiveDate.postLoading()
                    validatePatientResponseLiveDate.postValue(
                        registrationRepository.isPatientAlreadyRegistered(
                            reqMap, Pair(hashMap, serverData)
                        )
                    )
                }
            }
        }
    }
}