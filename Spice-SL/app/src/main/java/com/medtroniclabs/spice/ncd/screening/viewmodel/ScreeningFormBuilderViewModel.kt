package com.medtroniclabs.spice.ncd.screening.viewmodel

import android.content.Context
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
import com.medtroniclabs.spice.db.entity.ScreeningEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.ncd.data.ValidatePatientModel
import com.medtroniclabs.spice.ncd.screening.repo.ScreeningRepository
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.utils.ConnectivityManager
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
    var duplicateNudgeLiveData = MutableLiveData<ValidatePatientModel>()
    var screeningUpdateResponse = MutableLiveData<Resource<ScreeningEntity>>()
    val getMentalQuestions: LiveData<MentalHealthEntity?> =
        getMentalQuestion.switchMap {
            screeningRepository.getMentalQuestion(it.second)
        }
    private var lastLocation: Location? = null
    val getFormData = MutableLiveData<String>()
    val formLayoutsLiveData: LiveData<String> = getFormData.switchMap {
        screeningRepository.getFormData(it)
    }
    private var phQ4Score: Int? = null
    private var fbsBloodGlucose: Double? = null
    private var rbsBloodGlucose: Double? = null

    @Inject
    lateinit var connectivityManager: ConnectivityManager

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

    fun savePatientScreeningInformation(
        context: Context,
        screeningEntityRawString: String,
        generalDetail: String,
        byteArray: ByteArray?,
        isReferred: Boolean,
        uploadStatus: Boolean,
        isRecursion: Boolean
    ) {

        viewModelScope.launch(dispatcherIO)
        {
            screeningSaveResponse.postLoading()
            try {
                val screeningEntity = ScreeningEntity(
                    screeningDetails = screeningEntityRawString,
                    generalDetails = generalDetail,
                    userId = SecuredPreference.getUserFhirId(),
                    uploadStatus = uploadStatus,
                    signature = byteArray,
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
}