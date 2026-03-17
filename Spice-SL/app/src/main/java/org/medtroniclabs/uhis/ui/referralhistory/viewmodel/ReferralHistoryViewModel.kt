package org.medtroniclabs.uhis.ui.referralhistory.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.data.PncChildMedicalReview
import org.medtroniclabs.uhis.data.history.BirthDetails
import org.medtroniclabs.uhis.data.history.HistoryEntity
import org.medtroniclabs.uhis.data.history.MedicalReviewHistory
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.model.ReferralData
import org.medtroniclabs.uhis.model.ReferralDetailRequest
import org.medtroniclabs.uhis.model.ReferredDate
import org.medtroniclabs.uhis.model.medicalreview.RequestBirthDetails
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.network.utils.ConnectivityManager
import org.medtroniclabs.uhis.ui.BaseViewModel
import org.medtroniclabs.uhis.ui.medicalreview.utils.MedicalReviewTypeEnums
import org.medtroniclabs.uhis.ui.referralhistory.repo.ReferralHistoryRepository
import javax.inject.Inject

@HiltViewModel
class ReferralHistoryViewModel @Inject constructor(
    private val referralHistoryRepository: ReferralHistoryRepository,
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
) : BaseViewModel(dispatcherIO) {
    @Inject
    lateinit var connectivityManager: ConnectivityManager
    val referralTicketLiveData = MutableLiveData<Resource<ReferralData>>()
    val prescriptionTicketLiveData = MutableLiveData<Resource<HistoryEntity>>()
    val investigationTicketLiveData = MutableLiveData<Resource<HistoryEntity>>()
    val medicalReviewTicketLiveData = MutableLiveData<Resource<MedicalReviewHistory>>()
    val birthDetailsLiveData = MutableLiveData<Resource<BirthDetails>>()
    val medicalReviewTicketLiveDataPNC = MutableLiveData<Resource<PncChildMedicalReview>>()
    var referralDates = MutableLiveData<List<ReferredDate>>()
    var prescriptionReferralDates = MutableLiveData<List<ReferredDate>>()
    var investigationReferralDates = MutableLiveData<List<ReferredDate>>()
    var medicalReferralDates = MutableLiveData<List<ReferredDate>>()
    var ticketId: String? = null
    var prescriptionTicketId: String? = null
    var investigationTicketId: String? = null
    var medicalTicketId: String? = null
    var patientReference: String? = null
    var memberId: String? = null

    fun getReferralTicket(
        patientId: String? = null,
        ticketId: String? = null,
    ) {
        viewModelScope.launch(dispatcherIO) {
            referralTicketLiveData.postLoading()
            referralTicketLiveData.postValue(
                referralHistoryRepository.getReferralTicket(
                    ReferralDetailRequest(
                        patientId = patientId,
                        ticketId = ticketId,
                        type = MedicalReviewTypeEnums.medicalReview.name,
                        memberId = memberId,
                    ),
                ),
            )
        }
    }

    fun getPrescriptionHistory(
        patientId: String? = null,
        prescriptionTicketId: String? = null,
    ) {
        viewModelScope.launch(dispatcherIO) {
            prescriptionTicketLiveData.postLoading()
            prescriptionTicketLiveData.postValue(
                referralHistoryRepository.getPrescription(
                    ReferralDetailRequest(
                        patientReference = patientId,
                        encounterId = prescriptionTicketId,
                    ),
                ),
            )
        }
    }

    fun getMedicalReviewHistory(
        patientId: String? = null,
        medicalTicketId: String? = null,
    ) {
        viewModelScope.launch(dispatcherIO) {
            medicalReviewTicketLiveData.postLoading()
            medicalReviewTicketLiveData.postValue(
                referralHistoryRepository.getMedicalReviewHistory(
                    ReferralDetailRequest(
                        patientReference = patientId,
                        encounterId = medicalTicketId,
                    ),
                ),
            )
        }
    }

    fun getMedicalReviewHistoryPNC(
        patientId: String? = null,
        medicalTicketId: String? = null,
    ) {
        viewModelScope.launch(dispatcherIO) {
            medicalReviewTicketLiveDataPNC.postLoading()
            medicalReviewTicketLiveDataPNC.postValue(
                referralHistoryRepository.getMedicalReviewHistoryPNC(
                    ReferralDetailRequest(
                        patientReference = patientId,
                        encounterId = medicalTicketId,
                    ),
                ),
            )
        }
    }

    fun getBirthDetails(
        memberId: String? = null,
        patientReference: String? = null,
    ) {
        viewModelScope.launch(dispatcherIO) {
            birthDetailsLiveData.postLoading()
            birthDetailsLiveData.postValue(
                referralHistoryRepository.getBirthDetails(
                    RequestBirthDetails(
                        patientReference = patientReference,
                        memberId = memberId,
                    ),
                ),
            )
        }
    }

    fun getInvestigationHistory(
        patientId: String? = null,
        investigationTicketId: String? = null,
    ) {
        viewModelScope.launch(dispatcherIO) {
            investigationTicketLiveData.postLoading()
            investigationTicketLiveData.postValue(
                referralHistoryRepository.getInvestigation(
                    ReferralDetailRequest(
                        patientReference = patientId,
                        encounterId = investigationTicketId,
                    ),
                ),
            )
        }
    }
}
