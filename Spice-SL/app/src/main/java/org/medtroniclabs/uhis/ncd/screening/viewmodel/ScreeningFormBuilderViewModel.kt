package org.medtroniclabs.uhis.ncd.screening.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import org.medtroniclabs.uhis.appextensions.postError
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.appextensions.postSuccess
import org.medtroniclabs.uhis.common.SecuredPreference
import org.medtroniclabs.uhis.data.LocalSpinnerResponse
import org.medtroniclabs.uhis.db.entity.MentalHealthEntity
import org.medtroniclabs.uhis.db.entity.ScreeningEntity
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.formgeneration.model.FormLayout
import org.medtroniclabs.uhis.ncd.screening.repo.ScreeningRepository
import org.medtroniclabs.uhis.network.SingleLiveEvent
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.network.utils.ConnectivityManager
import org.medtroniclabs.uhis.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScreeningFormBuilderViewModel @Inject constructor(
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
    private val screeningRepository: ScreeningRepository,
) : BaseViewModel(dispatcherIO) {
    private var getMentalQuestion = MutableLiveData<Pair<String, String>>()
    val villageSpinnerLiveData = MutableLiveData<Resource<LocalSpinnerResponse>>()
    var screeningSaveResponse = SingleLiveEvent<Resource<ScreeningEntity>>()
    var screeningUpdateResponse = MutableLiveData<Resource<ScreeningEntity>>()
    val getMentalQuestions: LiveData<MentalHealthEntity?> =
        getMentalQuestion.switchMap {
            screeningRepository.getMentalQuestion(it.second)
        }
    private var lastLocation: Location? = null
    private var phQ4Score: Int? = null
    private var fbsBloodGlucose: Double? = null
    private var rbsBloodGlucose: Double? = null
    var validatePatientResponseLiveDate =
        SingleLiveEvent<Resource<Pair<HashMap<String, Any>, List<FormLayout?>?>>>()

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    fun getMentalQuestion(
        id: String,
        type: String,
    ) {
        getMentalQuestion.value = Pair(id, type)
    }

    fun getVillages(tag: String) {
        viewModelScope.launch(dispatcherIO) {
            villageSpinnerLiveData.postLoading()
            villageSpinnerLiveData.postValue(
                screeningRepository.getVillagesByChiefDom(tag, SecuredPreference.getChiefdomId()),
            )
        }
    }

    fun getIdOfMentalHealth() = getMentalQuestion.value

    fun setCurrentLocation(location: Location) {
        this.lastLocation = location
    }

    fun getCurrentLocation(): Location? = this.lastLocation

    fun setPhQ4Score(phQ4Score: Int) {
        this.phQ4Score = phQ4Score
    }

    fun getPhQ4Score(): Int? = phQ4Score

    fun setFbsBloodGlucose(glucose: Double) {
        fbsBloodGlucose = glucose
    }

    fun setRbsBloodGlucose(glucose: Double) {
        rbsBloodGlucose = glucose
    }

    fun getFbsBloodGlucose(): Double = fbsBloodGlucose ?: 0.0

    fun getRbsBloodGlucose(): Double = rbsBloodGlucose ?: 0.0

    fun validatePatient(
        resp: HashMap<String, Any>,
        serverData: List<FormLayout?>?,
    ) {
        viewModelScope.launch(dispatcherIO) {
            validatePatientResponseLiveDate.postLoading()
            validatePatientResponseLiveDate.postValue(
                screeningRepository.validatePatient(resp, Pair(resp, serverData)),
            )
        }
    }

    fun savePatientScreeningInformation(
        screeningEntityRawString: String,
        generalDetail: String,
        eSignature: ByteArray?,
        isReferred: Boolean,
    ) {
        viewModelScope.launch(dispatcherIO) {
            screeningSaveResponse.postLoading()
            try {
                val screeningEntity = ScreeningEntity(
                    screeningDetails = screeningEntityRawString,
                    generalDetails = generalDetail,
                    userId = SecuredPreference.getUserFhirId(),
                    signature = eSignature,
                    isReferred = isReferred,
                )
                val rowId = screeningRepository.savePatientScreeningInformation(screeningEntity)
                screeningSaveResponse.postSuccess(rowId)
            } catch (e: Exception) {
                screeningSaveResponse.postError()
            }
        }
    }

    fun updatePatientScreeningInformation(generalDetail: String) {
        viewModelScope.launch(dispatcherIO) {
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
