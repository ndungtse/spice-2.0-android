package com.medtroniclabs.spice.ui.mypatients.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.ncd.data.MedicalReviewRequestResponse
import com.medtroniclabs.spice.ncd.data.MedicalReviewResponse
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.mypatients.repo.NCDMedicalReviewRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NCDMedicalReviewViewModel @Inject constructor(
    private var ncdMedicalReviewRepo: NCDMedicalReviewRepo,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {
    val ncdMedicalReviewStaticLiveData = MutableLiveData<Resource<Boolean>>()
    val createMedicalReview = MutableLiveData<Resource<MedicalReviewResponse>>()

    fun getStaticMetaData() {
        viewModelScope.launch(dispatcherIO) {
            ncdMedicalReviewStaticLiveData.postLoading()
            ncdMedicalReviewStaticLiveData.postValue(ncdMedicalReviewRepo.getNcdMedicalReviewStaticData())
        }
    }

    fun createNCDMedicalReview(request: MedicalReviewRequestResponse) {
        viewModelScope.launch(dispatcherIO) {
            createMedicalReview.postLoading()
            createMedicalReview.postValue(
                ncdMedicalReviewRepo.createNCDMedicalReview(request)
            )
        }
    }
}