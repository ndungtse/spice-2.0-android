package com.medtroniclabs.spice.data.model

import com.medtroniclabs.spice.data.DiagnosisDiseaseModel
import com.medtroniclabs.spice.data.Prescription
import com.medtroniclabs.spice.data.history.Investigation
import com.medtroniclabs.spice.data.history.PatientStatus
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.ncd.data.Diagnosis

data class HivScreeningRequest(
    private val historyList: List<String>? = null,
    private val populationTypeList: List<String>? = null,
    private val otherPopulationType: String? = null,
    private val isHIVTestedBefore: String? = null,
    private val hivTestDuration: String? = null,
    private val entryPoint: String? = null,
    private val a1TestResult: String? = null,
    private val a2TestResult: String? = null,
    private val a3TestResult: String? = null,
    private val encounter: MedicalReviewEncounter? = null,
    private val lastMenstrualPeriod: String? = null,
    private val gestationalInWeeks: Int? = null,
    private val expectedDateOfDelivery: String? = null,
    private val hivSyphilisDuoTest:String? = null,
    private val hbsAGTest:String? = null,
    private val screeningType: String? = null,
    private val clinicalNotes: String? = null,
    private val id: String? = null,
    private val otherEntryPoint: String? = null

)


data class HivScreeningResponse(
    val patientReference: String? = null,
    val encounterId: String? = null,
)

data class HivCreateScreeningSummaryResponse(
    val id: String? = null,
    val eligibilities: Eligibilities? = null,
    val diagnosis : List<DiagnosisDiseaseModel>? = null,
    val a1TestResult: String? = null,
    val a2TestResult: String? = null,
    val a3TestResult: String? = null,
    val entryPoint: String? = null,
    val hivSyphilisDuoTest: String? = null,
    val summaryStatus: List<PatientStatus>? = null,
    val hbsAGTest: String? = null,
    val clinicalNotes : String? = null,
    val prescriptions: List<Prescription>? = null,
    val investigations: List<Investigation>? = null,
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

data class ViralLoadRequest(
    val patientReference: String? = null,
    val memberReference: String? = null,
)

data class ViralLoadResponse(
    val collectionDate: String? = null,
    val gestationAtCollection: String?= null,
    val result: String?= null,
)
