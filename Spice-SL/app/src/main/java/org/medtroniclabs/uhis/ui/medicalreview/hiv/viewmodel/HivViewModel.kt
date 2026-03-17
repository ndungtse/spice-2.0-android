package org.medtroniclabs.uhis.ui.medicalreview.hiv.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.common.DateUtils
import org.medtroniclabs.uhis.common.DefinedParams
import org.medtroniclabs.uhis.data.MedicalReviewMetaItems
import org.medtroniclabs.uhis.data.MedicalReviewSummarySubmitRequest
import org.medtroniclabs.uhis.data.PregnancyDetailsModel
import org.medtroniclabs.uhis.data.model.HivCreateScreeningSummaryResponse
import org.medtroniclabs.uhis.data.model.HivRequestData
import org.medtroniclabs.uhis.data.model.HivScreeningRequest
import org.medtroniclabs.uhis.data.model.HivScreeningResponse
import org.medtroniclabs.uhis.data.model.MedicalReviewEncounter
import org.medtroniclabs.uhis.data.model.MultiSelectDropDownModel
import org.medtroniclabs.uhis.data.model.PatientEncounterResponse
import org.medtroniclabs.uhis.data.model.ViralLoadRequest
import org.medtroniclabs.uhis.data.model.ViralLoadResponse
import org.medtroniclabs.uhis.data.offlinesync.model.ProvanceDto
import org.medtroniclabs.uhis.data.resource.CD4DetailsRequest
import org.medtroniclabs.uhis.data.resource.CD4DetailsResponse
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.model.ARTResponse
import org.medtroniclabs.uhis.model.ArtRequest
import org.medtroniclabs.uhis.model.PatientListRespModel
import org.medtroniclabs.uhis.model.PregnancySummaryRequest
import org.medtroniclabs.uhis.model.medicalreview.EMTCTVisitStatusRequest
import org.medtroniclabs.uhis.model.medicalreview.EMTCTVisitStatusResponse
import org.medtroniclabs.uhis.model.medicalreview.HivVitalsRequest
import org.medtroniclabs.uhis.model.medicalreview.HivVitalsResponse
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.ui.medicalreview.hiv.repo.HivMedicalReviewRepo
import org.medtroniclabs.uhis.ui.medicalreview.utils.MedicalReviewTypeEnums
import javax.inject.Inject

@HiltViewModel
class HivViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var repository: HivMedicalReviewRepo,
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
    var encounterId: String? = null
    var patientReference: String? = null
    var nextVisitDate: String? = null
    val createHivMedicalReviewSummaryLiveData = MutableLiveData<Resource<HashMap<String, Any>>>()
    var id: String? = null
    var villageId: String? = null
    var selectedPatientStatus: String? = null
    var isSummary: Boolean = false
    val getHivPatientStatusMeta = MutableLiveData<String>()
    val getHivEmtctStatusMeta = MutableLiveData<String>()
    val createEMTCTVistStatusLiveData = MutableLiveData<Resource<EMTCTVisitStatusResponse>>()
    val hivVitalsLiveData = MutableLiveData<Resource<HivVitalsResponse>>()
    val hivVitalsByTypeLiveData = MutableLiveData<Resource<HivVitalsResponse>>()
    var selectedemtctVisitStatus: String? = null
    var isEMTCT = false
    var populationOther: String? = null
    var gestationalWeeks: String? = null
    var lastMenstrualPeriod: String? = null
    var expectedDateOfDelivery: String? = null
    val hivCD4DetailLiveData = MutableLiveData<Resource<ArrayList<CD4DetailsResponse>>>()
    val getViralLoadLiveData = MutableLiveData<Resource<List<ViralLoadResponse>>>()
    val getARTLiveData = MutableLiveData<Resource<List<ARTResponse>>>()
    val hivCreateResponse = MutableLiveData<Resource<PatientEncounterResponse>>()
    var isEMTCTMR = false
    val getPatientSummaryDetails = MutableLiveData<Resource<PregnancyDetailsModel>>()
    var cd4Value: String? = null
    var whovalue: String? = null
    var emtctVisitStatus: String? = null
    var ancVisit = -1
    var otherEntryPoint: String? = null

    fun getHivMetaData() {
        viewModelScope.launch(dispatcherIO) {
            hivMetaResponseLiveData.postLoading()
            hivMetaResponseLiveData.postValue(
                repository.getStaticMetaData(),
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
        encounterId: String?,
        isConsentGiven: Boolean,
        weight: Double?,
        height: Double?,
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
                        screeningType = if (isEMTCT) DefinedParams.EMTCT_HIV_MEDICAL_SCREENING else DefinedParams.HIV_MEDICAL_SCREENING,
                        clinicalNotes = clinicalNotes,
                        id = encounterId,
                        isConsentGiven = isConsentGiven,
                        weight = weight,
                        height = height,
                    ),
                ),
            )
        }
    }

    fun getHivScreeningDetails(request: HivScreeningResponse) {
        viewModelScope.launch(dispatcherIO) {
            hivScreeningDetailsLiveData.postLoading()
            hivScreeningDetailsLiveData.postValue(repository.getHivScreeningDetails(request))
        }
    }

    fun createHivSummary(request: MedicalReviewSummarySubmitRequest) {
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

    fun getHivCD4Details(
        patientReference: String?,
        isCD4: Boolean,
        isCD4Percentage: Boolean,
    ) {
        viewModelScope.launch(dispatcherIO) {
            hivCD4DetailLiveData.postLoading()
            hivCD4DetailLiveData.postValue(
                repository.getHivCD4Details(
                    CD4DetailsRequest(
                        patientReference = patientReference,
                        isCD4 = isCD4,
                        isCD4Percentage = isCD4Percentage,
                    ),
                ),
            )
        }
    }

    fun getViralLoadData(
        patientReference: String?,
        memberReference: String?,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            getViralLoadLiveData.postLoading()
            val request = ViralLoadRequest(
                patientReference = patientReference,
                memberReference = memberReference,
            )
            getViralLoadLiveData.postValue(repository.getViralLoadData(request))
        }
    }

    fun getARTData(
        patientReference: String?,
        isActive: Boolean,
        limit: Int,
        category: String,
        memberId: String?,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            getARTLiveData.postLoading()
            val request = ArtRequest(
                patientReference = patientReference,
                isActive = isActive,
                limit = limit,
                category = category,
                memberReference = memberId,
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
            var pregnancySummaryReq = PregnancySummaryRequest(patientReference)
            getPatientSummaryDetails.postValue(
                repository.getPatientSummaryDetails(
                    pregnancySummaryReq,
                ),
            )
        }
    }

    fun getSubmitCreateId(): String? = createHivScreeningLiveData.value?.data?.encounterId
}
