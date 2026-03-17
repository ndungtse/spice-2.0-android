package org.medtroniclabs.uhis.ui.medicalreview.tb.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.data.model.MotherNeonateAncRequest
import org.medtroniclabs.uhis.data.model.TbHistory
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.ui.medicalreview.tb.repo.TbMedicalReviewRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TbPatientHistoryAndPresumptiveViewModel @Inject constructor(
    private val tbMedicalReviewRepo: TbMedicalReviewRepo,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
) : ViewModel() {
    val getHistory = MutableLiveData<Resource<TbHistory>>()

    fun fetchPatientHistory(motherNeonateAncRequest: MotherNeonateAncRequest) {
        viewModelScope.launch(dispatcherIO) {
            getHistory.postLoading()
            getHistory.postValue(
                tbMedicalReviewRepo.fetchTbAssessmentDetails(motherNeonateAncRequest),
            )
        }
    }
}
