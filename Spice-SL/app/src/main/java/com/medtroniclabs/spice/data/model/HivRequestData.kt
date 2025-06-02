package com.medtroniclabs.spice.data.model

data class HivRequestData(
    val clinicalStage: String? = null,
    val cd4: String? = null,
    val weight: Double? = null,
    val height: Double? = null,
    val artCode: String? = null,
    val hivStatus: HivStatus? = null,
    val presentingComplaints: List<String?>? = emptyList(),
    val presentingComplaintsNotes: String? = null,
    val comorbiditiesCoinfectionsNotes: String? = null,
    val systemicExaminations: Map<String, String?> = emptyMap(),
    val comorbiditiesCoinfections: List<String?>? = emptyList(),
    val opportunisticInfections: Map<String, HashMap<String, String>>? = null,
    val encounter: MedicalReviewEncounter? = null,
    val medicalReviewType: String? = null,
    val clinicalNotes: String? = null,
    val id: String? = null,
    val emtctVisitStatus:String? = null,
    val obstetricExaminations: List<String?>? = null,
    val obstetricExaminationNotes: String? = null,
    val fundalHeight : String? = null,
    val fetalHeartRate : String? = null,
)

data class HivStatus(
    val pregnancyBreastfeedStatus: String? = null,
    val ahdStatus: String? = null,
    val dsdStatus: String? = null,
    val model: String? = null,
    val lastMenstrualPeriod: String? = null,
    val gestationalInWeeks: Long? = null,
    val expectedDateOfDelivery: String? = null,
    val tbStatus : String? = null
)

data class OpportunisticInfectionsDTO(
    val tbPreventiveDateRange: DateRange? = null,
    val cotrimoxazoleDateRange: DateRange? = null,
    val tbTreatmentDateRange: DateRange? = null,
    val cryptococcalMeningitisDateRange: DateRange? = null
)

data class DateRange(
    val startDate: String? = null,
    val endDate: String? = null
)
