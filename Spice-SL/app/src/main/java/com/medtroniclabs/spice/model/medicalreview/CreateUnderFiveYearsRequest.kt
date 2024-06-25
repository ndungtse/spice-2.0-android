package com.medtroniclabs.spice.model.medicalreview

import ClinicalSummaryAndSigns
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.model.PatientListRespModel

data class CreateUnderFiveYearsRequest(
    val clinicalNotes: String? = null,
    val clinicalSummaryAndSigns: ClinicalSummaryAndSigns? = null,
    val examination: UnderFiveExamination? = null,
    val presentingComplaints: String? = null,
    val encounter: UnderFiveYearsDTO? = null,
    var systemicExamination: List<String?>? = null,
    var systemicExaminationNotes: String? = null
)

data class UnderFiveYearsDTO(
    val householdId: String? = null,
    val memberId: String? = null,
    val referred: Boolean? = null,
    val patientId: String? = null,
    val provenance: ProvanceDto? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val visitNumber: Long? = null,
    val startTime: String? = null,
    val endTime: String? = null,
)

data class UnderFiveExamination(
    val generalDangerSigns: GeneralDangerSings? = null,
    val diarrhoea: UnderFiveDiarrhoea? = null,
    val cough: CoughOrDifficultBreathing? = null,
    val fever: Fever? = null,
    val earProblem: EarProblem? = null,
    val anaemia: MalnutritionOrAnaemia? = null,
    val hivRDT: HivAndAids? = null
)

data class UnderFiveDiarrhoea(
    val hasDiarrhoea: Boolean? = null,
    val bloodyDiarrhoea: Boolean? = null,
    val sings: List<String?>? = null,
    val timePeriod: String? = null
)

data class GeneralDangerSings(
    val unableToDrinkOrBreastfeed: Boolean? = null,
    val historyOfConvulsion: Boolean? = null,
    val lethargicOrUnconscious: Boolean? = null,
    val vomitingEverything: Boolean? = null,
    val convulsingNow: Boolean? = null
)

data class CoughOrDifficultBreathing(
    val coughOrDIfficultBreathing: Boolean? = null,
    val chestIndrawing: Boolean? = null,
    val stridor: Boolean? = null,
    val noOfDays: String? = null
)

data class Fever(
    val hasFever: Boolean? = null,
    val isMotherHasFever: String? = null,
    val signs: List<String>? = null,
    var microscopyResult: String? = null,
    val noOfDays: String? = null
)

data class EarProblem(
    val noOfDays: String? = null,
    val hasEarPain: Boolean? = null,
    val earDischarge: String? = null
)

data class MalnutritionOrAnaemia(
    val appetiteTest: Boolean? = null,
    val sings: List<String?>? = null
)

data class HivAndAids(
    val mother: String? = null,
    val child: String? = null
)