package com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.data.model.HivCreateScreeningSummaryResponse
import com.medtroniclabs.spice.data.model.HivMedicalReviewSummaryRequest
import com.medtroniclabs.spice.data.model.HivMedicalReviewSummaryResponse
import com.medtroniclabs.spice.data.model.HivScreeningRequest
import com.medtroniclabs.spice.data.model.HivScreeningResponse
import com.medtroniclabs.spice.data.model.MedicalReviewEncounter
import com.medtroniclabs.spice.data.model.MultiSelectDropDownModel
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.data.resource.CD4DetailsRequest
import com.medtroniclabs.spice.data.resource.CD4DetailsResponse
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.model.medicalreview.EMTCTVisitStatusRequest
import com.medtroniclabs.spice.model.medicalreview.EMTCTVisitStatusResponse
import com.medtroniclabs.spice.model.medicalreview.HivVitalsRequest
import com.medtroniclabs.spice.model.medicalreview.HivVitalsResponse
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.DiagnosisRepository
import com.medtroniclabs.spice.ui.medicalreview.hiv.repo.HivMedicalReviewRepo
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HivViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var repository: HivMedicalReviewRepo,
    private var repo: DiagnosisRepository
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
    val hivScreeningDetailsLiveData = MutableLiveData<Resource<HivCreateScreeningSummaryResponse>>()
    var encounterId : String? = null
    var patientReference : String? = null
    var nextVisitDate : String? = null
    val createHivMedicalReviewSummaryLiveData = MutableLiveData<Resource<HivMedicalReviewSummaryResponse>>()
    var id : String? = null
    var villageId : String? = null
    var selectedPatientStatus: String? = null
    var isSummary : Boolean = false
    val getHivPatientStatusMeta = MutableLiveData<String>()
    val getHivEmtctStatusMeta = MutableLiveData<String>()
    val createEMTCTVistStatusLiveData = MutableLiveData<Resource<EMTCTVisitStatusResponse>>()
    val hivVitalsLiveData = MutableLiveData<Resource<HivVitalsResponse>>()
    val hivVitalsByTypeLiveData = MutableLiveData<Resource<HivVitalsResponse>>()
    var selectedemtctVisitStatus: String? = null
    val shouldCloseParent = MutableLiveData<Boolean>()
    var isEMTCT = false
    var populationOther: String? = null
    var gestationalWeeks: String? = null
    var lastMenstrualPeriod: String? = null
    var expectedDateOfDelivery: String? = null
    var isViralLoad = false
    var isARTRegimen = false
    val hivCD4DetailLiveData = MutableLiveData<Resource<ArrayList<CD4DetailsResponse>>>()

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
        entryPoint: String?,
        hivEmtctResult: Pair<String, String>
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
                            provenance = ProvanceDto(),
                        ),
                        entryPoint = entryPoint,
                        populationTypeList = selectedEligibilityPair.second.filterNotNull(),
                        otherPopulationType = populationOther,
                        a1TestResult = hivTestResult.first,
                        a2TestResult = hivTestResult.second,
                        a3TestResult = hivTestResult.third,
                        gestationalInWeeks = gestationalWeeks?.toIntOrNull(),
                        lastMenstrualPeriod = lastMenstrualPeriod,
                        expectedDateOfDelivery = expectedDateOfDelivery,
                        hivSyphilisDuoTest = hivEmtctResult.first,
                        hbsAGTest = hivEmtctResult.second,
                        screeningType = if(isEMTCT) DefinedParams.EMTCT_HIV_MEDICAL_SCREENING else DefinedParams.HIV_MEDICAL_SCREENING
                    )
                )
            )
        }
    }

    fun getHivScreeningDetails(request: HivScreeningResponse) {
        viewModelScope.launch(dispatcherIO) {
            hivScreeningDetailsLiveData.postLoading()
            hivScreeningDetailsLiveData.postValue(repository.getHivScreeningDetails(request))
        }
    }

    fun createHivSummary(request: HivMedicalReviewSummaryRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            createHivMedicalReviewSummaryLiveData.postLoading()
            createHivMedicalReviewSummaryLiveData.postValue(repository.createHivSummary(request))
        }

    }

    fun getHivPatientStatusByCategory(category: String) {
        getHivPatientStatusMeta.value = category
    }

    val hivPatientStatusLiveData: LiveData<List<MedicalReviewMetaItems>> =
        getHivPatientStatusMeta.switchMap {
            repository.getHivPatientStatus(it, MedicalReviewTypeEnums.HIV.name)
        }


    fun getHivEmtctVistStatusByCategory(category: String) {
        getHivEmtctStatusMeta.value = category
    }

    val hivEmtctStatusLiveData: LiveData<List<MedicalReviewMetaItems>> =
        getHivEmtctStatusMeta.switchMap {
            repository.getHivPatientStatus(it, MedicalReviewTypeEnums.HIV_REVIEW.name)
        }


    fun createEMTCT(request: EMTCTVisitStatusRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            createEMTCTVistStatusLiveData.postLoading()
            createEMTCTVistStatusLiveData.postValue(repository.createEMTCT(request))
        }
    }

    fun getHivVitalsDetails(request: HivVitalsRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            hivVitalsLiveData.postLoading()
            hivVitalsLiveData.postValue(repository.getHivVitalsDetails(request))
        }
    }

    fun getHivVitalsDetailsbyType(request: HivVitalsRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            hivVitalsByTypeLiveData.postLoading()
            hivVitalsByTypeLiveData.postValue(repository.getHivVitalsDetails(request))
        }
    }


    fun getHivCD4Details(patientReference: String?, isCD4: Boolean, isCD4Percentage: Boolean) {
        viewModelScope.launch(dispatcherIO) {
            hivCD4DetailLiveData.postLoading()
            hivCD4DetailLiveData.postValue(
                repository.getHivCD4Details(
                    CD4DetailsRequest(
                        patientReference = patientReference,
                        isCD4 = isCD4,
                        isCD4Percentage = isCD4Percentage
                    )
                )
            )
        }
    }

}