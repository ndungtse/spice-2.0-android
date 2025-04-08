package com.medtroniclabs.spice.data.model

import com.medtroniclabs.spice.data.DiagnosisDiseaseModel
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.data.Prescription
import com.medtroniclabs.spice.data.history.Investigation
import com.medtroniclabs.spice.data.history.PatientStatus

data class BpAndWeightResponse(
    val systolic: Double? = null,
    val diastolic: Double? = null,
    val pulse: Double? = null,
    val weight: Double? = null,
    val height: Double? = null,
    val bmi: Double? = null,
    val dateValue: String? = null
)

data class TbHistory(
    val id: String? = null,
    val diagnosis: ArrayList<DiagnosisDiseaseModel>? = null,
    val presentingComplaints: List<String>? = null,
    val tbSummary: TbSummary? = null,
    val presentingComplaintsNotes: String? = null,
    val systemicExaminations: List<MedicalReviewMetaItems>? = null,
    val systemicExaminationNotes: String? = null,
    val comorbiditiesNotes: String? = null,
    val comorbidities: List<String>? = null,
    val memberId: String? = null,
    val clinicalNotes: String? = null,
    val lastReviewDate: String? = null,
    val prescriptions: List<Prescription>? = null,
    val investigations: List<Investigation>? = null,
    val summaryStatus:List<PatientStatus>?=null
)

data class TbSummary(
    val tbScreening : TbScreening? = null
)

data class TbScreening(
    val id: String? = null,
    val householdNo: String? = null,
    val patientId: String? = null,
    val name: String? = null,
    val villageId: String? = null,
    val hasCough: Boolean? = null,
    val hasCoughLastedLonger: Boolean? = null,
    val hasNightSweats: Boolean? = null,
    val hasFever: Boolean? = null,
    val hasWeightLoss: Boolean? = null,
    val houseHoldUUID: String? = null,
    val dateOfOnset: String? = null
)

