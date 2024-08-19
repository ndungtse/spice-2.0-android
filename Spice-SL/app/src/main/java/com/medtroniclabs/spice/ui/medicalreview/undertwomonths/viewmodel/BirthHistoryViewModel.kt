package com.medtroniclabs.spice.ui.medicalreview.undertwomonths.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.BirthHistoryRequest
import com.medtroniclabs.spice.data.BirthHistoryResponse
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.UnderTwoMonthsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BirthHistoryViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var repository: UnderTwoMonthsRepository
) : ViewModel() {
    val birthHistoryLiveData = MutableLiveData<Resource<BirthHistoryResponse>>()
    val lowBirthWeight=1

    fun getBirthHistoryDetails(patientId: String?, memberId: String?) {
        val birthHistoryRequest =
            BirthHistoryRequest(memberId = memberId, motherPatientId = patientId)
        viewModelScope.launch(dispatcherIO) {
            birthHistoryLiveData.postLoading()
            birthHistoryLiveData.postValue(
                repository.getBirthHistoryDetailsUnderTwoMonths(birthHistoryRequest)
            )
        }
    }
}