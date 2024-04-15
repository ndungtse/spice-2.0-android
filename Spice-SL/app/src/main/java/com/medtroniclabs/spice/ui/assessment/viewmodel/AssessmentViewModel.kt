package com.medtroniclabs.spice.ui.assessment.viewmodel

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.data.LocalSpinnerResponse
import com.medtroniclabs.spice.db.entity.AssessmentEntity
import com.medtroniclabs.spice.db.entity.HealthFacilityEntity
import com.medtroniclabs.spice.db.entity.MemberClinicalEntity
import com.medtroniclabs.spice.db.entity.SignsAndSymptomsEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.model.FormResponse
import com.medtroniclabs.spice.model.assessment.AssessmentMemberDetails
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.AssessmentRepository
import com.medtroniclabs.spice.repo.HouseholdMemberRepository
import com.medtroniclabs.spice.ui.MenuConstants.ICCM_MENU_ID
import com.medtroniclabs.spice.ui.MenuConstants.OTHER_SYMPTOMS
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.IsClinicTaken
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.otherSymptoms
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.signsAndSymptoms
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.symptoms
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.Diarrhoea
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.DiarrhoeaSigns
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.ANC_MENU
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.ChildHoodVisit
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.ancSigns
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.childhoodVisitSigns
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.otherAncSigns
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.otherChildhoodVisitSigns
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.otherSigns
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.pncChildSigns
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssessmentViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var memberRegistrationRepository: HouseholdMemberRepository,
    private var assessmentRepository: AssessmentRepository
) : ViewModel() {

    var selectedHouseholdMemberId = -1L
    val assessmentSaveLiveData = MutableLiveData<Resource<AssessmentEntity>>()
    val assessmentStringLiveData = MutableLiveData<String?>()
    val assessmentUpdateLiveData = MutableLiveData<Resource<String>>()
    val memberDetailsLiveData = MutableLiveData<Resource<AssessmentMemberDetails>>()
    var menuId: String? = null
    var workflowName: String? = null
    var formLayout: List<FormLayout>? = null
    var symptomTypeListResponse = MutableLiveData<List<SignsAndSymptomsEntity>>()
    var otherAssessmentDetails = HashMap<String, Any>()
    val formLayoutsLiveData = MutableLiveData<Resource<FormResponse>>()
    val nearestFacilityLiveData = MutableLiveData<Resource<List<HealthFacilityEntity>>>()
    var referralStatus: String? = null
    private var lastLocation: Location? = null
    val facilitySpinnerLiveData = MutableLiveData<Resource<LocalSpinnerResponse>>()
    val memberClinicalLiveData = MutableLiveData<MemberClinicalEntity?>()
    var pncMotherDetailMap: HashMap<String, Any>? = null

    fun getMemberDetailsById() {
        if (selectedHouseholdMemberId == -1L) {
            return
        }
        viewModelScope.launch(dispatcherIO) {
            memberRegistrationRepository.getAssessmentMemberDetails(
                selectedHouseholdMemberId,
                memberDetailsLiveData
            )
        }
    }

    fun saveAssessment(
        assessmentMap: HashMap<*, *>,
        referralResult: Pair<String?, ArrayList<String>>?,
        menuId: String?
    ) {
        viewModelScope.launch(dispatcherIO) {
            memberDetailsLiveData.value?.data?.let { details ->
                referralStatus = referralResult?.first
                val assessmentDetail =
                    getAssessmentDetails(assessmentMap as HashMap<Any, Any>)
                assessmentStringLiveData.postValue(assessmentDetail.first)
                assessmentRepository.saveAssessment(
                    assessmentDetail.second,
                    details,
                    assessmentSaveLiveData,
                    menuId,
                    referralResult,
                    lastLocation
                )
            }
        }
    }

    private fun getAssessmentDetails(
        map: HashMap<Any, Any>
    ): Pair<String, String> {
        val assessmentDetail = StringConverter.convertGivenMapToString(map) ?: ""

        // Request modification for syncing ICCM to Backend
        if (map.containsKey(ICCM_MENU_ID)) {
            val iccm = map[ICCM_MENU_ID] as HashMap<*, *>
            if (iccm.containsKey(Diarrhoea)) {
                val diarrhoea = iccm[Diarrhoea] as HashMap<Any, Any>
                if (diarrhoea.containsKey(DiarrhoeaSigns)) {
                    val signsList = mutableListOf<String>()
                    val list = diarrhoea[DiarrhoeaSigns] as List<*>
                    list.forEach { it ->
                        if (it is HashMap<*, *>) {
                            signsList.add(it["name"] as String)
                        }
                    }
                    diarrhoea[DiarrhoeaSigns] = signsList
                }
            }
        }

        // Request modification for syncing Other Symptoms to Backend
        if (map.containsKey(OTHER_SYMPTOMS)) {
            val otherSymptom = map[OTHER_SYMPTOMS] as HashMap<Any, Any>
            if (otherSymptom.containsKey(signsAndSymptoms)) {
                val signsAndSymptom = otherSymptom[signsAndSymptoms] as HashMap<Any, Any>
                if (signsAndSymptom.containsKey(otherSymptoms)) {
                    val signsList = mutableListOf<String>()
                    val list = signsAndSymptom[otherSymptoms] as List<*>
                    list.forEach { it ->
                        if (it is HashMap<*, *>) {
                            signsList.add(it["name"] as String)
                        }
                    }

                    signsAndSymptom.remove(otherSymptoms)
                    signsAndSymptom[symptoms] = signsList
                }
            }
            map.remove(OTHER_SYMPTOMS)
            map[otherSymptoms] = otherSymptom
        }

        // Request modification for syncing RMNCH Childhood Visit to Backend
        if (map.containsKey(ChildHoodVisit)) {
            val childHoodVisit = map[ChildHoodVisit] as HashMap<Any, Any>
            if (childHoodVisit.containsKey(childhoodVisitSigns)) {
                val signsList = mutableListOf<String>()
                val list = childHoodVisit[childhoodVisitSigns] as List<*>
                list.forEach { it ->
                    if (it is HashMap<*, *>) {
                        signsList.add(it["name"] as String)
                    }
                }

                childHoodVisit.remove(childhoodVisitSigns)
                childHoodVisit[pncChildSigns] = signsList
            }

            if (childHoodVisit.containsKey(otherChildhoodVisitSigns)) {
                val os = childHoodVisit[otherChildhoodVisitSigns] as Any
                childHoodVisit.remove(otherChildhoodVisitSigns)
                childHoodVisit[otherSigns] = os
            }
        }

        // Request modification for syncing RMNCH ANC Visit to Backend
        if (map.containsKey(ANC_MENU)) {
            val anc = map[ANC_MENU] as HashMap<Any, Any>
            if (anc.containsKey(ancSigns)) {
                val signsList = mutableListOf<String>()
                val list = anc[ancSigns] as List<*>
                list.forEach { it ->
                    if (it is HashMap<*, *>) {
                        signsList.add(it["name"] as String)
                    }
                }
                anc[ancSigns] = signsList
            }

            if (anc.containsKey(otherAncSigns)) {
                val os = anc[otherAncSigns] as Any
                anc.remove(otherAncSigns)
                anc[otherSigns] = os
            }
        }

        val assessmentDetailBE = StringConverter.convertGivenMapToString(map) ?: ""
        return Pair(assessmentDetail, assessmentDetailBE)
    }

    fun updateOtherAssessmentDetails() {
        viewModelScope.launch(dispatcherIO) {
            if (otherAssessmentDetails.containsKey(IsClinicTaken)) {
                val isTakenToClinical = otherAssessmentDetails[IsClinicTaken] as String
                otherAssessmentDetails[IsClinicTaken] = (isTakenToClinical == "Yes")
            }

            assessmentRepository.updateOtherAssessmentDetails(
                assessmentSaveLiveData.value?.data,
                otherAssessmentDetails,
                assessmentUpdateLiveData,
                lastLocation
            )
        }
    }

    fun addOtherDetailsToType(key: String) {
        val otherDetailsMap = HashMap<String, Any>()
        otherDetailsMap[key] = otherAssessmentDetails
        otherAssessmentDetails = otherDetailsMap
    }

    fun getSymptomListByType(type: String) {
        viewModelScope.launch(dispatcherIO) {
            assessmentRepository.getSymptomListByType(type, symptomTypeListResponse)
        }
    }

    fun getFormData(formType: String) {
        viewModelScope.launch(dispatcherIO) {
            assessmentRepository.getFormData(formType, formLayoutsLiveData)
        }
    }

    fun getNearestHealthFacility() {
        viewModelScope.launch(dispatcherIO) {
            assessmentRepository.getNearestHealthFacility(nearestFacilityLiveData)
        }
    }

    fun setCurrentLocation(location: Location) {
        this.lastLocation = location
    }

    fun getCurrentLocation(): Location? {
        return this.lastLocation
    }

    fun loadDataCacheByType(type: String, tag: String) {
        viewModelScope.launch(dispatcherIO) {
            when (type) {
                RMNCH.PlaceOfDelivery -> {
                    assessmentRepository.getNearestHealthFacility(facilitySpinnerLiveData, tag)
                }
            }
        }
    }

    fun getPatientVisitCountByType(type: String, patientId: String) {
        viewModelScope.launch(dispatcherIO) {
            memberClinicalLiveData.postValue(
                memberRegistrationRepository.getPatientVisitCountByType(
                    type,
                    patientId
                )
            )
        }
    }

    private fun savePatientVisitCountByType(memberClinicalEntity: MemberClinicalEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            memberRegistrationRepository.savePatientVisitCountByType(memberClinicalEntity)
        }
    }


    fun handlePregnancy(
        details: HashMap<String, Any>,
        workflowName: String,
        memberDetail: AssessmentMemberDetails,
        memberClinicalEntity: MemberClinicalEntity?
    ) {
        memberDetail.apply {
            if (details.containsKey(workflowName) && details[workflowName] is Map<*, *>) {
                val map = details[workflowName] as HashMap<String, Any>
                memberClinicalEntity?.let { memberClinicalEntity ->
                    map[RMNCH.visitNo] = memberClinicalEntity.visitCount + 1
                    getClinicalDateKey()?.let {
                        map[it] = memberClinicalEntity.clinicalDate
                    }
                    map[RMNCH.NoOfNeonate] = memberClinicalEntity.numberOfNeonate ?: 0L
                    savePatientClinicalInformation(patientId, workflowName, map, memberClinicalEntity.id)
                } ?: kotlin.run {
                    map[RMNCH.visitNo] = 1L
                    savePatientClinicalInformation(patientId, workflowName, map)
                }
            }
        }
    }

    private fun getClinicalDateKey(): String? {
        when (workflowName) {
            RMNCH.ANC -> {
                return RMNCH.lastMenstrualPeriod
            }

            RMNCH.PNC -> {
                return RMNCH.DateOfDelivery
            }
        }
        return null
    }

    private fun savePatientClinicalInformation(
        patientId: String?,
        workflowName: String,
        map: HashMap<String, Any>,
        rowId: Long = 0
    ) {
        patientId?.let { id ->
            getClinicalDateAndVisitCount(map, workflowName).let {
                val clinicalEntity = MemberClinicalEntity(
                    id = rowId,
                    patientId = id,
                    type = workflowName,
                    visitCount = it.first,
                    clinicalDate = it.second ?: "",
                    numberOfNeonate = it.third
                )
                savePatientVisitCountByType(clinicalEntity)
            }
        }
    }

    private fun getClinicalDateAndVisitCount(
        details: HashMap<String, Any>,
        workflowName: String
    ): Triple<Long, String?, Long?> {
        return when (workflowName) {
            RMNCH.ANC -> {
                Triple(
                    details[RMNCH.visitNo] as Long,
                    details[RMNCH.lastMenstrualPeriod] as String,
                    null
                )
            }

            RMNCH.PNC -> {
                Triple(
                    details[RMNCH.visitNo] as Long,
                    details[RMNCH.DateOfDelivery] as String,
                    getNumberOfNeonates(details)
                )
            }

            else -> {
                Triple(details[RMNCH.visitNo] as Long, null, null)
            }
        }
    }

    private fun getNumberOfNeonates(details: HashMap<String, Any>): Long? {
        val value = details[RMNCH.NoOfNeonate]
        return value.toString().toLongOrNull()
    }

}