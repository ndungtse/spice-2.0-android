package org.medtroniclabs.uhis.ui.medicalreview.pharmacist.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.medtroniclabs.uhis.app.analytics.model.UserDetail
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsDefinedParams
import org.medtroniclabs.uhis.appextensions.postError
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.appextensions.postSuccess
import org.medtroniclabs.uhis.common.DefinedParams.TYPE_REFILL
import org.medtroniclabs.uhis.data.DispensePrescriptionRequest
import org.medtroniclabs.uhis.data.DispensePrescriptionResponse
import org.medtroniclabs.uhis.data.DispenseUpdatePrescriptionRequest
import org.medtroniclabs.uhis.data.DispenseUpdateRequest
import org.medtroniclabs.uhis.data.DispenseUpdateResponse
import org.medtroniclabs.uhis.data.EncounterDetails
import org.medtroniclabs.uhis.data.ShortageReasonEntity
import org.medtroniclabs.uhis.data.offlinesync.model.ProvanceDto
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.ui.BaseViewModel
import org.medtroniclabs.uhis.ui.medicalreview.pharmacist.repo.NCDPharmacistRepository
import javax.inject.Inject

@HiltViewModel
class NCDPharmacistViewModel @Inject constructor(
    private var nCDPharmacistRepository: NCDPharmacistRepository,
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
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
                nCDPharmacistRepository.getDispensePrescriptionHistory(request),
            )
        }
    }

    fun getShortageReasonList() {
        viewModelScope.launch(dispatcherIO) {
            try {
                shortageReasonList.postLoading()
                shortageReasonList.postSuccess(
                    nCDPharmacistRepository.getShortageReasonList(
                        TYPE_REFILL,
                    ),
                )
            } catch (e: Exception) {
                shortageReasonList.postError()
            }
        }
    }

    fun updateDispensePrescription(
        memberId: String? = null,
        patientReference: String? = null,
        patientVisitId: String? = null,
        request: List<DispenseUpdatePrescriptionRequest>,
    ) {
        viewModelScope.launch(dispatcherIO) {
            val prescriptionRequest = DispensePrescriptionRequest(
                encounter = EncounterDetails(
                    memberId = memberId,
                    patientReference = patientReference,
                    patientVisitId = patientVisitId,
                    provenance = ProvanceDto(),
                ),
                prescriptions = request,
            )
            updatePrescriptionLiveData.postLoading()
            setAnalyticsData(
                UserDetail.startDateTime,
                eventName = AnalyticsDefinedParams.NCDPrescriptionUpdated,
                isCompleted = true,
            )
            updatePrescriptionLiveData.postValue(
                nCDPharmacistRepository.updateDispensePrescription(
                    prescriptionRequest,
                ),
            )
        }
    }
}
