package com.medtroniclabs.spice.ui.mypatients.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.PatientDetailRequest
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.mypatients.repo.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatientDetailViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
): ViewModel() {

    var dateOfDelivery: String? =null
    var childPatientDetails: String? = null
    val patientDetailsLiveData = MutableLiveData<Resource<PatientListRespModel>>()
    //the below id is one which get from patient details response
    var patientDetailsId : String? = null
    var isSummary : Boolean = false

    var encounterId: String ?= null
    var childEncounterId: String ?= null

    var origin: String? = null

    fun getPatients(id: String, assessmentType: String? = null, origin: String? = null) {
        viewModelScope.launch(dispatcherIO) {
            patientDetailsLiveData.postLoading()
            patientDetailsLiveData.postValue(patientRepository.getPatients(PatientDetailRequest(patientId = id, assessmentType = assessmentType, id = id, type = origin)))
        }
    }

    fun getPatientFHIRId(): String? {
        return patientDetailsLiveData.value?.data?.id
    }

    fun getPatientMemberId(): String? {
        return patientDetailsLiveData.value?.data?.memberId
    }

    fun getPatientId(): String? {
        return patientDetailsLiveData.value?.data?.patientId
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
}