package com.medtroniclabs.spice.ui.medicalreview.prescription

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.data.APIResponse
import com.medtroniclabs.spice.data.MedicationResponse
import com.medtroniclabs.spice.data.MedicationSearchRequest
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrescriptionViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val medicationRepository: MedicationRepository
) : ViewModel() {

    val medicationListLiveData = MutableLiveData<Resource<APIResponse<ArrayList<MedicationResponse>>>>()

    fun searchMedicationByName(name: String) {
        viewModelScope.launch(dispatcherIO) {
            try {
                medicationListLiveData.postLoading()
                val request = MedicationSearchRequest(name)
                val response  = medicationRepository.searchMedicationByName(request)
                medicationListLiveData.postSuccess(response.body())
            }catch (e:Exception){
                medicationListLiveData.postError()
            }
        }
    }
}