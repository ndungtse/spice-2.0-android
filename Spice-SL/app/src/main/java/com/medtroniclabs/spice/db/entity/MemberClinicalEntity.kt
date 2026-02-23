package com.medtroniclabs.spice.db.entity

data class MemberClinicalEntity(
    val patientId: String?,
    val visitCount: Long,
    val clinicalDate: String?,
    val numberOfNeonate: Long? = null,
    val isDeliveryAtHome: Boolean? = null,
    val neonateHouseholdMemberLocalId: Long? = null,
    val isNeonateAlive: Boolean? = null,
    val isNeonateDeathRecordedByPHU: Boolean? = null,
)
