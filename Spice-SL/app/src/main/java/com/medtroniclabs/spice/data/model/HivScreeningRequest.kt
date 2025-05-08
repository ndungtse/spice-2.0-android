package com.medtroniclabs.spice.data.model

import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.model.medicalreview.ExaminationDetail

data class HivScreeningRequest(
    private val historyList: List<String>? = null,
    private val populationTypeList: List<String>? = null,
    private val isHIVTestedBefore: String? = null,
    private val hivTestDuration: String? = null,
    private val entryPoint: String? = null,
    private val a1TestResult: String? = null,
    private val a2TestResult: String? = null,
    private val a3TestResult: String? = null,
    private val encounter: MedicalReviewEncounter? = null
)


data class HivScreeningResponse(
    val patientReference: String? = null,
    val encounterId: String? = null,
)

data class HivCreateScreeningSummaryResponse(
    val id: String? = null,
    val eligibilities: Eligibilities? = null,
    val a1TestResult: String? = null,
    val a2TestResult: String? = null,
    val a3TestResult: String? = null,
    val entryPoint: String? = null
)

data class Eligibilities(
    val hivPopulationType: List<String>? = null,
    val Symptoms: List<String>? = null
)


data class HivMedicalReviewSummaryRequest(
    val category: String? = null,
    val encounterType: String? = null,
    val id: String? = null,
    val memberId: String? = null,
    val nextVisitDate: String? = null,
    val patientId: String? = null,
    val patientReference: String? = null,
    val patientStatus: String? = null,
    val provenance: ProvanceDto? = null,
    val villageId: String? = null
)


data class HivMedicalReviewSummaryResponse(
    val message: String? = null,
    val entity: MedicalReviewEntity? = null,
    val status: Boolean? = null,
    val entityList: Any? = null,
    val responseCode: Int? = null,
    val totalCount: Any? = null
)

data class MedicalReviewEntity(
    val id: String? = null,
    val medicalSupplies: Any? = null,
    val memberId: String? = null,
    val householdId: Any? = null,
    val patientId: String? = null,
    val villageId: String? = null,
    val encounterType: String? = null,
    val diagnosis: Any? = null,
    val cost: Any? = null,
    val nextVisitDate: String? = null,
    val patientStatus: String? = null,
    val patientReference: String? = null,
    val type: Any? = null,
    val reason: Any? = null,
    val category: String? = null,
    val provenance: ProvanceDto? = null,
    val referralDetails: Any? = null
)


