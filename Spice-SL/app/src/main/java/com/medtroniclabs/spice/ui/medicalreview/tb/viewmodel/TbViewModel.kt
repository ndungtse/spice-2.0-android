package com.medtroniclabs.spice.ui.medicalreview.tb.viewmodel

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.model.PatientEncounterResponse
import com.medtroniclabs.spice.data.model.TbMedicalReviewCreateRequest
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.medicalreview.tb.repo.TbMedicalReviewRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TbViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val tbRepo: TbMedicalReviewRepo
) : ViewModel() {
    var lastLocation: Location? = null
    var patientId: String? = null
    var memberId: String? = null
    val tbMetaResponse = MutableLiveData<Resource<Boolean>>()
    val tbCreateResponse = MutableLiveData<Resource<PatientEncounterResponse>>()

    fun getTbStaticData() {
        viewModelScope.launch(dispatcherIO) {
            tbRepo.getTbStaticData(tbMetaResponse)
        }
    }



    fun createMotherNeonate(request: TbMedicalReviewCreateRequest) {
        viewModelScope.launch(dispatcherIO) {
            try {
                tbCreateResponse.postLoading()
                tbCreateResponse.postValue(tbRepo.saveTbMedicalReview(request))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}