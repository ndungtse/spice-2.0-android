package com.medtroniclabs.spice.ui.mypatients.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.model.BpAndWeightResponse
import com.medtroniclabs.spice.data.model.MotherNeonateAncRequest
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.repo.MotherNeonateANCRepo
import com.medtroniclabs.spice.ui.medicalreview.tb.repo.TbMedicalReviewRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MotherNeonateBpWeightViewModel @Inject constructor(
    private val motherNeonateANCRepo: MotherNeonateANCRepo,
    private val tbMedicalReviewRepo: TbMedicalReviewRepo,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {
    val getBloodPressure = MutableLiveData<Resource<BpAndWeightResponse>>()
    val getWeight = MutableLiveData<Resource<BpAndWeightResponse>>()
    val getHeight = MutableLiveData<Resource<BpAndWeightResponse>>()
    fun fetchBloodPressure(motherNeonateAncRequest: MotherNeonateAncRequest) {
        viewModelScope.launch(dispatcherIO) {
            getBloodPressure.postLoading()
            getBloodPressure.postValue(
                motherNeonateANCRepo.fetchBloodPressure(
                    motherNeonateAncRequest
                )
            )
        }
    }

    fun fetchWeight(motherNeonateAncRequest: MotherNeonateAncRequest) {
        viewModelScope.launch(dispatcherIO) {
            getWeight.postLoading()
            getWeight.postValue(motherNeonateANCRepo.fetchWeight(motherNeonateAncRequest))
        }
    }

    fun getWeight(): Double? {
        return getWeight.value?.data?.weight
    }

    fun getBp(): BpAndWeightResponse? {
        return getBloodPressure.value?.data
    }

    fun fetchHeight(motherNeonateAncRequest: MotherNeonateAncRequest) {
        viewModelScope.launch(dispatcherIO) {
            getHeight.postLoading()
            getHeight.postValue(tbMedicalReviewRepo.fetchHeight(motherNeonateAncRequest))
        }
    }
}