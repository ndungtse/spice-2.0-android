package org.medtroniclabs.uhis.data.offlinesync.model

data class HouseholdMemberCallRegisterDto(
    val memberId: String,
    val patientId: String?,
    val villageId: String,
    val callDate: Long,
)
