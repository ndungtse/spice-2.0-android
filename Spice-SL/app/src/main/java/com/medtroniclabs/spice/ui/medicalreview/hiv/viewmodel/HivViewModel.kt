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
import com.medtroniclabs.spice.data.PregnancyDetailsModel
import com.medtroniclabs.spice.data.model.HivCreateScreeningSummaryResponse
import com.medtroniclabs.spice.data.model.HivMedicalReviewSummaryRequest
import com.medtroniclabs.spice.data.model.HivMedicalReviewSummaryResponse
import com.medtroniclabs.spice.data.model.HivRequestData
import com.medtroniclabs.spice.data.model.HivScreeningRequest
import com.medtroniclabs.spice.data.model.HivScreeningResponse
import com.medtroniclabs.spice.data.model.MedicalReviewEncounter
import com.medtroniclabs.spice.data.model.MultiSelectDropDownModel
import com.medtroniclabs.spice.data.model.PatientEncounterResponse
import com.medtroniclabs.spice.data.model.ViralLoadRequest
import com.medtroniclabs.spice.data.model.ViralLoadResponse
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.data.resource.CD4DetailsRequest
import com.medtroniclabs.spice.data.resource.CD4DetailsResponse
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.ARTResponse
import com.medtroniclabs.spice.model.ArtRequest
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.model.PatientsDataModel
import com.medtroniclabs.spice.model.PregnancySummaryRequest
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
    val getViralLoadLiveData = MutableLiveData<Resource<List<ViralLoadResponse>>>()
    val getARTLiveData = MutableLiveData<Resource<List<ARTResponse>>>()
    val hivCreateResponse = MutableLiveData<Resource<PatientEncounterResponse>>()
    var isEMTCTMR =  false
    val getPatientSummaryDetails = MutableLiveData<Resource<PregnancyDetailsModel>>()
    var cd4Value: String? = null
    var whovalue: String? = null
    var emtctVisitStatus: String? = null
    var ancVisit = -1
    var otherEntryPoint :String? = null

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
        hivEmtctResult: Pair<String, String>,
        clinicalNotes: String,
        encounterId:String?
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
                            id = encounterId,
                            latitude = lastLocation?.latitude ?: 0.0,
                            longitude = lastLocation?.longitude ?: 0.0,
                            memberId = patientListRespModel.memberId,
                            patientId = patientListRespModel.patientId,
                            provenance = ProvanceDto(),
                        ),
                        entryPoint = entryPoint,
                        otherEntryPoint = otherEntryPoint,
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
                        screeningType = if(isEMTCT) DefinedParams.EMTCT_HIV_MEDICAL_SCREENING else DefinedParams.HIV_MEDICAL_SCREENING,
                        clinicalNotes =  clinicalNotes,
                        id = encounterId
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
            repository.getHivPatientStatus(it, MedicalReviewTypeEnums.HIV.name)
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

    fun getViralLoadData(patientReference: String?, memberReference: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            getViralLoadLiveData.postLoading()
            val request = ViralLoadRequest(
                patientReference = patientReference,
                memberReference = memberReference
            )
            getViralLoadLiveData.postValue(repository.getViralLoadData(request))
        }
    }
    fun getARTData(
        patientReference: String?,
        isActive: Boolean,
        limit: Int,
        category: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            getARTLiveData.postLoading()
            val request = ArtRequest(
                patientReference = patientReference,
                isActive = isActive,
                limit = limit,
                category = category
            )
            getARTLiveData.postValue(repository.getARTData(request))
        }
    }
    fun hivCreate(request: HivRequestData) {
        viewModelScope.launch(dispatcherIO) {
            try {
                hivCreateResponse.postLoading()
                hivCreateResponse.postValue(repository.saveHIVMedicalReview(request))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun getPatientSummaryDetails(patientReference: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            getPatientSummaryDetails.postLoading()
            var pregnancySummaryReq= PregnancySummaryRequest(patientReference)
            getPatientSummaryDetails.postValue(
                repository.getPatientSummaryDetails(
                    pregnancySummaryReq
                )
            )
        }
    }



}