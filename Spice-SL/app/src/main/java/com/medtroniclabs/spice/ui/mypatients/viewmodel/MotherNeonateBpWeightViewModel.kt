package com.medtroniclabs.spice.ui.mypatients.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.data.model.BpAndWeightResponse
import com.medtroniclabs.spice.data.model.MotherNeonateAncRequest
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.repo.MotherNeonateANCRepo
import com.medtroniclabs.spice.ui.medicalreview.tb.repo.TbMedicalReviewRepo
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MotherNeonateBpWeightViewModel @Inject constructor(
    private val motherNeonateANCRepo: MotherNeonateANCRepo,
    private val tbMedicalReviewRepo: TbMedicalReviewRepo,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
) : ViewModel() {
    val getBloodPressure = MutableLiveData<Resource<BpAndWeightResponse>>()
    val getWeight = MutableLiveData<Resource<BpAndWeightResponse>>()
    val getHeight = MutableLiveData<Resource<BpAndWeightResponse>>()
    val getBmi = MutableLiveData<Resource<BpAndWeightResponse>>()
    val getPatientType = MutableLiveData<Resource<HashMap<String, Any>>>()

    fun fetchBloodPressure(motherNeonateAncRequest: MotherNeonateAncRequest) {
        viewModelScope.launch(dispatcherIO) {
            getBloodPressure.postLoading()
            getBloodPressure.postValue(
                motherNeonateANCRepo.fetchBloodPressure(
                    motherNeonateAncRequest,
                ),
            )
        }
    }

    fun fetchWeight(motherNeonateAncRequest: MotherNeonateAncRequest) {
        viewModelScope.launch(dispatcherIO) {
            getWeight.postLoading()
            getWeight.postValue(motherNeonateANCRepo.fetchWeight(motherNeonateAncRequest))
        }
    }

    fun getWeight(): Double? = getWeight.value?.data?.weight

    fun getHeight(): Double? = getWeight.value?.data?.height

    fun getBp(): BpAndWeightResponse? = getBloodPressure.value?.data

    fun getHeights(): Double? = getHeight.value?.data?.height

    fun fetchHeight(motherNeonateAncRequest: MotherNeonateAncRequest) {
        viewModelScope.launch(dispatcherIO) {
            getHeight.postLoading()
            getHeight.postValue(tbMedicalReviewRepo.fetchHeight(motherNeonateAncRequest))
        }
    }

    fun fetchBmi(motherNeonateAncRequest: MotherNeonateAncRequest) {
        viewModelScope.launch(dispatcherIO) {
            getBmi.postLoading()
            getBmi.postValue(tbMedicalReviewRepo.fetchBmi(motherNeonateAncRequest))
        }
    }

    private val getPatientTypeMeta = MutableLiveData<String>()
    val getPatientTypeLiveData: LiveData<List<MedicalReviewMetaItems>> =
        getPatientTypeMeta.switchMap {
            tbMedicalReviewRepo.getExaminationsComplaints(it, MedicalReviewTypeEnums.TB.name)
        }

    fun setPatientType(category: String) {
        getPatientTypeMeta.value = category
    }

    fun getPatientType(request: MotherNeonateAncRequest) {
        viewModelScope.launch(dispatcherIO) {
            getPatientType.postLoading()
            getPatientType.postValue(tbMedicalReviewRepo.getPatientType(request))
        }
    }
}
