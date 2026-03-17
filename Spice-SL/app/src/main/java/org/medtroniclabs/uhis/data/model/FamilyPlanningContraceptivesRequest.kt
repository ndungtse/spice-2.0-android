package org.medtroniclabs.uhis.data.model

data class FamilyPlanningContraceptivesRequest(
    val id: String? = null,
    val clinicalNotes: String? = null,
    val patientId: String? = null,
    val assessmentName: String? = null,
    val encounter: MedicalReviewEncounter? = null,
    val contraceptive: Contraceptive? = null,
)

data class Contraceptive(
    val occupation: String? = null,
    val maritalStatus: String? = null,
    val clientType: String? = null,
    val postPartum: String? = null,
    val combinedOralContraceptive: String? = null,
    val otherCombinedOralContraceptive: String? = null,
    val progestinOnlyOrals: String? = null,
    val otherProgestinOnlyOrals: String? = null,
    val microlutQuantity: Long? = null,
    val injectables: String? = null,
    val otherInjectables: String? = null,
    val iucd: List<String>? = null,
    val implants: String? = null,
    val otherImplants: String? = null,
    val condoms: String? = null,
    val emergencyContraceptive: String? = null,
    val permanentMethod: String? = null,
    val otherPermanentMethod: String? = null,
)

data class FamilyPlanningSummaryResponse(
    val clinicalNotes: String? = null,
    val contraceptive: Contraceptive? = null,
)

data class FamilyPlanningCreateResponse(
    val patientReference: String? = null,
    val encounterId: String? = null,
)
