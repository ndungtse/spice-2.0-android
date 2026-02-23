package com.medtroniclabs.spice.ui.medicalreview.motherneonate.labourdelivery.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.model.CreateLabourDeliveryRequest
import com.medtroniclabs.spice.data.model.LabourDeliverySummaryDetails
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.labourdelivery.repo.LabourDeliveryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LabourDeliverySummaryViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var repository: LabourDeliveryRepository,
) : ViewModel() {
    val summaryDetailsLiveData = MutableLiveData<Resource<CreateLabourDeliveryRequest>>()
    val summaryCreateResponse = MutableLiveData<Resource<HashMap<String, Any>>>()
    var nextFollowupDate: String? = null
    var neonatePatientStatus: String? = null

    fun getLabourDeliverySummaryDetails(
        motherId: String?,
        patientReference: String?,
        childPatientReference: String?,
        neonateId: String?,
    ) {
        val request = LabourDeliverySummaryDetails(
            motherId = motherId,
            patientReference = patientReference,
            childPatientReference = childPatientReference,
            neonateId = neonateId,
        )
        viewModelScope.launch(dispatcherIO) {
            summaryDetailsLiveData.postLoading()
            summaryDetailsLiveData.postValue(repository.getLabourDeliverySummaryDetails(request))
        }
    }
}
