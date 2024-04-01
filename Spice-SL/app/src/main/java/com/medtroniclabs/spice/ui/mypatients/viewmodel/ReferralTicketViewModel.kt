package com.medtroniclabs.spice.ui.mypatients.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.PatientDetailReq
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.model.ReferralData
import com.medtroniclabs.spice.model.ReferredDate
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.utils.ConnectivityManager
import com.medtroniclabs.spice.ui.mypatients.repo.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReferralTicketViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {

    @Inject
    lateinit var connectivityManager: ConnectivityManager
    val patientsLiveData = MutableLiveData<Resource<PatientListRespModel>>()
    val referralTicketLiveData = MutableLiveData<Resource<ReferralData>>()
    var referralDates = listOf<ReferredDate>()
    var ticketId: String? = null

    fun getPatients(id: String) {
        viewModelScope.launch(dispatcherIO) {
            patientRepository.getPatients(patientsLiveData, PatientDetailReq(id))
        }
    }

    fun getReferralTicket(patientId: String? = null, ticketId: String? = null) {
        viewModelScope.launch(dispatcherIO) {
            patientRepository.getReferralTicket(
                referralTicketLiveData,
                PatientDetailReq(patientId = patientId, ticketId = ticketId)
            )
        }
    }
}