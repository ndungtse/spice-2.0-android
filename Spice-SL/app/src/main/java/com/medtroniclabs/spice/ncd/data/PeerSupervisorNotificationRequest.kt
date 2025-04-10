package com.medtroniclabs.spice.ncd.data

data class PeerSupervisorNotificationRequest(
    var userId: String? = null,
    var ids: List<Int>? = null
)

data class PeerSupervisorNotificationResponse(
    val id: Int,
    val formDataId: String,
    val formData: FormData,
    val formType: String,
    val userId: Int,
    val viewed: Boolean
)

data class FormData(
    val memberPhoneNumber: String,
    val chwPhoneNumber: String,
    val countryCode: String,
    val assessmentDate: String,
    val otherNotifiableConditions: String,
    val memberName: String,
    val notifiableConditions: List<String>,
    val villageName: String,
    val chwName: String
)
