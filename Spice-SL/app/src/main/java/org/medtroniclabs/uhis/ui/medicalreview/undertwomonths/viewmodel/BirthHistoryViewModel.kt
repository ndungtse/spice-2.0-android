package org.medtroniclabs.uhis.ui.medicalreview.undertwomonths.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.data.BirthHistoryRequest
import org.medtroniclabs.uhis.data.BirthHistoryResponse
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.repo.UnderTwoMonthsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BirthHistoryViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var repository: UnderTwoMonthsRepository,
) : ViewModel() {
    val birthHistoryLiveData = MutableLiveData<Resource<BirthHistoryResponse>>()
    val lowBirthWeight = 2.0

    fun getBirthHistoryDetails(
        patientId: String?,
        memberId: String?,
    ) {
        val birthHistoryRequest =
            BirthHistoryRequest(memberId = memberId, motherPatientId = patientId)
        viewModelScope.launch(dispatcherIO) {
            birthHistoryLiveData.postLoading()
            birthHistoryLiveData.postValue(
                repository.getBirthHistoryDetailsUnderTwoMonths(birthHistoryRequest),
            )
        }
    }
}
