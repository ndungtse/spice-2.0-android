package com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.model.MotherNeonateAncRequest
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.medicalreview.hiv.repo.HivMedicalReviewRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OpportunisticInfectionViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var hivMedicalReviewRepo: HivMedicalReviewRepo,
) : ViewModel() {
    val resultHashMap = hashMapOf<String, HashMap<String, String>>()

    val getOpportunisticInfection = MutableLiveData<Resource<HashMap<String, HashMap<String, String>?>>>()

    fun getOpportunisticInfection(
        memberId: String?,
        patientReference: String?,
    ) {
        viewModelScope.launch(dispatcherIO) {
            getOpportunisticInfection.postLoading()
            getOpportunisticInfection.postValue(
                hivMedicalReviewRepo.getOpportunisticInfection(MotherNeonateAncRequest(memberId = memberId, patientReference = patientReference)),
            )
        }
    }
}
