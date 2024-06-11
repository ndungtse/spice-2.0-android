package com.medtroniclabs.spice.ui.mypatients.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryDetails
import com.medtroniclabs.spice.data.ReferPatientHealthFacilityItem
import com.medtroniclabs.spice.data.ReferPatientNameNumber
import com.medtroniclabs.spice.db.entity.HealthFacilityEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.mypatients.repo.ReferPatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReferPatientViewModel @Inject constructor(
    private val repository: ReferPatientRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
    ): ViewModel() {

    var referToSelectedId: String? = null
    var clinicalSelectedId: String? = null
    var enteredReferredReason: String? =null
    var patientId: String? = null
    var villageId: String? = null
    var houseHoldId: String? = null
    var memberId: String? = null
    val healthFacilityLiveData = MutableLiveData<Resource<List<ReferPatientHealthFacilityItem>>>()
    val nameNumberListLiveData = MutableLiveData<Resource<List<ReferPatientNameNumber>>>()
    val referPatientResultLiveData = MutableLiveData<Resource<HashMap<String,Any>>>()
    val defaultHealthFacilityLiveData = MutableLiveData<Resource<HealthFacilityEntity?>>()

    fun getHealthFacilityMetaData(districtId: String?) {
        districtId?.let {
            viewModelScope.launch(dispatcherIO) {
                healthFacilityLiveData.postLoading()
                healthFacilityLiveData.postValue(repository.getHealthFacilityMetaData(districtId))
            }
        }
    }
    fun getNameNumberFieldList(tenantId: String) {
        viewModelScope.launch(dispatcherIO){
            healthFacilityLiveData.postLoading()
            nameNumberListLiveData.postValue(repository.getReferPatientMobileUserList(tenantId))
        }
    }
    fun createReferPatientResult(
        details: AboveFiveYearsSummaryDetails,
        assessmentName: Pair<String?, String>,
        patientId: String?,
        houseHoldId: Long?,
        villageId: String?,
        memberId: String?
    ) {
        viewModelScope.launch(dispatcherIO) {
            referPatientResultLiveData.postLoading()
            referPatientResultLiveData.postValue(
                repository.createReferPatientResult(
                    details,
                    Triple(referToSelectedId, clinicalSelectedId, enteredReferredReason),
                    assessmentName,
                    patientId,
                    houseHoldId,
                    villageId,
                    memberId
                )
            )
        }
    }

    fun getDefaultHealthFacilityDistrictId() {
        viewModelScope.launch(dispatcherIO){
            defaultHealthFacilityLiveData.postLoading()
            defaultHealthFacilityLiveData.postValue(repository.getDefaultHealthFacilityDistrictId())
        }
    }
}