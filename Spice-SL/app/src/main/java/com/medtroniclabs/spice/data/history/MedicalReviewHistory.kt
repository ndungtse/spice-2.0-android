package com.medtroniclabs.spice.data.history

import com.medtroniclabs.spice.data.model.Contraceptive
import com.medtroniclabs.spice.model.ReferredDate
import java.io.Serializable

data class MedicalReviewHistory(
    val id: String? = null,
    val patientReference: String? = null,
    val dateOfReview: String? = null,
    val reviewDetails: ReviewDetails? = null,
    val history: List<ReferredDate>? = null,
    val type: String? = null
)

data class NCDMedicalReviewHistory(
    val patientVisitId: String? = null,
    val history: List<ReferredDate>? = null,
    val medicalReview: MedicalReview? = null,
    val dateOfReview: String? = null
)

data class MedicalReview(
    val physicalExams: List<PhysicalExaminations>? = null,
    val complaints: List<String>? = null,
    val notes: List<String>? = null,
    val prescriptions: List<String>? = null,
    val investigations: List<String>? = null
)

data class PhysicalExaminations(
    val physicalExaminations: List<String>? = null,
    val physicalExaminationsNote: String? = null
)

data class ReviewDetails(
    val id: String? = null,
    val visitNumber: Int? = null,
    val patientReference: String? = null,
    val diagnosis: List<DiseaseInfo>? = null,
    val patientStatus: String? = null,
    val isMotherAlive: Boolean? = null,
    val breastCondition: String? = null,
    val breastConditionNotes: String? = null,
    val involutionsOfTheUterus: String? = null,
    val involutionsOfTheUterusNotes: String? = null,
    val presentingComplaints: Any? = null,
    val presentingComplaintsNotes: String? = null,
    val systemicExaminations: Any? = null,
    val systemicExamination: List<String?>? = null,
    val comorbidities: List<String?>? = null,
    val comorbiditiesCoinfections: List<String?>? = null,
    val obstetricExaminations: List<String?>? = null,
    val systemicExaminationsNotes: String? = null,
    val comorbiditiesNotes: String? = null,
    val comorbiditiesCoinfectionsNotes: String? = null,
    val systemicExaminationNotes: String? = null,
    val obstetricExaminationNotes: String? = null,
    val clinicalNotes: String? = null,
    val entryPoint: String? = null,
    val hbsAGTest: String? = null,
    val a1TestResult: String? = null,
    val a2TestResult: String? = null,
    val a3TestResult: String? = null,
    val eligibilities: Eligibility? = null,
    val labourDTO:LabourDTO? = null,
    val neonateOutcome:String?=null,
    val stateOfBaby:String? = null,
    val birthWeight:String? = null,
    val signs:List<String?>? = null,
    val vaccinated: List<String>? = null,
    val lastScheduledDate: String? = null,
    val lastScheduledDateReason: String? = null,
    val nextVaccinationDuration: String? = null,
    val nextVaccinationDose: List<String>? = null,
    val nextVaccinationDate: String? = null,
    val contraceptive: Contraceptive? = null
)
data class Eligibility(
    val hivPopulationType: List<String>? = null,
    val Symptoms: List<String>? = null,
)
data class DiseaseInfo(
    val diseaseCategoryId: Long? = null,
    val diseaseConditionId: Long? = null,
    val diseaseCategory: String? = null,
    val notes: String? = null,
    val diseaseCondition: String? = null,
    val type: String? = null
) : Serializable

data class LabourDTO(
    val dateAndTimeOfDelivery: String? = null,
    val dateAndTimeOfLabourOnset: String? = null,
    val deliveryType: String? = null,
    val deliveryBy: String? = null,
    val deliveryAt: String? = null,
    val deliveryStatus: String? = null,
)

data class BirthDetails(
    val neonateOutcome: String?,
    val gender: String?,
    val birthWeight: Double?,
    val stateOfBaby: String?,
    val patientStatus: String?,
    val signs: List<String>?,
    val gestationalAge: String?,
    val total: Int?,
    val apgarScoreFiveMinuteDTO: APGARScoreFiveMin?
)

data class APGARScoreFiveMin(
    val activity: Int,
    val pulse: Int,
    val grimace: Int,
    val appearance: Int,
    val respiration: Int,
    val fiveMinuteTotalScore: Int,
)

