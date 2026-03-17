package org.medtroniclabs.uhis.ui.medicalreview.hiv.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.data.model.MotherNeonateAncRequest
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.ui.medicalreview.hiv.repo.HivMedicalReviewRepo
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
