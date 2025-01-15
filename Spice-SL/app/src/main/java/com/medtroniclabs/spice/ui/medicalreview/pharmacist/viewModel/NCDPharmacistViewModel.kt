package com.medtroniclabs.spice.ui.medicalreview.pharmacist.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.app.analytics.model.UserDetail
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.DefinedParams.TYPE_REFILL
import com.medtroniclabs.spice.data.DispensePrescriptionRequest
import com.medtroniclabs.spice.data.DispensePrescriptionResponse
import com.medtroniclabs.spice.data.DispenseUpdatePrescriptionRequest
import com.medtroniclabs.spice.data.EncounterDetails
import com.medtroniclabs.spice.data.DispenseUpdateRequest
import com.medtroniclabs.spice.data.DispenseUpdateResponse
import com.medtroniclabs.spice.data.ShortageReasonEntity
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.BaseViewModel
import com.medtroniclabs.spice.ui.medicalreview.pharmacist.repo.NCDPharmacistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NCDPharmacistViewModel @Inject constructor(
    private var nCDPharmacistRepository: NCDPharmacistRepository,
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher
) : BaseViewModel(dispatcherIO) {

    var patientVisitId: String? = null
    var memberId: String? = null
    var lastRefillVisitId: String? = null
    var patientReference: String? = null


    val prescriptionDispenseLiveData =
        MutableLiveData<Resource<ArrayList<DispensePrescriptionResponse>>>()
    val prescriptionDispenseHistoryLiveData =
        MutableLiveData<Resource<ArrayList<DispensePrescriptionResponse>>>()
    val updatePrescriptionLiveData = MutableLiveData<Resource<DispenseUpdateResponse>>()
    val shortageReasonList = MutableLiveData<Resource<List<ShortageReasonEntity>>>()

    fun getPrescriptionDispenseList(request: DispenseUpdateRequest) {
        viewModelScope.launch(dispatcherIO) {
            prescriptionDispenseLiveData.postLoading()
            prescriptionDispenseLiveData.postValue(nCDPharmacistRepository.getPrescriptionDispenseList(request))
        }
    }

    fun getDispensePrescriptionHistory(request: DispenseUpdateRequest) {
        viewModelScope.launch(dispatcherIO) {
            prescriptionDispenseHistoryLiveData.postLoading()
            prescriptionDispenseHistoryLiveData.postValue(
                nCDPharmacistRepository.getDispensePrescriptionHistory(request)
            )
        }
    }

    fun getShortageReasonList() {
        viewModelScope.launch(dispatcherIO) {
            try {
                shortageReasonList.postLoading()
                shortageReasonList.postSuccess(
                    nCDPharmacistRepository.getShortageReasonList(
                        TYPE_REFILL
                    )
                )
            } catch (e: Exception) {
                shortageReasonList.postError()
            }
        }
    }

    fun updateDispensePrescription(
        memberId: String? = null,
        patientReference: String? = null,
        patientVisitId: String? =null,
        request: List<DispenseUpdatePrescriptionRequest>
    ) {
        viewModelScope.launch(dispatcherIO) {
            val prescriptionRequest = DispensePrescriptionRequest(
                encounter = EncounterDetails(
                    memberId = memberId,
                    patientReference = patientReference,
                    patientVisitId = patientVisitId,
                    provenance = ProvanceDto()
                ),
                prescriptions = request
            )
            updatePrescriptionLiveData.postLoading()
            setAnalyticsData(
                UserDetail.startDateTime,
                eventName = AnalyticsDefinedParams.NCDPrescriptionUpdated,
                isCompleted = true
            )
            updatePrescriptionLiveData.postValue(
                nCDPharmacistRepository.updateDispensePrescription(
                    prescriptionRequest
                )
            )
        }
    }

}