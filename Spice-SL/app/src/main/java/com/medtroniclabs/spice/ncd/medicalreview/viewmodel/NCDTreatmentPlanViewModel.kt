package com.medtroniclabs.spice.ncd.medicalreview.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.APIResponse
import com.medtroniclabs.spice.db.entity.TreatmentPlanEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.ncd.data.NCDTreatmentPlanModel
import com.medtroniclabs.spice.ncd.medicalreview.repo.NCDTreatmentPlanRepo
import com.medtroniclabs.spice.network.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NCDTreatmentPlanViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val ncdTreatmentPlanRepo: NCDTreatmentPlanRepo
) : ViewModel() {
    var patientReference: String? = null
    var memberReference: String? = null

    var updateNCDTreatmentPlanLiveData =
        MutableLiveData<Resource<APIResponse<NCDTreatmentPlanModel>>>()
    var getNCDTreatmentPlanLiveData =
        MutableLiveData<Resource<APIResponse<NCDTreatmentPlanModel>>>()

    var medicalReviewFrequency: TreatmentPlanEntity? = null
    var bpCheckFrequency: TreatmentPlanEntity? = null
    var bgCheckFrequency: TreatmentPlanEntity? = null
    var hba1cCheckFrequency: TreatmentPlanEntity? = null

    private var frequencies = MutableLiveData<Boolean>()
    val allFrequencies: LiveData<List<TreatmentPlanEntity>> =
        frequencies.switchMap { ncdTreatmentPlanRepo.getFrequencies() }

    fun getFrequencies() {
        frequencies.value = true
    }

    fun getNCDTreatmentPlan(request: NCDTreatmentPlanModel) {
        viewModelScope.launch(dispatcherIO) {
            getNCDTreatmentPlanLiveData.postLoading()
            getNCDTreatmentPlanLiveData.postValue(
                ncdTreatmentPlanRepo.getNCDTreatmentPlan(
                    request
                )
            )
        }
    }

    fun updateNCDTreatmentPlan(request: NCDTreatmentPlanModel) {
        viewModelScope.launch(dispatcherIO) {
            updateNCDTreatmentPlanLiveData.postLoading()
            updateNCDTreatmentPlanLiveData.postValue(
                ncdTreatmentPlanRepo.updateNCDTreatmentPlan(
                    request
                )
            )
        }
    }
}