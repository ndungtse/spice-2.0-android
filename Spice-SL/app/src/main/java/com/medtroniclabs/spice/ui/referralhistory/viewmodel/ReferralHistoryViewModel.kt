package com.medtroniclabs.spice.ui.referralhistory.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.history.MedicalReviewHistory
import com.medtroniclabs.spice.data.history.PrescriptionHistoryEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.ReferralData
import com.medtroniclabs.spice.model.ReferralDetailRequest
import com.medtroniclabs.spice.model.ReferredDate
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.utils.ConnectivityManager
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.referralhistory.repo.ReferralHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReferralHistoryViewModel @Inject constructor(
    private val referralHistoryRepository: ReferralHistoryRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {

    @Inject
    lateinit var connectivityManager: ConnectivityManager
    val referralTicketLiveData = MutableLiveData<Resource<ReferralData>>()
    val prescriptionTicketLiveData = MutableLiveData<Resource<PrescriptionHistoryEntity>>()
    val medicalReviewTicketLiveData = MutableLiveData<Resource<MedicalReviewHistory>>()
    var referralDates = listOf<ReferredDate>()
    var prescriptionReferralDates = listOf<ReferredDate>()
    var medicalReferralDates = listOf<ReferredDate>()
    var ticketId: String? = null
    var prescriptionTicketId: String? = null
    var medicalTicketId: String? = null

    fun getReferralTicket(patientId: String? = null, ticketId: String? = null) {
        viewModelScope.launch(dispatcherIO) {
            referralTicketLiveData.postLoading()
            referralTicketLiveData.postValue(
                referralHistoryRepository.getReferralTicket(
                    ReferralDetailRequest(
                        patientId = patientId,
                        ticketId = ticketId,
                        type = MedicalReviewTypeEnums.medicalReview.name
                    )
                )
            )
        }
    }

    fun getPrescriptionHistory(patientId: String? = null, prescriptionTicketId: String? = null) {
        viewModelScope.launch(dispatcherIO) {
            prescriptionTicketLiveData.postLoading()
            prescriptionTicketLiveData.postValue(
                referralHistoryRepository.getPrescription(
                    ReferralDetailRequest(
                        patientReference = patientId,
                        encounterId = prescriptionTicketId,
                    )
                )
            )
        }
    }

    fun getMedicalReviewHistory(patientId: String? = null, medicalTicketId: String? = null) {
        viewModelScope.launch(dispatcherIO) {
            medicalReviewTicketLiveData.postLoading()
            medicalReviewTicketLiveData.postValue(
                referralHistoryRepository.getMedicalReviewHistory(
                    ReferralDetailRequest(
                        patientReference = patientId,
                        encounterId = medicalTicketId,
                    )
                )
            )
        }
    }
}