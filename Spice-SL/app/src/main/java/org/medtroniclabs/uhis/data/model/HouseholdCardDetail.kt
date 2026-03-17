package org.medtroniclabs.uhis.data.model

data class HouseholdCardDetail(
    val id: Long,
    val name: String,
    val householdNo: Long? = null,
    val villageName: String,
    val memberRegistered: Int,
    val memberAdded: Int,
)
