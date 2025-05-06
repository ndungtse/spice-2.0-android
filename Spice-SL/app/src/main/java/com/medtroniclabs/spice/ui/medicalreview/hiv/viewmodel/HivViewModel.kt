package com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.data.model.HivScreeningRequest
import com.medtroniclabs.spice.data.model.HivScreeningResponse
import com.medtroniclabs.spice.data.model.HivScreeningSummaryResponse
import com.medtroniclabs.spice.data.model.MedicalReviewEncounter
import com.medtroniclabs.spice.data.model.MultiSelectDropDownModel
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.medicalreview.hiv.repo.HivMedicalReviewRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HivViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var repository: HivMedicalReviewRepo
) : ViewModel() {
    var patientId: String? = null
    var memberId: String? = null
    var isHivSummary: Boolean = false
    val hivMetaListItems = MutableLiveData<Resource<List<MedicalReviewMetaItems>>>()
    val hivMetaResponseLiveData = MutableLiveData<Resource<Boolean>>()
    var nextFollowupDate: String? = null
    var lastLocation: Location? = null
    val resultHashMap = HashMap<String, Any>()
    var selectedHistoryListItem = ArrayList<MultiSelectDropDownModel>()
    var selectedPopulationType = ArrayList<MultiSelectDropDownModel>()
    var selectedLastTestForHIV: String? = null
    var selectedEntryPoint: String? = null
    val createHivScreeningLiveData = MutableLiveData<Resource<HivScreeningResponse>>()
    val hivScreeningDetailsLiveData = MutableLiveData<Resource<HivScreeningSummaryResponse>>()

    fun getHivMetaData() {
        viewModelScope.launch(dispatcherIO) {
            hivMetaResponseLiveData.postLoading()
            hivMetaResponseLiveData.postValue(
                repository.getStaticMetaData()
            )
        }
    }

    fun getHistoryListMetaItems() {
        viewModelScope.launch(dispatcherIO) {
            hivMetaListItems.postLoading()
            hivMetaListItems.postValue(repository.getHivMetaItems())
        }
    }



    fun createHivRequestModel(
        patientListRespModel: PatientListRespModel,
        selectedEligibilityPair: Pair<List<String?>, List<String?>>,
        haveHivTestTestedBeforePair: Pair<String?, String?>,
        hivTestResult: Triple<String?, String?, String?>,
        entryPoint: String?
    ) {
        viewModelScope.launch(dispatcherIO) {
            val currentTime =
                DateUtils.getCurrentDateAndTime(DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ)
            createHivScreeningLiveData.postValue(
                repository.createHivScreening(
                    HivScreeningRequest(
                        historyList = selectedEligibilityPair.first.filterNotNull(),
                        isHIVTestedBefore = haveHivTestTestedBeforePair.first,
                        hivTestDuration = haveHivTestTestedBeforePair.second,
                        encounter = MedicalReviewEncounter(
                            startTime = currentTime,
                            endTime = currentTime,
                            householdId = patientListRespModel.houseHoldId,
                            id = patientListRespModel.id,
                            latitude = lastLocation?.latitude ?: 0.0,
                            longitude = lastLocation?.longitude ?: 0.0,
                            memberId = patientListRespModel.memberId,
                            patientId = patientListRespModel.patientId,
                            provenance = ProvanceDto()
                        ),
                        entryPoint = entryPoint,
                        populationTypeList = selectedEligibilityPair.second.filterNotNull(),
                        a1TestResult = hivTestResult.first,
                        a2TestResult = hivTestResult.second,
                        a3TestResult = hivTestResult.third
                    )
                )
            )
        }
    }

    fun getHivScreeningDetails(request : HivScreeningResponse) {
        viewModelScope.launch(dispatcherIO) {
            hivScreeningDetailsLiveData.postLoading()
            hivScreeningDetailsLiveData.postValue(repository.getHivScreeningDetails(request))

        }
    }


}