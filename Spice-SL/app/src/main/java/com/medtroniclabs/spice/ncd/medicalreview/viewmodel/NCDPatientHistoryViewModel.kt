package com.medtroniclabs.spice.ncd.medicalreview.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.ncd.medicalreview.repo.NCDMedicalReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class NCDPatientHistoryViewModel @Inject constructor(@IoDispatcher private val dispatcherIO: CoroutineDispatcher, private val ncdMedicalReviewRepository: NCDMedicalReviewRepository) :
    ViewModel() {

    val resultDiabetesHashMap = HashMap<String, Any>()
    val resultHypertensionHashMap = HashMap<String, Any>()
    var value:String? = null
    private val getSymptomListByTypeForNCD = MutableLiveData<String>()

    val getSymptomListByTypeForNCDLiveData = getSymptomListByTypeForNCD.switchMap {
        ncdMedicalReviewRepository.getSymptomListByTypeForNCD(it)
    }

    fun getSymptoms(type: String) {
        getSymptomListByTypeForNCD.value = type
    }
}