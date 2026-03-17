package org.medtroniclabs.uhis.ui.mypatients.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.data.ReferPatientHealthFacilityItem
import org.medtroniclabs.uhis.data.ReferPatientNameNumber
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.ui.mypatients.repo.ReferPatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReferPatientViewModel @Inject constructor(
    private val repository: ReferPatientRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
) : ViewModel() {
    var referToSelectedId: String? = null
    var clinicalSelectedId: String? = null
    var enteredReferredReason: String? = null
    var patientId: String? = null
    var villageId: String? = null
    var houseHoldId: String? = null
    var memberId: String? = null
    val healthFacilityLiveData = MutableLiveData<Resource<List<ReferPatientHealthFacilityItem>>>()
    val nameNumberListLiveData = MutableLiveData<Resource<List<ReferPatientNameNumber>>>()
    val referPatientResultLiveData = MutableLiveData<Resource<HashMap<String, Any>>>()

    fun getHealthFacilityMetaData(districtId: String?) {
        districtId?.let {
            viewModelScope.launch(dispatcherIO) {
                healthFacilityLiveData.postLoading()
                healthFacilityLiveData.postValue(repository.getHealthFacilityMetaData(districtId))
            }
        }
    }

    fun getNameNumberFieldList(tenantId: String) {
        viewModelScope.launch(dispatcherIO) {
            healthFacilityLiveData.postLoading()
            nameNumberListLiveData.postValue(repository.getReferPatientMobileUserList(tenantId))
        }
    }

    fun createReferPatientResult(
        patientReference: String?,
        encounterId: String?,
        assessmentName: Pair<String?, String>,
        patientId: String?,
        houseHoldId: String?,
        villageId: String?,
        memberId: String?,
        tbIMRCompleted: Boolean? = null,
    ) {
        viewModelScope.launch(dispatcherIO) {
            referPatientResultLiveData.postLoading()
            referPatientResultLiveData.postValue(
                repository.createReferPatientResult(
                    patientReference,
                    encounterId,
                    Triple(referToSelectedId, clinicalSelectedId, enteredReferredReason),
                    assessmentName,
                    patientId,
                    houseHoldId,
                    villageId,
                    memberId,
                    tbIMRCompleted = tbIMRCompleted,
                ),
            )
        }
    }
}
