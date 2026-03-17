package org.medtroniclabs.uhis.model.assessment

data class AssessmentDetails(
    val id: Long,
    val householdMemberLocalId: Long,
    val villageId: String, // Village Id of household
    val assessmentType: String,
    val assessmentDetails: String,
    val patientId: String?, // member - patient Id
    val referralStatus: String,
    val referredReason: ArrayList<String>? = null,
    var otherDetails: String? = null,
    val callResult: String? = null, // Call logs for PS and PHU
    val memberId: String?, // Member -FHIR id
    val householdId: String?, // Household - FHIR id
    var isReferred: Boolean = false,
    val createdAt: Long,
    val followUpId: Long? = null,
    val neonatePatientId: String? = null,
    val neonatePatientReferenceId: Long? = null,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
)
