package org.medtroniclabs.uhis.ncd.medicalreview.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import org.medtroniclabs.uhis.app.analytics.model.UserDetail
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsDefinedParams
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.data.model.MultiSelectDropDownModel
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.ncd.data.NCDMentalHealthMedicalReviewDetails
import org.medtroniclabs.uhis.ncd.data.NCDMentalHealthStatusRequest
import org.medtroniclabs.uhis.ncd.medicalreview.repo.NCDMedicalReviewRepository
import org.medtroniclabs.uhis.network.SingleLiveEvent
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NCDMentalHealthViewModel @Inject constructor(
    private val ncdMedicalReviewRepository: NCDMedicalReviewRepository,
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
) : BaseViewModel(dispatcherIO) {
    var patientStatusId: String? = null
    var id: String? = null
    var mentalHealthStatusId: String? = null
    var substanceUseStatusId: String? = null
    val resultDiabetesHashMap = HashMap<String, Any>()
    var value: String? = null
    var encounterId: String? = null
    var questionarieId: String? = null
    var observationId: String? = null
    var yearForDiabetes: String? = null
    var yearForHypertension: String? = null
    var yearForMentalHealth: Int? = null
    var yearForSubstanceUse: Int? = null
    var mentalHealthComments: String? = null
    var substanceUseComments: String? = null
    val resultHypertensionHashMap = HashMap<String, Any>()
    val resultMentalHealthHashMap = HashMap<String, Any>()
    val resultSubstanceUseHashMap = HashMap<String, Any>()
    private val getSymptomListByTypeForNCD = MutableLiveData<Triple<String, String, Boolean>>()
    private val getSymptomListByTypeForNCDMentalHealth = MutableLiveData<Triple<String, String, Boolean>>()
    private val getSymptomListByTypeForNCDSubstanceAbuse = MutableLiveData<Triple<String, String, Boolean>>()
    var selectedMentalHealthListItem = ArrayList<MultiSelectDropDownModel>()
    var selectedSubstanceListItem = ArrayList<MultiSelectDropDownModel>()
    val createMentalHealthStatus = MutableLiveData<Resource<HashMap<String, Any>>>()
    val assessmentMentalHealth =
        SingleLiveEvent<Resource<String>>()
    val mentalHealthDetails = SingleLiveEvent<Resource<HashMap<String, Any>>>()

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

    val getMHLiveData = getSymptomListByTypeForNCDMentalHealth.switchMap {
        ncdMedicalReviewRepository.getNCDDiagnosisList(listOf(it.first), it.second, it.third)
    }

    val getSubstanceAbuseLiveData = getSymptomListByTypeForNCDSubstanceAbuse.switchMap {
        ncdMedicalReviewRepository.getNCDDiagnosisList(listOf(it.first), it.second, it.third)
    }

    fun getMH(
        type: String,
        gender: String,
        isPregnant: Boolean,
    ) {
        getSymptomListByTypeForNCDMentalHealth.value = Triple(type, gender, isPregnant)
    }

    fun getSubstanceAbuse(
        type: String,
        gender: String,
        isPregnant: Boolean,
    ) {
        getSymptomListByTypeForNCDSubstanceAbuse.value = Triple(type, gender, isPregnant)
    }

    fun createMentalHealthStatus(request: NCDMentalHealthStatusRequest) {
        viewModelScope.launch(dispatcherIO) {
            createMentalHealthStatus.postLoading()
            setAnalyticsData(
                UserDetail.startDateTime,
                eventName = AnalyticsDefinedParams.NCDPatientHistoryCreationForMentalHealth,
                isCompleted = true,
            )
            createMentalHealthStatus.postValue(ncdMedicalReviewRepository.createMentalHealthStatus(request))
        }
    }

    fun ncdMentalHealthMedicalReviewCreate(
        request: JsonObject,
        isAssessment: Boolean,
    ) {
        viewModelScope.launch(dispatcherIO) {
            assessmentMentalHealth.postLoading()
            assessmentMentalHealth.postValue(
                ncdMedicalReviewRepository.ncdMentalHealthMedicalReviewCreate(
                    request,
                    isAssessment,
                ),
            )
        }
    }

    fun ncdMentalHealthMedicalReviewDetails(
        request: NCDMentalHealthMedicalReviewDetails,
        isAssessment: Boolean,
    ) {
        viewModelScope.launch(dispatcherIO) {
            mentalHealthDetails.postLoading()
            mentalHealthDetails.postValue(
                ncdMedicalReviewRepository.ncdMentalHealthMedicalReviewDetails(
                    request,
                    isAssessment,
                ),
            )
        }
    }
}
