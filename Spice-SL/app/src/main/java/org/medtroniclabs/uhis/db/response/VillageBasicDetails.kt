package org.medtroniclabs.uhis.db.response

data class VillageBasicDetails(
    val id: Long,
    val name: String,
    val districtId: Long,
)
