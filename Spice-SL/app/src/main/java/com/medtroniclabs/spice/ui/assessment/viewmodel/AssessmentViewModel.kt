package com.medtroniclabs.spice.ui.assessment.viewmodel

import android.content.Context
import android.location.Location
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.medtroniclabs.spice.app.analytics.model.UserDetail
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.calculateGestationalAge
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.CBS
import com.medtroniclabs.spice.common.DefinedParams.TB
import com.medtroniclabs.spice.common.DefinedParams.TbScreening
import com.medtroniclabs.spice.common.DefinedParams.notifiableConditions
import com.medtroniclabs.spice.common.DefinedParams.surveillanceDetails
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.SpiceLocationManager
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.data.LocalSpinnerResponse
import com.medtroniclabs.spice.data.UserProfile
import com.medtroniclabs.spice.data.model.RecommendedDosageListModel
import com.medtroniclabs.spice.data.model.SymptomModel
import com.medtroniclabs.spice.db.entity.AssessmentEntity
import com.medtroniclabs.spice.db.entity.HealthFacilityEntity
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.db.entity.MedicalComplianceEntity
import com.medtroniclabs.spice.db.entity.MemberClinicalEntity
import com.medtroniclabs.spice.db.entity.PregnancyDetail
import com.medtroniclabs.spice.db.entity.RiskClassificationModel
import com.medtroniclabs.spice.db.entity.SignsAndSymptomsEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.model.FormResponse
import com.medtroniclabs.spice.mappingkey.Screening
import com.medtroniclabs.spice.model.assessment.AssessmentMemberDetails
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.network.utils.ConnectivityManager
import com.medtroniclabs.spice.repo.AssessmentRepository
import com.medtroniclabs.spice.repo.HouseholdMemberRepository
import com.medtroniclabs.spice.ui.BaseViewModel
import com.medtroniclabs.spice.ui.MenuConstants.ICCM_MENU_ID
import com.medtroniclabs.spice.ui.MenuConstants.OTHER_SYMPTOMS
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.IsClinicTaken
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.otherSymptoms
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.signsAndSymptoms
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.symptoms
import com.medtroniclabs.spice.ui.assessment.AssessmentNCDEntity
import com.medtroniclabs.spice.ui.assessment.referrallogic.ReferralResultGenerator
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.Diarrhoea
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.DiarrhoeaSigns
import com.medtroniclabs.spice.ui.assessment.referrallogic.utils.ReferralReasons
import com.medtroniclabs.spice.ui.assessment.referrallogic.utils.ReferralStatus
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.ANC
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.ANC_MENU
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.ChildHoodVisit
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.DeathOfMother
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.Miscarriage
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.ancSigns
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.childhoodVisitSigns
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.deathOfBaby
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.estimatedDeliveryDate
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.gestationalAge
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.getDeathStatus
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.lastMenstrualPeriod
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.otherAncSigns
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.otherChildhoodVisitSigns
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.otherSigns
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.pncChildSigns
import com.medtroniclabs.spice.ui.boarding.repo.MetaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AssessmentViewModel @Inject constructor(
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
    private var memberRegistrationRepository: HouseholdMemberRepository,
    private var assessmentRepository: AssessmentRepository,
    private val metaRepository: MetaRepository
) : BaseViewModel(dispatcherIO) {

    var selectedHouseholdMemberId = -1L
    var followUpId: Long? = null
    val assessmentSaveLiveData = MutableLiveData<Resource<AssessmentEntity>>()
    val assessmentStringLiveData = MutableLiveData<String?>()
    val assessmentUpdateLiveData = MutableLiveData<Resource<String>>()
    val memberDetailsLiveData = MutableLiveData<Resource<AssessmentMemberDetails>>()
    var menuId: String? = null
    var workflowName: String? = null
    var formLayout: List<FormLayout>? = null
    var symptomTypeListResponse = MutableLiveData<List<SignsAndSymptomsEntity>>()
    var symptomListResponse = MutableLiveData<List<SignsAndSymptomsEntity>>()
    var otherAssessmentDetails = HashMap<String, Any>()
    val formLayoutsLiveData = MutableLiveData<Resource<FormResponse>>()
    val nearestFacilityLiveData = MutableLiveData<Resource<ArrayList<Map<String, Any>>>>()
    var referralStatus: String? = null
    var lastLocation: Location? = null
    val facilitySpinnerLiveData = MutableLiveData<Resource<LocalSpinnerResponse>>()
    val memberClinicalLiveData = MutableLiveData<MemberClinicalEntity?>()
    var pncMotherDetailMap: HashMap<String, Any>? = null
    var dosageListModel: ArrayList<RecommendedDosageListModel>? = null
    var instructionId: String? = null
    val treatmentDays = HashMap<String, Int>()
    var referralReason: ArrayList<String>? = null
    var pregnancyDetail: PregnancyDetail? = null
    var selectedMemberDob: String? = null
    var selectedSymptoms = MutableLiveData<List<SymptomModel>>()
    var medicationParentComplianceResponse = MutableLiveData<List<MedicalComplianceEntity>>()
    var selectedMedication = MutableLiveData<MedicalComplianceEntity?>()
    var medicationChildComplianceResponse = MutableLiveData<List<MedicalComplianceEntity>>()
    var complianceMap: ArrayList<HashMap<String, Any>>? = null
    var bioDataMap: HashMap<String, Any>? = null
    var bioMetric: HashMap<String, Any>? = null
    var list = ArrayList<RiskClassificationModel>()
    private var fbsBloodGlucose: Double? = null
    private var rbsBloodGlucose: Double? = null
    var assessmentType: String? = null
    val assessmentSaveResponse =
        MutableLiveData<Resource<Pair<AssessmentNCDEntity, HashMap<String, Any>?>>>()
    var mentalHealthQuestions = MutableLiveData<Resource<HashMap<String, LocalSpinnerResponse>>>()
    private var phQ4Score: Int? = null
    var isDangerSignFlow: Boolean = false
    var ageInMonth = MutableLiveData<String>()
    var formRenderedLiveData = MutableLiveData<Boolean>()
    var muacColor:String? = null
    var hasDiarrhoea:Boolean = false
    val userProfileLiveData = MutableLiveData<Resource<UserProfile>>()
    val callResultHashMap = HashMap<String, Any>()
    val patientHealthFacility = MutableLiveData<Resource<List<HealthFacilityEntity>>>()
    val callResultSaveLiveData = MutableLiveData<Resource<AssessmentEntity>>()
    val getAssessmentDetails = MutableLiveData<Resource<AssessmentEntity>>()

    val childhoodVisitConditionLiveData = MediatorLiveData<String>().apply {
        addSource(ageInMonth) { age ->
            if (formRenderedLiveData.value == true && age != null) {
                value = age
            }
        }

        addSource(formRenderedLiveData) { rendered ->
            if (rendered == true && ageInMonth.value != null) {
                value = ageInMonth.value
            }
        }
    }

    var dangerSingsKey:String? = null
    @Inject
    lateinit var connectivityManager: ConnectivityManager

    val pncChildMemberDetailsLiveData = MutableLiveData<HouseholdMemberEntity?>()
    val pncAssessmentStringSaveLiveData = MutableLiveData<String?>()
    val pncAssessmentSaveLiveData = MutableLiveData<Resource<Pair<AssessmentEntity, AssessmentEntity?>>>()

    var isAssessmentCancelLiveData=MutableLiveData<Boolean>()

    init {
        SecuredPreference.getFollowUpCriteria()?.let { followUpCriteria ->
            treatmentDays[ReferralReasons.Pneumonia.name] = followUpCriteria.pneumonia
            treatmentDays[ReferralReasons.Diarrhoea.name] = followUpCriteria.diarrhea
            treatmentDays[ReferralReasons.MUAC.name] = followUpCriteria.muac
            treatmentDays[ReferralReasons.Malaria.name] = followUpCriteria.malaria
        }
    }

    var isDeathOfNewborn=false
    var nameOfDangerSignClicked:String? = null



    fun getPNCChildInfoByParentId(parentId: Long) {
        viewModelScope.launch(dispatcherIO) {
            assessmentRepository.getChildPatientId(parentId)?.let { childLocalId ->
                pncChildMemberDetailsLiveData.postValue(
                    memberRegistrationRepository.getMemberDetails(childLocalId)
                )
            }
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
        assessmentMap: HashMap<String, Any>,
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
                        otherDetails,
                        followUpId = followUpId
                    )
                )
            }
        }
    }

    fun saveCallResult(
       assessmentEntity: AssessmentEntity,
       assessmentMap: HashMap<String, Any>? = null
    ) {
        viewModelScope.launch(dispatcherIO) {
            assessmentMap?.let {
                val assessmentDetail =
                    getAssessmentDetails(it as HashMap<Any, Any>)
                assessmentStringLiveData.postValue(assessmentDetail.first)
            }
            callResultSaveLiveData.postValue(assessmentRepository.saveCallResult(assessmentEntity))
        }
    }

    fun getAssessmentDetailsById(assessmentId: Long) {
        viewModelScope.launch(dispatcherIO) {
            getAssessmentDetails.postValue(assessmentRepository.getAssessmentById(assessmentId))
        }
    }

    private fun calculatePNCOtherDetails(
        assessmentMap: HashMap<Any, Any>,
        referralStatus: String?
    ): HashMap<String, Any>? {
        val otherDetails = HashMap<String, Any>()
        if (referralStatus != null && referralStatus == ReferralStatus.Referred.name) {
            otherDetails[AssessmentDefinedParams.ReferredPHUSiteID] =
                SecuredPreference.getString(SecuredPreference.EnvironmentKey.DEFAULT_SITE_ID.name)
                    ?: "-1"
        }
        if (assessmentMap.containsKey(RMNCH.PNC)) {
            val map = assessmentMap[RMNCH.PNC] as Map<*, *>
            if (map.containsKey(RMNCH.DateOfDelivery)) {
                val dateOfDelivery = map[RMNCH.DateOfDelivery] as String
                if (dateOfDelivery != null && dateOfDelivery.isNotEmpty()) {
                    DateUtils.convertStringToDate(
                        dateOfDelivery,
                        DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                    )?.let { deliveryDate ->
                        RMNCH.calculateNextPNCVisitDate(deliveryDate)?.let { visitDate ->
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

    private fun calculateOtherDetails(
        assessmentMap: HashMap<String, Any>,
        referralStatus: String?,
        menuId: String?
    ): HashMap<String, Any>? {
        var otherDetails = HashMap<String, Any>()

        if (menuId == ICCM_MENU_ID) {
            otherDetails = otherAssessmentDetails
        }

        if (referralStatus != null && referralStatus == ReferralStatus.Referred.name) {
            otherDetails[AssessmentDefinedParams.ReferredPHUSiteID] =
                SecuredPreference.getString(SecuredPreference.EnvironmentKey.DEFAULT_SITE_ID.name)
                    ?: "-1"
        } else if (referralStatus != null && referralStatus == ReferralStatus.OnTreatment.name) {
            otherDetails[AssessmentDefinedParams.NextFollowupDate] =
                DateUtils.convertDateTimeToDate(
                    DateUtils.getDateAfterDays(referralReason?.mapNotNull { treatmentDays[it] }
                        ?.minOrNull() ?: 3),
                    DateUtils.DATE_ddMMyyyy,
                    DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                    inUTC = true
                )
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
                val deathOfMother = getDeathStatus(assessmentMap, ANC, DeathOfMother)

                if (!deathOfMother && !miscarriageValue && ancMap.containsKey(lastMenstrualPeriod)) {
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
            val deathOfBaby = getDeathStatus(assessmentMap, ChildHoodVisit, deathOfBaby)

            memberDetailsLiveData.value?.data?.dateOfBirth?.let {
                if (!deathOfBaby) {
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
        }
        return if (otherDetails.isEmpty()) null else otherDetails
    }

    private fun getPNCAssessmentDetails(map: HashMap<Any, Any>): Triple<String, String, String> {
        val assessmentDetail = StringConverter.convertGivenMapToString(map) ?: ""
        var motherMapString: String? = null
        var childMapString: String? = null

        // Request modification for syncing PNC Mother to Backend
        if (map.containsKey(RMNCH.PNC)) {
            val pnc = map[RMNCH.PNC] as HashMap<Any, Any>
            if (pnc.containsKey(RMNCH.pncMotherSigns)) {
                val signsList = mutableListOf<String>()
                val list = pnc[RMNCH.pncMotherSigns] as List<*>
                list.forEach { it ->
                    if (it is HashMap<*, *>) {
                        signsList.add(it[DefinedParams.Value] as String)
                    }
                }
                pnc[RMNCH.pncMotherSigns] = signsList
            }

            if (pnc.containsKey(RMNCH.otherPncMotherSigns)) {
                val os = pnc[RMNCH.otherPncMotherSigns] as Any
                pnc.remove(RMNCH.otherPncMotherSigns)
                pnc[RMNCH.otherSigns] = os
            }

            val parentMap = HashMap<String, Any>()
            parentMap[RMNCH.PNC] = pnc
            motherMapString = StringConverter.convertGivenMapToString(parentMap)
        }

        // Request modification for syncing PNC neonatal to Backend
        if (map.containsKey(RMNCH.PNCNeonatal)) {
            val pncNeonate = map[RMNCH.PNCNeonatal] as HashMap<Any, Any>
            if (pncNeonate.containsKey(RMNCH.pncNeonateSigns)) {
                val signsList = mutableListOf<String>()
                val list = pncNeonate[RMNCH.pncNeonateSigns] as List<*>
                list.forEach { it ->
                    if (it is HashMap<*, *>) {
                        signsList.add(it[DefinedParams.Value] as String)
                    }
                }

                pncNeonate.remove(RMNCH.pncNeonateSigns)
                pncNeonate[RMNCH.pncNeonatalSigns] = signsList
            }

            if (pncNeonate.containsKey(RMNCH.otherPncNeonateSigns)) {
                val os = pncNeonate[RMNCH.otherPncNeonateSigns] as Any
                pncNeonate.remove(RMNCH.otherPncNeonateSigns)
                pncNeonate[RMNCH.otherSigns] = os
            }
            val parentMap = HashMap<String, Any>()
            parentMap[RMNCH.PNCNeonatal] = pncNeonate
            childMapString = StringConverter.convertGivenMapToString(parentMap)
        }

        return Triple(assessmentDetail, motherMapString ?: "", childMapString ?: "")
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
                            signsList.add(it[DefinedParams.Value] as String)
                        }
                    }
                    diarrhoea[notifiableConditions]?.let { cbs ->
                        diarrhoea.remove(notifiableConditions)
                        val cbsData = hashMapOf<String, Any>()
                        cbsData[notifiableConditions] = cbs
                        map[CBS.lowercase()] = cbsData
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
                            signsList.add(it[DefinedParams.Value] as String)
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
                        signsList.add(it[DefinedParams.Value] as String)
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
                        signsList.add(it[DefinedParams.Value] as String)
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

        // Request modification for syncing TB to Backend
        if (map.containsKey(TB.lowercase())) {
            val result = map[TB.lowercase()] as? HashMap<Any, Any>
            if ( result != null && result.containsKey(TbScreening) ) {
                val value = result[TbScreening] as? HashMap<Any, Any>
                if (!value.isNullOrEmpty()) {
                    map.remove(TB.lowercase())
                    map[TB.lowercase()] = value
                }
            }
        }
        if (map.containsKey(CBS.lowercase())) {
            val result = map[CBS.lowercase()] as? HashMap<Any, Any>
            if (result != null && result.containsKey(surveillanceDetails)) {
                val value = result[surveillanceDetails] as? HashMap<Any, Any>
                if (!value.isNullOrEmpty()) {
                    map.remove(CBS.lowercase())
                    map[CBS.lowercase()] = value
                }
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

    fun getPatientVisitCountByType(type: String, hhmLocalId: Long) {
        viewModelScope.launch(dispatcherIO) {
            memberClinicalLiveData.postValue(
                memberRegistrationRepository.getPatientVisitCountByType(
                    type,
                    hhmLocalId
                )
            )
        }
    }


    fun handlePregnancy(
        details: HashMap<String, Any>,
        workflowName: String,
        memberDetail: AssessmentMemberDetails,
        memberClinicalEntity: MemberClinicalEntity?,
        childDetailsMap: HashMap<String, Any>? = null

    ) {
        memberDetail.apply {
            if (details.containsKey(workflowName) && details[workflowName] is Map<*, *>) {
                val map = details[workflowName] as HashMap<String, Any>
                val pregnancyDetail = pregnancyDetail
                    ?: PregnancyDetail(householdMemberLocalId = id)
                getClinicalDateAndVisitCount(
                    map,
                    workflowName,
                    pregnancyDetail,
                    childDetailsMap
                )
                savePatientClinicalInformation(pregnancyDetail)

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
                    memberRegistrationRepository.getPregnancyDetailByPatientId(detail.id)
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
                pregnancyDetail.neonatePatientId = null
                pregnancyDetail.isDeliveryAtHome = null
                pregnancyDetail.neonateHouseholdMemberLocalId = null
                pregnancyDetail.isNeonateDeathRecordedByPHU = null
            }

            RMNCH.PNC -> {
                val visitNo = getVisitNumber(pregnancyDetail.pncVisitNo)
                pregnancyDetail.pncVisitNo = visitNo
                pregnancyDetail.dateOfDelivery =
                    getClinicalDate(pregnancyDetail.dateOfDelivery, details[RMNCH.DateOfDelivery])
                pregnancyDetail.noOfNeonates =
                    getNumberOfNeonates(pregnancyDetail.noOfNeonates, details[RMNCH.NoOfNeonate])
                pregnancyDetail.isDeliveryAtHome =
                    if (visitNo == 1L) true else pregnancyDetail.isDeliveryAtHome

                details[RMNCH.visitNo] = pregnancyDetail.pncVisitNo ?: 0L
                details[RMNCH.DateOfDelivery] = pregnancyDetail.dateOfDelivery ?: ""
                details[RMNCH.NoOfNeonate] = pregnancyDetail.noOfNeonates ?: 0
                pregnancyDetail.lastMenstrualPeriod?.let { lmp ->
                    childDetailsMap?.let {
                        childDetailsMap[lastMenstrualPeriod] = lmp
                        val lastMenstrualDate = DateUtils.getLastMenstrualDate(lmp)
                        val gestationWeek = calculateGestationalAge(lastMenstrualDate).first
                        childDetailsMap[gestationalAge] = gestationWeek.toInt()
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

    private fun getNumberOfNeonates(existingCount: Int?, noOfNeonate: Any?): Int? {
        existingCount?.let { return it }
            ?: return noOfNeonate?.let { (it as String).toIntOrNull() }
    }


    fun updateMemberClinicalData(
        hhmLocalId: Long,
        visitCount: Long,
        clinicalDate: String?
    ) {
        viewModelScope.launch(dispatcherIO) {
            assessmentRepository.updatePregnancyAncDetail(
                hhmLocalId,
                visitCount,
                clinicalDate
            )
        }
    }

    fun updateMemberDeceasedStatus(id: Long, status: Boolean) {
        viewModelScope.launch(dispatcherIO) {
            memberRegistrationRepository.updateMemberDeceasedStatus(
                id,
                status
            )
        }
    }

    fun getSymptomList() {
        viewModelScope.launch(dispatcherIO) {
            symptomListResponse.postValue(assessmentRepository.getSymptomList())
        }
    }

    fun getMedicationParentComplianceList() {
        viewModelScope.launch(dispatcherIO) {
            medicationParentComplianceResponse.postValue(assessmentRepository.getMedicationParentComplianceList())
        }
    }

    fun getMedicationChildComplianceList(parentId: Long) {
        viewModelScope.launch(dispatcherIO) {
            try {
                medicationChildComplianceResponse.postValue(
                    assessmentRepository.getMedicationChildComplianceList(
                        parentId
                    )
                )
            } catch (_: Exception) {
                //Exception - Catch block
            }
        }
    }

    fun saveAssessmentInformation(
        request: String,
        uploadStatus: Boolean,
        isRecursion: Boolean,
        onlineSaveResponse: HashMap<String, Any>? = null
    ) {
        viewModelScope.launch(dispatcherIO)
        {
            assessmentSaveResponse.postLoading()
            try {
                if (!isRecursion && connectivityManager.isNetworkAvailable()) {
                    val reqMap = StringConverter.convertStringToMap(request)
                    val response = assessmentRepository.createAssessmentNCD(
                        StringConverter.getJsonObject(
                            Gson().toJson(reqMap)
                        )
                    )
                    val success = response.isSuccessful
                    saveAssessmentInformation(
                        request,
                        uploadStatus = success,
                        isRecursion = true,
                        onlineSaveResponse = if (success) response.body() else null
                    )
                } else {
                    val assessmentEntity = AssessmentNCDEntity(
                        assessmentDetails = request,
                        uploadStatus = uploadStatus,
                        userId = SecuredPreference.getUserId()
                    )
                    val rowId = assessmentRepository.saveAssessmentInformation(assessmentEntity)
                    setAnalyticsData(
                        UserDetail.startDateTime,
                        eventName = AnalyticsDefinedParams.NCDAssessmentCreation + " " + menuId,
                        isCompleted = true
                    )
                    assessmentSaveResponse.postSuccess(Pair(rowId, onlineSaveResponse))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                assessmentSaveResponse.postError()
            }
        }
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

    fun fetchMentalHealthQuestions(type: String) {
        viewModelScope.launch(dispatcherIO) {
            var mhResponse = mentalHealthQuestions.value?.data
            mentalHealthQuestions.postLoading()
            try {
                val phq4Questions = assessmentRepository.getMentalQuestion(type = Screening.PHQ4)
                val phq9Questions =
                    assessmentRepository.getMentalQuestion(type = AssessmentDefinedParams.PHQ9)
                val gad7Questions =
                    assessmentRepository.getMentalQuestion(type = AssessmentDefinedParams.GAD7)
                if (mhResponse == null)
                    mhResponse = HashMap()
                mhResponse[Screening.PHQ4] =
                    LocalSpinnerResponse(
                        tag = Screening.MentalHealthDetails,
                        response = phq4Questions
                    )
                mhResponse[AssessmentDefinedParams.PHQ9] =
                    LocalSpinnerResponse(
                        tag = AssessmentDefinedParams.PHQ9_Mental_Health,
                        response = phq9Questions
                    )
                mhResponse[AssessmentDefinedParams.GAD7] =
                    LocalSpinnerResponse(
                        tag = AssessmentDefinedParams.GAD7_Mental_Health,
                        response = gad7Questions
                    )

                mentalHealthQuestions.postValue(Resource(ResourceState.SUCCESS, mhResponse))
            } catch (e: Exception) {
                mentalHealthQuestions.postValue(Resource(ResourceState.ERROR))
            }
        }
    }

    fun setPhQ4Score(phQ4Score: Int) {
        this.phQ4Score = phQ4Score
    }

    fun getPhQ4Score(): Int? {
        return phQ4Score
    }

    fun savePNCDetails(
        motherDetailMap: HashMap<String, Any>,
        memberDetail: AssessmentMemberDetails,
        childMemberId: Long,
        childFhirId: String? = null,
        followUpId: Long? = null,
        deathOfNewborn: Boolean?
    ) {

        viewModelScope.launch(dispatcherIO) {
            //Update Mother member details to NotSynced for PNC Flow
            if(childFhirId == null) {
                memberRegistrationRepository.changeMemberDetailsToNotSynced(memberDetail.id)
            }

            val groupMap = HashMap<String, Any>()
            groupMap[RMNCH.PNC] = motherDetailMap[RMNCH.PNC] as Any

            val motherReferralResult =
                ReferralResultGenerator().calculateRMNCHReferralResult(groupMap, false)
            val childReferralResult =
                ReferralResultGenerator().calculateRMNCHReferralResult(groupMap, true)
            val assessmentDetail = getPNCAssessmentDetails(groupMap as HashMap<Any, Any>)
            referralStatus = motherReferralResult.first
            pncAssessmentStringSaveLiveData.postValue(assessmentDetail.first)
            val otherDetails = calculatePNCOtherDetails(groupMap, referralStatus)
            pncAssessmentSaveLiveData.postValue(
                assessmentRepository.savePNCAssessment(
                    assessmentDetail.second,
                    null,
                    memberDetail,
                    motherReferralResult,
                    getCurrentLocation(),
                    otherDetails,
                    Triple(childMemberId, followUpId,deathOfNewborn),
                    childReferralResult
                )
            )
        }
    }
    fun fetchCurrentLocation(context: Context) {
        val locationManager = SpiceLocationManager(context)
        locationManager.getCurrentLocation {
            setCurrentLocation(it)
        }
    }

    fun getUserProfile() {
        viewModelScope.launch(dispatcherIO) {
            userProfileLiveData.postLoading()
            userProfileLiveData.postValue(metaRepository.getUserProfile())
        }
    }

    fun getHealthFacilityBasedOnVillageId(villageId: Long) {
        viewModelScope.launch(dispatcherIO) {
            patientHealthFacility.postValue(
                assessmentRepository.getHealthFacilityBasedOnVillageId(
                    villageId = villageId
                )
            )
        }
    }
}