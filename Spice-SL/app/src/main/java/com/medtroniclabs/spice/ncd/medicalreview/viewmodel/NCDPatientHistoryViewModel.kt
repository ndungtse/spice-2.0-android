package com.medtroniclabs.spice.ncd.medicalreview.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.ncd.data.NCDPatientStatusRequest
import com.medtroniclabs.spice.ncd.medicalreview.repo.NCDMedicalReviewRepository
import com.medtroniclabs.spice.network.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NCDPatientHistoryViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val ncdMedicalReviewRepository: NCDMedicalReviewRepository
) : ViewModel() {

    val resultDiabetesHashMap = HashMap<String, Any>()
    val resultHypertensionHashMap = HashMap<String, Any>()
    var value: String? = null
    private val getSymptomListByTypeForNCD = MutableLiveData<Pair<String,String>>()
    val createNCDPatientStatus = MutableLiveData<Resource<HashMap<String, Any>>>()
    var yearForDiabetes :String? = null
    var yearForHypertension :String? = null

    val getSymptomListByTypeForNCDLiveData = getSymptomListByTypeForNCD.switchMap {
        ncdMedicalReviewRepository.getNCDDiagnosisList(listOf(it.first), it.second)
    }

    fun getSymptoms(type: String,gender: String) {
        getSymptomListByTypeForNCD.value = Pair(type,gender)
    }

    fun createNCDPatientStatus(request: NCDPatientStatusRequest) {
        viewModelScope.launch(dispatcherIO) {
            createNCDPatientStatus.postLoading()
            createNCDPatientStatus.postValue(ncdMedicalReviewRepository.createNCDPatientStatus(request))
        }
    }
}