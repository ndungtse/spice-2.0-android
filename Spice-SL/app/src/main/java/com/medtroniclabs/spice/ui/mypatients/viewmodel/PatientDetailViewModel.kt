package com.medtroniclabs.spice.ui.mypatients.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.app.analytics.model.UserDetail
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.db.entity.ClinicalWorkflowEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.PatientDetailRequest
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.model.PregnancyDetails
import com.medtroniclabs.spice.ncd.data.NCDInstructionModel
import com.medtroniclabs.spice.ncd.data.NCDPregnancyRiskUpdate
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil
import com.medtroniclabs.spice.ncd.medicalreview.repo.NCDMedicalReviewRepository
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.BaseViewModel
import com.medtroniclabs.spice.ui.mypatients.repo.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatientDetailViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
    private val ncdMedicalReviewRepository: NCDMedicalReviewRepository
): BaseViewModel(dispatcherIO) {

    var dateOfDelivery: String? =null
    var childPatientDetails: String? = null
    val updatePregnancyRisk = MutableLiveData<Resource<Boolean>>()
    val ncdInstructionModelResponse = MutableLiveData<Resource<NCDInstructionModel>>()
    val patientDetailsLiveData = MutableLiveData<Resource<PatientListRespModel>>()
    val patientDetailsNeonateLiveData = MutableLiveData<Resource<PatientListRespModel>>()
    var isFamilyPlanning: Boolean = false

    //the below id is one which get from patient details response
    var patientDetailsId : String? = null
    var isSummary : Boolean = false

    var encounterId: String ?= null
    var childEncounterId: String ?= null

    var origin: String? = null
    var mrMenuId: String? = null
    var isCmr: Boolean = false
    var forceRefresh: Boolean = false

    var neonateOutCome: String?=null

    var chwName:String?=null
    var occupation:String?=null
    var presumptiveTbNo: String? = null
    var maritalStatus:String?=null
    val clinicalWorkflowsMenusLiveData = MutableLiveData<List<ClinicalWorkflowEntity>>()
    var artCode: String? = null
    var patientCurrentStatus = MutableLiveData<String>()

    var isEmtctFlow: Boolean = false
    var hivTestedPositive: Boolean = false

    fun getPatients(id: String, assessmentType: String? = null, origin: String? = null) {
        viewModelScope.launch(dispatcherIO) {
            patientDetailsLiveData.postLoading()
            patientDetailsLiveData.postValue(patientRepository.getPatients(PatientDetailRequest(patientId = id, assessmentType = assessmentType, id = id, type = origin)))
        }
    }
    fun getPatientsForNCD(id: String) {
        viewModelScope.launch(dispatcherIO) {
            patientDetailsLiveData.postLoading()
            patientDetailsLiveData.postValue(patientRepository.getPatientBasedOnId(id))
        }
    }


    fun getLastRefillVisitId() : String? {
        return patientDetailsLiveData.value?.data?.prescribedDetails?.lastRefillVisitId
    }

    fun getPatientFHIRId(): String? {
        return patientDetailsLiveData.value?.data?.id
    }

    fun getPatientVillageId(): String? {
        return patientDetailsLiveData.value?.data?.villageId
    }
    fun getPatientMemberId(): String? {
        return patientDetailsLiveData.value?.data?.memberId
    }

    fun getPatientId(): String? {
        return patientDetailsLiveData.value?.data?.patientId
    }

    fun getEnrollmentType(): String? {
        return patientDetailsLiveData.value?.data?.enrollmentType
    }

    fun getIdentityValue(): String? {
        return patientDetailsLiveData.value?.data?.identityValue
    }

    fun getChildPatientName(): String? {
        return patientDetailsLiveData.value?.data?.pregnancyDetails?.neonatePatientId
    }
    fun getPatientHouseholdId(): String? {
        return patientDetailsLiveData.value?.data?.houseHoldId
    }

    fun getAncVisit(): Int {
        return patientDetailsLiveData.value?.data?.pregnancyDetails?.ancVisitMedicalReview?.takeIf { true }
            ?.plus(1)
            ?: 1
    }
    fun getPncVisit(): Int {
        return patientDetailsLiveData.value?.data?.pregnancyDetails?.pncVisitMedicalReview?.takeIf { true }
            ?.plus(1)
            ?: 1
    }

    fun getPatientLmb():String? {
        return patientDetailsLiveData.value?.data?.pregnancyDetails?.lastMenstrualPeriod
    }

    fun getVillageId(): String? {
        return patientDetailsLiveData.value?.data?.villageId
    }

    fun getNCDInitialMedicalReview():Boolean {
        return patientDetailsLiveData.value?.data?.initialReviewed ?: false
    }

    fun getGenderIsFemale():Boolean {
        return patientDetailsLiveData.value?.data?.gender?.equals(DefinedParams.female, ignoreCase = true) == true
    }

    fun getGender(): String? {
        return patientDetailsLiveData.value?.data?.gender
    }

    fun isPatientEnrolled(): Boolean {
        return !patientDetailsLiveData.value?.data?.programId.isNullOrBlank()
    }

    fun recentBP(): String {
        val avgBP = patientDetailsLiveData.value?.data?.avgBloodPressure
        return if (avgBP.isNullOrEmpty()) {
            ""
        } else {
            "$avgBP ${NCDMRUtil.mmHg}"
        }
    }

    fun recentGlucose(): String {
        val glucoseValue = patientDetailsLiveData.value?.data?.glucoseValue
        val glucoseUnit = patientDetailsLiveData.value?.data?.glucoseUnit
        return if (glucoseValue.isNullOrEmpty() || glucoseUnit.isNullOrEmpty()) {
            ""
        } else {
            "$glucoseValue ($glucoseUnit)"
        }
    }

    fun getWeightInKG(): Double? {
        return patientDetailsLiveData.value?.data?.weight
    }

    fun getPregnantDetails(): PregnancyDetails? {
        return patientDetailsLiveData.value?.data?.pregnancyDetails
    }

    fun isPregnant(): Boolean {
        return getGenderIsFemale() && patientDetailsLiveData.value?.data?.isPregnant == true
    }

    fun ncdGetInstructions() {
        viewModelScope.launch(dispatcherIO) {
            ncdInstructionModelResponse.postLoading()
            setAnalyticsData(
                UserDetail.startDateTime,
                eventName = AnalyticsDefinedParams.NCDInstructionPregnancyRisk,
                isCompleted = true
            )
            ncdInstructionModelResponse.postValue(ncdMedicalReviewRepository.ncdGetInstructions())
        }
    }

    fun ncdUpdatePregnancyRisk(request: NCDPregnancyRiskUpdate) {
        viewModelScope.launch(dispatcherIO) {
            updatePregnancyRisk.postLoading()
            setAnalyticsData(
                UserDetail.startDateTime,
                eventName = AnalyticsDefinedParams.NCDUpdatePregnancyRisk,
                isCompleted = true
            )
            updatePregnancyRisk.postValue(ncdMedicalReviewRepository.ncdUpdatePregnancyRisk(request))
        }
    }

    fun getMenuForClinicalWorkflows() {
        viewModelScope.launch(dispatcherIO) {
            clinicalWorkflowsMenusLiveData.postValue(patientRepository.getMenuForClinicalWorkflows())
        }
    }

    fun getNeonatePatients(id: String, assessmentType: String? = null, origin: String? = null) {
        viewModelScope.launch(dispatcherIO) {
            patientDetailsNeonateLiveData.postLoading()
            patientDetailsNeonateLiveData.postValue(patientRepository.getPatients(PatientDetailRequest(patientId = id, assessmentType = assessmentType, id = id, type = origin)))
        }
    }

    fun getTbMedicalReviewStatus(): Boolean {
        return patientDetailsLiveData.value?.data?.tbIMRCompleted ?: false
    }

    fun getHivMedicalReviewStatus(): Boolean {
        return patientDetailsLiveData.value?.data?.hivIMRCompleted ?: false
    }
    fun getDob(): String? {
        return patientDetailsLiveData.value?.data?.birthDate
    }

    fun getPregnancyBreastFeedStatus(): String? {
        return patientDetailsLiveData.value?.data?.pregnancyBreastFeedStatus
    }
}