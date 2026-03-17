package org.medtroniclabs.uhis.ncd.medicalreview.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.medtroniclabs.uhis.app.analytics.model.UserDetail
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsDefinedParams
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.ncd.data.NCDPatientStatusRequest
import org.medtroniclabs.uhis.ncd.medicalreview.repo.NCDMedicalReviewRepository
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.ui.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class NCDPatientHistoryViewModel @Inject constructor(
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
    private val ncdMedicalReviewRepository: NCDMedicalReviewRepository,
) : BaseViewModel(dispatcherIO) {
    var patientStatusId: String? = null
    var id: String? = null
    val resultDiabetesHashMap = HashMap<String, Any>()
    val resultHypertensionHashMap = HashMap<String, Any>()
    var value: String? = null
    private val getSymptomListByTypeForNCD = MutableLiveData<Triple<String, String, Boolean>>()
    val createNCDPatientStatus = MutableLiveData<Resource<HashMap<String, Any>>>()
    var yearForDiabetes: String? = null
    var yearForHypertension: String? = null

    val getSymptomListByTypeForNCDLiveData = getSymptomListByTypeForNCD.switchMap {
        ncdMedicalReviewRepository.getNCDDiagnosisList(listOf(it.first), it.second, it.third)
    }

    fun getSymptoms(
        type: String,
        gender: String,
        isPregnant: Boolean,
    ) {
        getSymptomListByTypeForNCD.value = Triple(type, gender, isPregnant)
    }

    fun createNCDPatientStatus(request: NCDPatientStatusRequest) {
        viewModelScope.launch(dispatcherIO) {
            createNCDPatientStatus.postLoading()
            setAnalyticsData(
                UserDetail.startDateTime,
                eventName = AnalyticsDefinedParams.NCDPatientHistoryCreationForNCD,
                isCompleted = true,
            )
            createNCDPatientStatus.postValue(ncdMedicalReviewRepository.createNCDPatientStatus(request))
        }
    }
}
