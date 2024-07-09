package com.medtroniclabs.spice.ui.assessment.viewmodel

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.calculateGestationalAge
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.data.LocalSpinnerResponse
import com.medtroniclabs.spice.data.model.RecommendedDosageListModel
import com.medtroniclabs.spice.db.entity.AssessmentEntity
import com.medtroniclabs.spice.db.entity.MemberClinicalEntity
import com.medtroniclabs.spice.db.entity.PregnancyDetail
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
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.IsClinicTaken
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.otherSymptoms
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.signsAndSymptoms
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.symptoms
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.Diarrhoea
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.DiarrhoeaSigns
import com.medtroniclabs.spice.ui.assessment.referrallogic.utils.ReferralReasons
import com.medtroniclabs.spice.ui.assessment.referrallogic.utils.ReferralStatus
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.ANC
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.ANC_MENU
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.ChildHoodVisit
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.Miscarriage
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.ancSigns
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.childhoodVisitSigns
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.estimatedDeliveryDate
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.gestationalAge
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.lastMenstrualPeriod
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.otherAncSigns
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.otherChildhoodVisitSigns
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.otherSigns
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.pncChildSigns
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.Locale
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
    val nearestFacilityLiveData = MutableLiveData<Resource<ArrayList<Map<String, Any>>>>()
    var referralStatus: String? = null
    private var lastLocation: Location? = null
    val facilitySpinnerLiveData = MutableLiveData<Resource<LocalSpinnerResponse>>()
    val memberClinicalLiveData = MutableLiveData<MemberClinicalEntity?>()
    var pncMotherDetailMap: HashMap<String, Any>? = null
    var dosageListModel: ArrayList<RecommendedDosageListModel>? = null
    var instructionId: String? = null
    val treatmentDays = HashMap<String, Int>()
    var referralReason: ArrayList<String>? = null
    var pregnancyDetail: PregnancyDetail? = null

    init {
        SecuredPreference.getFollowUpCriteria()?.let { followUpCriteria ->
            treatmentDays[ReferralReasons.Pneumonia.name] = followUpCriteria.pneumonia
            treatmentDays[ReferralReasons.Diarrhoea.name] = followUpCriteria.diarrhea
            treatmentDays[ReferralReasons.MUAC.name] = followUpCriteria.muac
            treatmentDays[ReferralReasons.Malaria.name] = followUpCriteria.malaria
        }
    }

    fun getMemberDetailsById() {
        if (selectedHouseholdMemberId == -1L) {
            return
        }
        viewModelScope.launch(dispatcherIO) {
            memberDetailsLiveData.postLoading()
            memberDetailsLiveData.postValue(
                memberRegistrationRepository.getAssessmentMemberDetails(
                    selectedHouseholdMemberId
                )
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
                referralReason = referralResult?.second
                val otherDetails = calculateOtherDetails(assessmentMap, referralStatus, menuId)
                assessmentSaveLiveData.postValue(
                    assessmentRepository.saveAssessment(
                        assessmentDetail.second,
                        details,
                        menuId,
                        referralResult,
                        lastLocation,
                        otherDetails
                    )
                )
            }
        }
    }

    private fun calculateOtherDetails(
        assessmentMap: HashMap<Any, Any>,
        referralStatus: String?,
        menuId: String?
    ): HashMap<String, Any>? {
        var otherDetails = HashMap<String, Any>()

        if (menuId == ICCM_MENU_ID){
            otherDetails = otherAssessmentDetails
        }

        if (referralStatus != null && referralStatus == ReferralStatus.Referred.name) {
            otherDetails[AssessmentDefinedParams.ReferredPHUSiteID] =
                SecuredPreference.getString(SecuredPreference.EnvironmentKey.DEFAULT_SITE_ID.name)
                    ?: "-1"
        } else if (referralStatus != null && referralStatus == ReferralStatus.OnTreatment.name) {
            otherDetails[AssessmentDefinedParams.NextFollowupDate] =
                DateUtils.getDateAfterDays(referralReason?.mapNotNull { treatmentDays[it] }
                    ?.minOrNull() ?: 3)
        }

        if (menuId == ANC_MENU.uppercase(Locale.getDefault())) {
            if (assessmentMap.containsKey(ANC)) {
                val ancMap = assessmentMap[ANC] as Map<*, *>
                var miscarriageValue = false
                if (ancMap.containsKey(Miscarriage)) {
                    val miscarriage = ancMap[Miscarriage]
                    if (miscarriage is Boolean && miscarriage) {
                        miscarriageValue = miscarriage
                    }
                }
                if (!miscarriageValue && ancMap.containsKey(lastMenstrualPeriod)) {
                    val lmp = ancMap[lastMenstrualPeriod] as String
                    DateUtils.convertStringToDate(
                        lmp,
                        DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                    )?.let { lmpDate ->
                        RMNCH.calculateNextANCVisitDate(
                            lmpDate
                        )?.let { visitDate ->
                            otherDetails[AssessmentDefinedParams.NextFollowupDate] =
                                DateUtils.convertDateTimeToDate(
                                    DateUtils.getDateStringFromDate(
                                        visitDate, DateUtils.DATE_ddMMyyyy
                                    ),
                                    DateUtils.DATE_ddMMyyyy,
                                    DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                                )
                        }
                    }
                }
            }
        } else if (menuId == RMNCH.CHILD_MENU.uppercase(Locale.getDefault())) {
            memberDetailsLiveData.value?.data?.dateOfBirth?.let {
                DateUtils.calculateAgeInMonths(it)?.let { pair ->
                    if (pair.first <= RMNCH.childHoodVisitMaxMonth) {
                        RMNCH.calculateNextChildHoodVisitDate(
                            age = pair.first,
                            birthDate = pair.second
                        )?.let { visitDate ->
                            otherDetails[AssessmentDefinedParams.NextFollowupDate] =
                                DateUtils.convertDateTimeToDate(
                                    DateUtils.getDateStringFromDate(
                                        visitDate, DateUtils.DATE_ddMMyyyy
                                    ),
                                    DateUtils.DATE_ddMMyyyy,
                                    DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                                )
                        }
                    }
                }
            }
        }
        return if (otherDetails.isEmpty()) null else otherDetails
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

            if (anc.containsKey(lastMenstrualPeriod)) {
                anc[estimatedDeliveryDate] =
                    DateUtils.getEstDeliveryDateFromLmp(anc[lastMenstrualPeriod] as String)
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
            assessmentUpdateLiveData.postValue(
                assessmentRepository.updateOtherAssessmentDetails(
                    assessmentSaveLiveData.value?.data,
                    otherAssessmentDetails,
                    lastLocation
                )
            )
        }
    }


    fun getSymptomListByType(type: String) {
        viewModelScope.launch(dispatcherIO) {
            symptomTypeListResponse.postValue(assessmentRepository.getSymptomListByType(type))
        }
    }

    fun getFormData(formType: String) {
        viewModelScope.launch(dispatcherIO) {
            formLayoutsLiveData.postLoading()
            formLayoutsLiveData.postValue(assessmentRepository.getFormData(formType))
        }
    }

    fun getNearestHealthFacility() {
        viewModelScope.launch(dispatcherIO) {
            nearestFacilityLiveData.postValue(assessmentRepository.getNearestHealthFacility())
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
                    facilitySpinnerLiveData.postLoading()
                    facilitySpinnerLiveData.postValue(
                        assessmentRepository.getNearestHealthFacility(
                            tag
                        )
                    )
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


    fun handlePregnancy(
        details: HashMap<String, Any>,
        workflowName: String,
        memberDetail: AssessmentMemberDetails,
        memberClinicalEntity: MemberClinicalEntity?,
        childDetailsMap: HashMap<String,Any>? = null

    ) {
        memberDetail.apply {
            if (details.containsKey(workflowName) && details[workflowName] is Map<*, *>) {
                val map = details[workflowName] as HashMap<String, Any>
                patientId.let { id ->
                    val pregnancyDetail = pregnancyDetail
                        ?: PregnancyDetail(patientId = id)
                    getClinicalDateAndVisitCount(map, workflowName, pregnancyDetail,childDetailsMap)
                    savePatientClinicalInformation(pregnancyDetail)
                }
                /*memberClinicalEntity?.let { memberClinicalEntity ->
                    *//*map[RMNCH.visitNo] = memberClinicalEntity.visitCount + 1
                    memberClinicalEntity.clinicalDate?.let { date ->
                        getClinicalDateKey()?.let {
                            map[it] = date
                        }
                    }
                    map[RMNCH.NoOfNeonate] = memberClinicalEntity.numberOfNeonate ?: 0L*//*
                    savePatientClinicalInformation(
                        patientId,
                        workflowName,
                        map
                    )
                } ?: kotlin.run {
                    map[RMNCH.visitNo] = 1L
                    savePatientClinicalInformation(patientId, workflowName, map)
                }*/
            }
        }
    }

    private fun getClinicalDateKey(): String? {
        when (workflowName) {
            ANC -> {
                return lastMenstrualPeriod
            }

            RMNCH.PNC -> {
                return RMNCH.DateOfDelivery
            }
        }
        return null
    }

    private fun savePatientClinicalInformation(
        pregnancyDetail: PregnancyDetail,
    ) {
        viewModelScope.launch(dispatcherIO) {
            memberRegistrationRepository.savePregnancyDetail(pregnancyDetail)
        }
    }


    fun getPregnancyDetailInformation() {
        viewModelScope.launch(dispatcherIO) {
            memberDetailsLiveData.value?.data?.let { detail ->
                pregnancyDetail =
                    memberRegistrationRepository.getPregnancyDetailByPatientId(detail.patientId)
            }
        }
    }


    private fun getClinicalDateAndVisitCount(
        details: HashMap<String, Any>,
        workflowName: String,
        pregnancyDetail: PregnancyDetail,
        childDetailsMap: HashMap<String, Any>?
    ) {
        when (workflowName) {
            ANC -> {
                pregnancyDetail.ancVisitNo =
                    getVisitNumber(pregnancyDetail.ancVisitNo)
                pregnancyDetail.lastMenstrualPeriod = getClinicalDate(
                    pregnancyDetail.lastMenstrualPeriod,
                    details[lastMenstrualPeriod]
                )
                details[RMNCH.visitNo] = pregnancyDetail.ancVisitNo ?: 0L
                details[lastMenstrualPeriod] = pregnancyDetail.lastMenstrualPeriod ?: ""
                pregnancyDetail.pncVisitNo = 0
                pregnancyDetail.dateOfDelivery = null
                pregnancyDetail.noOfNeonates = null
            }

            RMNCH.PNC -> {
                pregnancyDetail.pncVisitNo =
                    getVisitNumber(pregnancyDetail.pncVisitNo)
                pregnancyDetail.dateOfDelivery =
                    getClinicalDate(pregnancyDetail.dateOfDelivery, details[RMNCH.DateOfDelivery])
                pregnancyDetail.noOfNeonates =
                    getNumberOfNeonates(pregnancyDetail.noOfNeonates, details[RMNCH.NoOfNeonate])
                details[RMNCH.visitNo] = pregnancyDetail.pncVisitNo ?: 0L
                details[RMNCH.DateOfDelivery] = pregnancyDetail.dateOfDelivery ?: ""
                details[RMNCH.NoOfNeonate] = pregnancyDetail.noOfNeonates ?: 0L
                pregnancyDetail.lastMenstrualPeriod?.let { lmp ->
                    childDetailsMap?.let {
                        childDetailsMap[lastMenstrualPeriod] = lmp
                        val lastMenstrualDate = DateUtils.getLastMenstrualDate(lmp)
                        val gestationWeek = calculateGestationalAge(lastMenstrualDate).first
                        childDetailsMap[gestationalAge] = gestationWeek
                    }
                }
                pregnancyDetail.ancVisitNo = 0
                pregnancyDetail.lastMenstrualPeriod = null
                pregnancyDetail.estimatedDeliveryDate = null
            }

            else -> {
                pregnancyDetail.childVisitNo =
                    getVisitNumber(pregnancyDetail.childVisitNo)
                details[RMNCH.visitNo] = pregnancyDetail.childVisitNo ?: 0L
            }
        }
    }

    private fun getVisitNumber(existingCount: Long?, visitNo: Long = 1): Long {
        existingCount?.let { return (it + 1) } ?: return visitNo.let { it }
    }

    private fun getClinicalDate(existingDate: String?, date: Any?): String? {
        existingDate?.let { return it } ?: return date?.let { it as String }
    }

    private fun getNumberOfNeonates(existingCount: Long?, noOfNeonate: Any?): Long? {
        existingCount?.let { return it }
            ?: return noOfNeonate?.let { (it as String).toLongOrNull() }
    }


    fun updateMemberClinicalData(
        patientId: String,
        visitCount: Long,
        clinicalDate: String?
    ) {
        viewModelScope.launch(dispatcherIO) {
            assessmentRepository.updatePregnancyAncDetail(
                patientId,
                visitCount,
                clinicalDate
            )
        }
    }


}