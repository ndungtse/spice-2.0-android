package com.medtroniclabs.spice.ncd.data

data class PeerSupervisorNotificationRequest(
    var userId: String? = null,
    var ids: List<Int>? = null
)

data class PeerSupervisorNotificationResponse(
    val id: Int,
    val formDataId: String,
    val formData: FormData? = null,
    val formType: String? = null,
    val userId: Int,
    val viewed: Boolean
)

data class FormData(
    val memberPhoneNumber: String? = null,
    val chwPhoneNumber: String? = null,
    val countryCode: String? = null,
    val assessmentDate: String? = null,
    val otherNotifiableConditions: String? = null,
    val memberName: String? = null,
    val notifiableConditions: List<String>? = null,
    val villageName: String? = null,
    val chwName: String? = null
)
