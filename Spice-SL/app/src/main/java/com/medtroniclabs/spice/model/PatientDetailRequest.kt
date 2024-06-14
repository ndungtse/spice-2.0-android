package com.medtroniclabs.spice.model

data class PatientDetailRequest(
    val id: String? = null,
    val patientId: String? = null,
    val ticketId:String? = null,
    val assessmentType:String? = null
)

data class ReferralDetailRequest(
    val id: String? = null,
    val patientId: Long? = null,
    val ticketId:String? = null,
    val type:String? = null
)