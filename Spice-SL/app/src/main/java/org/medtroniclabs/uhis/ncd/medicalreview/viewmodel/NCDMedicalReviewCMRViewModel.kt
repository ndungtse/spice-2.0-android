package org.medtroniclabs.uhis.ncd.medicalreview.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.data.history.HistoryEntity
import org.medtroniclabs.uhis.data.history.NCDMedicalReviewHistory
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.model.ReferralDetailRequest
import org.medtroniclabs.uhis.model.ReferredDate
import org.medtroniclabs.uhis.ncd.data.LifeStyleRequest
import org.medtroniclabs.uhis.ncd.data.LifeStyleResponse
import org.medtroniclabs.uhis.ncd.medicalreview.repo.NCDMedicalReviewRepository
import org.medtroniclabs.uhis.network.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NCDMedicalReviewCMRViewModel @Inject constructor(
    private var ncdMedicalReviewRepo: NCDMedicalReviewRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
) : ViewModel() {
    // prescription
    var prescriptionReferralDates = MutableLiveData<List<ReferredDate>>()
    val prescriptionTicketLiveData = MutableLiveData<Resource<HistoryEntity>>()
    var patientVisitId: String? = null

    // medical review History
    var medicalReferralDates = MutableLiveData<List<ReferredDate>>()
    val medicalReviewTicketLiveData = MutableLiveData<Resource<NCDMedicalReviewHistory>>()
    var medicalVisitId: String? = null

    // Investigation
    var investigationReferralDates = MutableLiveData<List<ReferredDate>>()
    val investigationTicketLiveData = MutableLiveData<Resource<HistoryEntity>>()
    var investigationVisitId: String? = null

    val lifeStyleResponse = MutableLiveData<Resource<ArrayList<LifeStyleResponse>>>()

    fun getPrescriptionHistory(
        patientId: String? = null,
        patientVisitId: String? = null,
    ) {
        viewModelScope.launch(dispatcherIO) {
            prescriptionTicketLiveData.postLoading()
            prescriptionTicketLiveData.postValue(
                ncdMedicalReviewRepo.getPrescription(
                    ReferralDetailRequest(
                        patientReference = patientId,
                        patientVisitId = patientVisitId,
                    ),
                ),
            )
        }
    }

    fun getMedicalReviewHistory(
        patientId: String? = null,
        medicalVisitId: String? = null,
    ) {
        viewModelScope.launch(dispatcherIO) {
            medicalReviewTicketLiveData.postLoading()
            medicalReviewTicketLiveData.postValue(
                ncdMedicalReviewRepo.getNCDMedicalReviewHistory(
                    ReferralDetailRequest(
                        patientReference = patientId,
                        patientVisitId = medicalVisitId,
                        requestFrom = null,
                    ),
                ),
            )
        }
    }

    fun getInvestigationHistory(
        patientReference: String? = null,
        investigationVisitId: String? = null,
    ) {
        viewModelScope.launch(dispatcherIO) {
            investigationTicketLiveData.postLoading()
            investigationTicketLiveData.postValue(
                ncdMedicalReviewRepo.getNCDInvestigation(
                    ReferralDetailRequest(
                        patientReference = patientReference,
                        patientVisitId = investigationVisitId,
                    ),
                ),
            )
        }
    }

    fun getNcdLifeStyleDetails(request: LifeStyleRequest) {
        viewModelScope.launch(dispatcherIO) {
            lifeStyleResponse.postLoading()
            val response = ncdMedicalReviewRepo.getNcdLifeStyleDetails(request)
            lifeStyleResponse.postValue(response)
        }
    }
}
