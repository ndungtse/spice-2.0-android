package com.medtroniclabs.spice.data.offlinesync.model

data class ResponseRxBuddy(
    val id: Long,
    val patientMemberId: String,
    val type: String,
    val registry: ResponseRxBuddyRegister,
    val isActive: Boolean,
)
