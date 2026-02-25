package com.medtroniclabs.spice.model.assessment

/**
 * Member details fetched from HouseholdMember with some inner join with Household
 *
 * @param name Name of household member
 * @param gender Gender of household member
 * @param dateOfBirth Date of birth of household member
 * @param patientId Patient id of household member
 * @param villageId Village id of household to which household member belongs to
 * @param memberId FHIR id of household member
 * @param householdNo Number of household to which the household member belongs to
 * @param householdId FHIR id of household
 * @param householdLocalId Auto generated id of household
 * @param id Auto generated id of household member
 * @param phoneNumber Phone number of the household member
 */
data class AssessmentMemberDetails(
    val name: String,
    val gender: String,
    val dateOfBirth: String,
    val patientId: String? = null,
    val villageId: String,
    val memberId: String? = null,
    val householdNo: Long? = null,
    val householdId: String? = null,
    val householdLocalId: Long,
    val id: Long,
    var isPregnant: Boolean? = null,
    val contactTracingStatus: Int? = null,
    val phoneNumber: String? = null,
)
