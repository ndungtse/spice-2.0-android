package com.medtroniclabs.spice.ncd.screening.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.db.entity.MentalHealthEntity
import com.medtroniclabs.spice.db.entity.RiskFactorEntity
import com.medtroniclabs.spice.db.entity.ScreeningEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.ncd.screening.repo.ScreeningRepository
import com.medtroniclabs.spice.network.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScreeningFormBuilderViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val screeningRepository: ScreeningRepository
) : ViewModel() {

    private var getMentalQuestion = MutableLiveData<Pair<String, String>>()
    var screeningSaveResponse = MutableLiveData<Resource<ScreeningEntity>>()
    var screeningUpdateResponse = MutableLiveData<Resource<ScreeningEntity>>()
    val getMentalQuestions: LiveData<MentalHealthEntity> =
        getMentalQuestion.switchMap {
            screeningRepository.getMentalQuestion(it.second)
        }
    private var lastLocation: Location? = null
    var summaryMap: Map<String, Any>? = null
    val getFormData = MutableLiveData<String>()
    val formLayoutsLiveData: LiveData<String> = getFormData.switchMap {
        screeningRepository.getFormData(it)
    }
    private var phQ4Score: Int? = null
    private var fbsBloodGlucose: Double? = null
    private var rbsBloodGlucose: Double? = null
    var systolicAverageSummary: Int? = null
    var diastolicAverageSummary: Int? = null

    fun getMentalQuestion(id: String, type: String) {
        getMentalQuestion.value = Pair(id, type)
    }

    fun getIdOfMentalHealth() = getMentalQuestion.value
    fun getFormData(formType: String) {
        getFormData.value = formType
    }

    fun setCurrentLocation(location: Location) {
        this.lastLocation = location
    }

    fun getCurrentLocation(): Location? {
        return this.lastLocation
    }

    fun setPhQ4Score(phQ4Score: Int) {
        this.phQ4Score = phQ4Score
    }

    fun getPhQ4Score(): Int? {
        return phQ4Score
    }

    fun setFbsBloodGlucose(glucose: Double) {
        fbsBloodGlucose = glucose
    }

    fun setRbsBloodGlucose(glucose: Double) {
        rbsBloodGlucose = glucose
    }

    fun getFbsBloodGlucose(): Double {
        return fbsBloodGlucose ?: 0.0
    }

    fun getRbsBloodGlucose(): Double {
        return rbsBloodGlucose ?: 0.0
    }

    fun getSystolicAverage(): Int? {
        return systolicAverageSummary
    }

    fun getDiastolicAverage(): Int? {
        return diastolicAverageSummary
    }

    fun savePatientScreeningInformation(
        screeningEntityRawString: String,
        generalDetail: String,
        isReferred: Boolean
    ) {

        viewModelScope.launch(dispatcherIO)
        {
            screeningSaveResponse.postLoading()
            try {
                val screeningEntity = ScreeningEntity(
                    screeningDetails = screeningEntityRawString,
                    generalDetails = generalDetail,
                    userId = SecuredPreference.getUserId(),
                    isReferred = isReferred
                )
                val rowId = screeningRepository.savePatientScreeningInformation(screeningEntity)
                screeningSaveResponse.postSuccess(rowId)
            } catch (e: Exception) {
                screeningSaveResponse.postError()
            }
        }
    }

    fun updatePatientScreeningInformation(
        generalDetail: String,
    ) {
        viewModelScope.launch(dispatcherIO)
        {
            try {
                screeningUpdateResponse.postLoading()
                screeningSaveResponse.value?.data?.let {
                    it.apply {
                        generalDetails = generalDetail
                    }
                    val screeningEntity = screeningRepository.savePatientScreeningInformation(it)
                    screeningUpdateResponse.postSuccess(screeningEntity)
                }
            } catch (e: Exception) {
                screeningUpdateResponse.postError()
            }
        }
    }
    private val getRiskEntityList = MutableLiveData<Boolean>()
    val getRiskEntityListLiveData: LiveData<List<RiskFactorEntity>> = getRiskEntityList.switchMap {
        screeningRepository.riskFactorListing()
    }

    fun getRiskEntityList() {
        getRiskEntityList.value = true
    }
}