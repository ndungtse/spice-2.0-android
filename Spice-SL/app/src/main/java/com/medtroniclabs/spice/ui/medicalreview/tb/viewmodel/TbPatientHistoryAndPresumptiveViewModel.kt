package com.medtroniclabs.spice.ui.medicalreview.tb.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.model.MotherNeonateAncRequest
import com.medtroniclabs.spice.data.model.TbHistory
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.medicalreview.tb.repo.TbMedicalReviewRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TbPatientHistoryAndPresumptiveViewModel @Inject constructor(
    private val tbMedicalReviewRepo: TbMedicalReviewRepo,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {
    val getHistory = MutableLiveData<Resource<TbHistory>>()
    fun fetchBmiList(motherNeonateAncRequest: MotherNeonateAncRequest) {
        viewModelScope.launch(dispatcherIO) {
            getHistory.postLoading()
            getHistory.postValue(
                tbMedicalReviewRepo.fetchTbAssessmentDetails(motherNeonateAncRequest)
            )
        }
    }
}