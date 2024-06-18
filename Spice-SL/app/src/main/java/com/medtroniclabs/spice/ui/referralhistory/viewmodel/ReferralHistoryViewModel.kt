package com.medtroniclabs.spice.ui.referralhistory.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
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
    var referralDates = listOf<ReferredDate>()
    var ticketId: String? = null

    fun getReferralTicket(patientId: String? = null, ticketId: String? = null) {
        viewModelScope.launch(dispatcherIO) {
            referralTicketLiveData.postLoading()
            referralTicketLiveData.postValue(
                referralHistoryRepository.getReferralTicket(
                    ReferralDetailRequest(
                        patientId = patientId?.toLong(),
                        ticketId = ticketId,
                        type = MedicalReviewTypeEnums.medicalReview.name
                    )
                )
            )
        }
    }
}