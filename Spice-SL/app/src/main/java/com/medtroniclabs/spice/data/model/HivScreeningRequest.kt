package com.medtroniclabs.spice.data.model

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
    private val patientReference: String? = null,
    private val encounterId: String? = null,
)

data class HivScreeningSummaryResponse(
    val id: String,
    val eligibilities: Eligibilities,
    val a1TestResult: String,
    val a2TestResult: String,
    val a3TestResult: String,
    val entryPoint: String
)

data class Eligibilities(
    val hivPopulationType: List<String>,
    val Symptoms: List<String>
)
