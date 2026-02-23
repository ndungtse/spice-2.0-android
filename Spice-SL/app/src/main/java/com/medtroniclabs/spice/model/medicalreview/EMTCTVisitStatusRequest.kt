package com.medtroniclabs.spice.model.medicalreview

import com.medtroniclabs.spice.data.model.MedicalReviewEncounter

data class EMTCTVisitStatusRequest(
    val encounter: MedicalReviewEncounter? = null,
    val stringValue: String? = null,
)

data class EMTCTVisitStatusResponse(
    val message: String? = null,
    val entity: EMTCTEntity? = null,
)

data class EMTCTEntity(
    val patientReference: String? = null,
    val weight: Double? = null,
    val height: Double? = null,
    val type: String? = null,
    val systolic: Int? = null,
    val numberValue: Int? = null,
    val birthHistoryDTO: Any? = null,
    val bmi: Double? = null,
    val dateValue: String? = null,
    val pulse: Int? = null,
    val diastolic: Int? = null,
    val patientType: String? = null,
    val stringValue: String? = null,
    val cd4: Int? = null,
    val cd4Percentage: Double? = null,
    val doubleValue: Double? = null,
    val encounter: EncounterResponse? = null,
)

data class EncounterResponse(
    val id: String? = null,
    val patientVisitId: String? = null,
    val patientReference: String? = null,
    val referred: Boolean? = null,
    val patientId: String? = null,
    val patientStatus: String? = null,
    val memberId: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val householdId: String? = null,
    val provenance: ProvenanceResponse? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val visitNumber: Int? = null,
    val visitId: String? = null,
    val villageId: String? = null,
    val type: String? = null,
)

data class ProvenanceResponse(
    val userId: String? = null,
    val spiceUserId: Int? = null,
    val organizationId: String? = null,
    val modifiedDate: String? = null,
)
