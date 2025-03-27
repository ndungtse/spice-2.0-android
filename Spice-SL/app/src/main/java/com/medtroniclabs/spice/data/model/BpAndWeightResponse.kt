package com.medtroniclabs.spice.data.model

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
    val presentingComplaints: List<String>? = null,
    val tbSummary: TbSummary? = null,
    val presentingComplaintsNotes: String? = null,
    val systemicExaminations: List<String>? = null,
    val systemicExaminationNotes: String? = null,
    val comorbidities: List<String>? = null,
    val memberId: String? = null,
    val clinicalNotes: String? = null,
    val lastReviewDate: String? = null,
    val prescriptions: List<String>? = null,
    val investigations: List<String>? = null
)

data class TbSummary(
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

