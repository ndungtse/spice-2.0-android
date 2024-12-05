package com.medtroniclabs.spice.ncd.medicalreview.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.ncd.data.NCDMedicalReviewUpdateModel
import com.medtroniclabs.spice.ncd.medicalreview.repo.NCDMedicalReviewRepository
import com.medtroniclabs.spice.network.SingleLiveEvent
import com.medtroniclabs.spice.network.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HrioViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val ncdMedicalReviewRepository: NCDMedicalReviewRepository
) : ViewModel() {
    val nextVisitResponse = SingleLiveEvent<Resource<HashMap<String, Any>>>()
    fun ncdUpdateNextVisitDate(request: NCDMedicalReviewUpdateModel) {
        viewModelScope.launch(dispatcherIO) {
            nextVisitResponse.postLoading()
            val response = ncdMedicalReviewRepository.ncdUpdateNextVisitDate(request)
            nextVisitResponse.postValue(response)
        }
    }
}