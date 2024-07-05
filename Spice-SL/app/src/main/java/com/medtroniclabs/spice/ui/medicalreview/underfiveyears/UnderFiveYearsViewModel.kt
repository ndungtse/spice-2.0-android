package com.medtroniclabs.spice.ui.medicalreview.underfiveyears

import ClinicalSummaryAndSigns
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.SummarySubmitRequest
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.mappingkey.UnderFIveYearExaminationKeyMapping
import com.medtroniclabs.spice.mappingkey.UnderFIveYearExaminationKeyMapping.Diarrhoea.bloodyDiarrhoea
import com.medtroniclabs.spice.mappingkey.UnderFIveYearExaminationKeyMapping.Diarrhoea.sings
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.model.medicalreview.CoughOrDifficultBreathing
import com.medtroniclabs.spice.model.medicalreview.CreateUnderFiveYearsRequest
import com.medtroniclabs.spice.model.medicalreview.CreateUnderTwoMonthsResponse
import com.medtroniclabs.spice.model.medicalreview.EarProblem
import com.medtroniclabs.spice.model.medicalreview.Fever
import com.medtroniclabs.spice.model.medicalreview.GeneralDangerSings
import com.medtroniclabs.spice.model.medicalreview.HivAndAids
import com.medtroniclabs.spice.model.medicalreview.MalnutritionOrAnaemia
import com.medtroniclabs.spice.model.medicalreview.UnderFiveDiarrhoea
import com.medtroniclabs.spice.model.medicalreview.UnderFiveExamination
import com.medtroniclabs.spice.model.medicalreview.UnderFiveYearsDTO
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.UnderFiveYearsRepository
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UnderFiveYearsViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var underFiveYearsRepository: UnderFiveYearsRepository
) : ViewModel() {

    val createUnderFiveMedicalReviewLiveData =
        MutableLiveData<Resource<CreateUnderTwoMonthsResponse>>()
    val createUnderFiveYearMedicalReview: LiveData<Resource<CreateUnderTwoMonthsResponse>>
        get() = createUnderFiveMedicalReviewLiveData
    private var lastLocation: Location? = null
    val summaryCreateResponse = MutableLiveData<Resource<HashMap<String, Any>>>()

    val underFiveYearsMetaLiveData = MutableLiveData<Resource<Boolean>>()
    var patientId: String? = null
    var memberId: String? = null
    var isSwipeRefresh: Boolean = false
    var encounterId: String? = null
    var patientReference:String? = null

    fun getStaticMetaData() {
        viewModelScope.launch(dispatcherIO) {
            underFiveYearsMetaLiveData.postLoading()
            underFiveYearsMetaLiveData.postValue(
                underFiveYearsRepository.getStaticMetaData(
                    MedicalReviewTypeEnums.UnderFiveYears.name
                )
            )
        }
    }

    fun createMedicalReviewForUnderFiveYears(
        patientDetail: PatientListRespModel,
        clinicalSummaryAndSigns: ClinicalSummaryAndSigns,
        examinationResultHashMap: HashMap<String, Any>,
        clinicalNotes: String,
        presentingComplaints: String,
        systemicExaminations: List<String?>?,
        systemicExaminationsNotes: String?
    ) {
        patientDetail.patientId?.let { id ->
            lastLocation.let { location ->
                patientDetail.houseHoldId?.let { hhId ->
                    patientDetail.memberId?.let { memberId ->
                        patientId?.let { selectedPatientId ->
                            viewModelScope.launch(dispatcherIO) {
                                val examination = getUnderFiveExamination(examinationResultHashMap)
                                val underFiveMedicalReviewRequest = CreateUnderFiveYearsRequest(
                                    clinicalNotes = clinicalNotes,
                                    clinicalSummaryAndSigns = if (clinicalSummaryAndSigns.isNotEmpty()) clinicalSummaryAndSigns else null,
                                    examination = examination,
                                    presentingComplaints = presentingComplaints.takeIf { it.isNotEmpty() },
                                    encounter = UnderFiveYearsDTO(
                                        startTime = DateUtils.getCurrentDateAndTime(
                                            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                                        ),
                                        endTime = DateUtils.getCurrentDateAndTime(
                                            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                                        ),
                                        latitude = location?.latitude,
                                        longitude = location?.longitude,
                                        householdId = hhId,
                                        patientId = selectedPatientId,
                                        memberId = memberId,
                                        referred = true,
                                        provenance = ProvanceDto(
                                            createdDateTime = DateUtils.getCurrentDateAndTime(
                                                DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                                            )
                                        )
                                    ),
                                    systemicExaminationNotes = systemicExaminationsNotes,
                                    systemicExamination = systemicExaminations
                                )

                                createUnderFiveMedicalReviewLiveData.postLoading()
                                createUnderFiveMedicalReviewLiveData.postValue(
                                    underFiveYearsRepository.createMedicalReviewForUnderFiveYears(
                                        underFiveMedicalReviewRequest
                                    )
                                )
                            }
                        }
                    }
                }
            }

        }
    }

    private fun getUnderFiveExamination(examinationResultHashMap: HashMap<String, Any>): UnderFiveExamination? {
        val diarrhoea = mapDiarrhoea(examinationResultHashMap)
        val malnutritionOrAnaemia = mapMalnutritionOrAnaemia(examinationResultHashMap)
        val coughOrDifficultBreathing = mapCoughOrDifficultBreathing(examinationResultHashMap)
        val fever = mapFever(examinationResultHashMap)
        val earProblem = mapEarProblem(examinationResultHashMap)
        val hiv = mapHivAndAids(examinationResultHashMap)
        val generalDangerSings = mapGeneralDangerSings(examinationResultHashMap)
        if (diarrhoea == null && fever == null && generalDangerSings == null && malnutritionOrAnaemia == null && coughOrDifficultBreathing == null && earProblem == null && hiv == null) {
            return null
        }
        return UnderFiveExamination(
            diarrhoea = diarrhoea,
            hivRDT = hiv,
            anaemia = malnutritionOrAnaemia,
            cough = coughOrDifficultBreathing,
            fever = fever,
            earProblem = earProblem,
            generalDangerSigns = generalDangerSings
        )
    }


    private fun mapDiarrhoea(examinationResultHashMap: HashMap<String, Any>): UnderFiveDiarrhoea? {
        if (examinationResultHashMap.containsKey(UnderFIveYearExaminationKeyMapping.DiseaseName.diarrhoea)) {
            val diarrhoeaHashMap =
                examinationResultHashMap[UnderFIveYearExaminationKeyMapping.DiseaseName.diarrhoea] as HashMap<String, Any>

            if (diarrhoeaHashMap.isNotEmpty()) {
                var diarrhoea = UnderFiveDiarrhoea()
                if (diarrhoeaHashMap.containsKey(UnderFIveYearExaminationKeyMapping.Diarrhoea.hasDiarrhoea)) {
                    diarrhoea = diarrhoea.copy(
                        hasDiarrhoea = mapStringToBoolean(diarrhoeaHashMap[UnderFIveYearExaminationKeyMapping.Diarrhoea.hasDiarrhoea] as String)
                    )
                }
                if (diarrhoeaHashMap.containsKey(bloodyDiarrhoea)) {
                    diarrhoea = diarrhoea.copy(
                        bloodyDiarrhoea = mapStringToBoolean(diarrhoeaHashMap[bloodyDiarrhoea] as String)
                    )

                }
                if (diarrhoeaHashMap.containsKey(sings)) {
                    diarrhoea = diarrhoea.copy(
                        sings = diarrhoeaHashMap[sings] as List<String?>?
                    )
                }
                if (diarrhoeaHashMap.containsKey(UnderFIveYearExaminationKeyMapping.Diarrhoea.timePeriod)) {
                    diarrhoea = diarrhoea.copy(
                        timePeriod = diarrhoeaHashMap[UnderFIveYearExaminationKeyMapping.Diarrhoea.timePeriod] as String
                    )
                }
                return diarrhoea

            }
        }

        return null
    }


    private fun mapHivAndAids(examinationResultHashMap: HashMap<String, Any>): HivAndAids? {

        if (examinationResultHashMap.containsKey(UnderFIveYearExaminationKeyMapping.DiseaseName.hivRDT)) {
            val hivAndAidsHashMap =
                examinationResultHashMap[UnderFIveYearExaminationKeyMapping.DiseaseName.hivRDT] as HashMap<String, Any>

            if (hivAndAidsHashMap.isNotEmpty()) {
                var hivAndAids = HivAndAids()
                if (hivAndAidsHashMap.containsKey(UnderFIveYearExaminationKeyMapping.HivAndAids.mother)) {
                    hivAndAids = hivAndAids.copy(
                        mother = hivAndAidsHashMap[UnderFIveYearExaminationKeyMapping.HivAndAids.mother] as String
                    )

                }
                if (hivAndAidsHashMap.containsKey(UnderFIveYearExaminationKeyMapping.HivAndAids.child)) {
                    hivAndAids = hivAndAids.copy(
                        child = hivAndAidsHashMap[UnderFIveYearExaminationKeyMapping.HivAndAids.child] as String
                    )
                }

                return hivAndAids
            }
        }
        return null
    }

    private fun mapMalnutritionOrAnaemia(examinationResultHashMap: HashMap<String, Any>): MalnutritionOrAnaemia? {
        if (examinationResultHashMap.containsKey(UnderFIveYearExaminationKeyMapping.DiseaseName.anaemia)) {
            val malnutritionOrAnaemiaHashMap =
                examinationResultHashMap[UnderFIveYearExaminationKeyMapping.DiseaseName.anaemia] as HashMap<String, Any>
            if (malnutritionOrAnaemiaHashMap.isNotEmpty()) {
                var malnutritionOrAnaemia = MalnutritionOrAnaemia()
                if (malnutritionOrAnaemiaHashMap.containsKey(UnderFIveYearExaminationKeyMapping.MalnutritionAndAnaemia.appetiteTest)) {
                    malnutritionOrAnaemia = malnutritionOrAnaemia.copy(
                        appetiteTest = mapStringToBoolean(malnutritionOrAnaemiaHashMap[UnderFIveYearExaminationKeyMapping.MalnutritionAndAnaemia.appetiteTest] as String)
                    )
                }
                if (malnutritionOrAnaemiaHashMap.containsKey(UnderFIveYearExaminationKeyMapping.MalnutritionAndAnaemia.signs)) {
                    malnutritionOrAnaemia = malnutritionOrAnaemia.copy(
                        sings = malnutritionOrAnaemiaHashMap[UnderFIveYearExaminationKeyMapping.MalnutritionAndAnaemia.signs] as List<String?>?
                    )
                }
                return malnutritionOrAnaemia
            }
        }
        return null
    }

    private fun mapCoughOrDifficultBreathing(examinationResultHashMap: HashMap<String, Any>): CoughOrDifficultBreathing? {

        if (examinationResultHashMap.containsKey(UnderFIveYearExaminationKeyMapping.DiseaseName.cough)) {
            val coughOrDifficultBreathingHashMap =
                examinationResultHashMap[UnderFIveYearExaminationKeyMapping.DiseaseName.cough] as HashMap<String, Any>
            if (coughOrDifficultBreathingHashMap.isNotEmpty()) {
                var coughOrDifficultBreathing = CoughOrDifficultBreathing()
                if (coughOrDifficultBreathingHashMap.containsKey(UnderFIveYearExaminationKeyMapping.Cough.coughOrDifficultBreathing)) {
                    coughOrDifficultBreathing = coughOrDifficultBreathing.copy(
                        coughOrDIfficultBreathing = mapStringToBoolean(
                            coughOrDifficultBreathingHashMap[UnderFIveYearExaminationKeyMapping.Cough.coughOrDifficultBreathing] as String
                        )
                    )
                }
                if (coughOrDifficultBreathingHashMap.containsKey(UnderFIveYearExaminationKeyMapping.Cough.chestIndrawing)) {
                    coughOrDifficultBreathing = coughOrDifficultBreathing.copy(
                        chestIndrawing = mapStringToBoolean(coughOrDifficultBreathingHashMap[UnderFIveYearExaminationKeyMapping.Cough.chestIndrawing] as String)
                    )
                }
                if (coughOrDifficultBreathingHashMap.containsKey(UnderFIveYearExaminationKeyMapping.Cough.stridor)) {
                    coughOrDifficultBreathing = coughOrDifficultBreathing.copy(
                        stridor = mapStringToBoolean(coughOrDifficultBreathingHashMap[UnderFIveYearExaminationKeyMapping.Cough.stridor] as String)
                    )
                }
                if (coughOrDifficultBreathingHashMap.containsKey(UnderFIveYearExaminationKeyMapping.Cough.noOfDays)) {
                    coughOrDifficultBreathing = coughOrDifficultBreathing.copy(
                        noOfDays = coughOrDifficultBreathingHashMap[UnderFIveYearExaminationKeyMapping.Cough.noOfDays] as String
                    )
                }

                return coughOrDifficultBreathing
            }
        }
        return null
    }

    private fun mapFever(examinationResultHashMap: HashMap<String, Any>): Fever? {

        if (examinationResultHashMap.containsKey(UnderFIveYearExaminationKeyMapping.DiseaseName.fever)) {
            val feverHashMap =
                examinationResultHashMap[UnderFIveYearExaminationKeyMapping.DiseaseName.fever] as HashMap<String, Any>

            if (feverHashMap.isNotEmpty()) {
                var fever = Fever()
                if (feverHashMap.containsKey(UnderFIveYearExaminationKeyMapping.Fever.hasFever)) {
                    fever = fever.copy(
                        hasFever = mapStringToBoolean(feverHashMap[UnderFIveYearExaminationKeyMapping.Fever.hasFever] as String)
                    )
                }

                if (feverHashMap.containsKey(UnderFIveYearExaminationKeyMapping.Fever.isMotherHasFever)) {
                    fever = fever.copy(
                        isMotherHasFever = feverHashMap[UnderFIveYearExaminationKeyMapping.Fever.isMotherHasFever] as String
                    )
                }

                if (feverHashMap.containsKey(UnderFIveYearExaminationKeyMapping.Fever.microscopyResult)) {
                    fever = fever.copy(
                        microscopyResult = feverHashMap[UnderFIveYearExaminationKeyMapping.Fever.microscopyResult] as String
                    )

                }

                if (feverHashMap.containsKey(UnderFIveYearExaminationKeyMapping.Fever.signs)) {
                    fever = fever.copy(
                        signs = feverHashMap[UnderFIveYearExaminationKeyMapping.Fever.signs] as List<String>?
                    )
                }

                if (feverHashMap.containsKey(UnderFIveYearExaminationKeyMapping.Fever.noOfDays)) {
                    fever = fever.copy(
                        noOfDays = feverHashMap[UnderFIveYearExaminationKeyMapping.Fever.noOfDays] as String
                    )
                }
                return fever
            }
        }
        return null
    }

    private fun mapEarProblem(examinationResultHashMap: HashMap<String, Any>): EarProblem? {
        if (examinationResultHashMap.containsKey(UnderFIveYearExaminationKeyMapping.DiseaseName.earProblem)) {
            val earProblemHashMap =
                examinationResultHashMap[UnderFIveYearExaminationKeyMapping.DiseaseName.earProblem] as HashMap<String, Any>
            if (earProblemHashMap.isNotEmpty()) {
                var earProblem = EarProblem()
                if (earProblemHashMap.containsKey(UnderFIveYearExaminationKeyMapping.EarProblem.hasEarPain)) {
                    earProblem = earProblem.copy(
                        hasEarPain = mapStringToBoolean(earProblemHashMap[UnderFIveYearExaminationKeyMapping.EarProblem.hasEarPain] as String)
                    )
                }
                if (earProblemHashMap.containsKey(UnderFIveYearExaminationKeyMapping.EarProblem.earDischarge)) {
                    earProblem = earProblem.copy(
                        earDischarge = earProblemHashMap[UnderFIveYearExaminationKeyMapping.EarProblem.earDischarge] as String
                    )
                }
                if (earProblemHashMap.containsKey(UnderFIveYearExaminationKeyMapping.EarProblem.noOfDays)) {
                    earProblem = earProblem.copy(
                        noOfDays = earProblemHashMap[UnderFIveYearExaminationKeyMapping.EarProblem.noOfDays] as String
                    )
                }
                return earProblem
            }
        }
        return null
    }

    private fun mapGeneralDangerSings(examinationResultHashMap: HashMap<String, Any>): GeneralDangerSings? {
        if (examinationResultHashMap.containsKey(UnderFIveYearExaminationKeyMapping.DiseaseName.generalDangerSigns)) {
            val generalDangerSingsHashMap =
                examinationResultHashMap[UnderFIveYearExaminationKeyMapping.DiseaseName.generalDangerSigns] as HashMap<String, Any>
            if (generalDangerSingsHashMap.isNotEmpty()) {
                var generalDangerSings = GeneralDangerSings()
                if (generalDangerSingsHashMap.containsKey(UnderFIveYearExaminationKeyMapping.GeneralDangerSigns.unableToDrinkOrBreastfeed)) {
                    generalDangerSings = generalDangerSings.copy(
                        unableToDrinkOrBreastfeed = mapStringToBoolean(generalDangerSingsHashMap[UnderFIveYearExaminationKeyMapping.GeneralDangerSigns.unableToDrinkOrBreastfeed] as String)
                    )
                }
                if (generalDangerSingsHashMap.containsKey(UnderFIveYearExaminationKeyMapping.GeneralDangerSigns.historyOfConvulsion)) {
                    generalDangerSings = generalDangerSings.copy(
                        historyOfConvulsion = mapStringToBoolean(generalDangerSingsHashMap[UnderFIveYearExaminationKeyMapping.GeneralDangerSigns.historyOfConvulsion] as String)
                    )
                }
                if (generalDangerSingsHashMap.containsKey(UnderFIveYearExaminationKeyMapping.GeneralDangerSigns.lethargicOrUnconscious)) {
                    generalDangerSings = generalDangerSings.copy(
                        lethargicOrUnconscious = mapStringToBoolean(generalDangerSingsHashMap[UnderFIveYearExaminationKeyMapping.GeneralDangerSigns.lethargicOrUnconscious] as String)
                    )
                }
                if (generalDangerSingsHashMap.containsKey(UnderFIveYearExaminationKeyMapping.GeneralDangerSigns.vomitingEverything)) {
                    generalDangerSings = generalDangerSings.copy(
                        vomitingEverything = mapStringToBoolean(generalDangerSingsHashMap[UnderFIveYearExaminationKeyMapping.GeneralDangerSigns.vomitingEverything] as String)
                    )
                }
                if (generalDangerSingsHashMap.containsKey(UnderFIveYearExaminationKeyMapping.GeneralDangerSigns.convulsingNow)) {
                    generalDangerSings = generalDangerSings.copy(
                        convulsingNow = mapStringToBoolean(generalDangerSingsHashMap[UnderFIveYearExaminationKeyMapping.GeneralDangerSigns.convulsingNow] as String)
                    )
                }
                return generalDangerSings
            }
        }
        return null
    }


    private fun mapStringToBoolean(value: String): Boolean {
        return value == DefinedParams.Yes
    }

    fun underFiveYearsSummaryCreate(
        details: PatientListRespModel,
        submitCreateId: String,
        nextFollowUpDate: String?,
        selectedPatientStatus: String?
    ) {
        details.patientId?.let { patientId ->
            details.memberId?.let { memberId ->
                val summarySubmitRequest = SummarySubmitRequest(
                    patientStatus = selectedPatientStatus,
                    submitCreateId = submitCreateId,
                    memberId = memberId,
                    id = submitCreateId,
                    provenance = ProvanceDto(
                        createdDateTime = DateUtils.getCurrentDateAndTime(
                            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                        )
                    ),
                    patientReference = details.id,
                    nextVisitDate = DateUtils.convertDateTimeToDate(
                        nextFollowUpDate,
                        DateUtils.DATE_ddMMyyyy,
                        DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                        inUTC = true
                    ),

                    )
                viewModelScope.launch(dispatcherIO) {
                    summaryCreateResponse.postLoading()
                    summaryCreateResponse.postValue(
                        underFiveYearsRepository.underFiveYearsSummaryCreate(
                            summarySubmitRequest
                        )
                    )
                }
            }
        }
    }

}