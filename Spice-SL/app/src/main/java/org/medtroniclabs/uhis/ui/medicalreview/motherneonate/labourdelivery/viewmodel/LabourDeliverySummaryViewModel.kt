package org.medtroniclabs.uhis.ui.medicalreview.motherneonate.labourdelivery.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.data.model.CreateLabourDeliveryRequest
import org.medtroniclabs.uhis.data.model.LabourDeliverySummaryDetails
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.ui.medicalreview.motherneonate.labourdelivery.repo.LabourDeliveryRepository
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
