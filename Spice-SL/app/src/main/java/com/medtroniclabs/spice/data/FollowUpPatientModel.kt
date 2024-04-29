package com.medtroniclabs.spice.data

data class FollowUpPatientModel(
    val id: Long? = null,
    val reason: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val patientStatus: String? = null,
    val startDate: String? = null,
    val village: String? = null,
    val landMark: String? = null,
    val hhName: String? = null,
    val memberId: Long? = null,
    val totalCall: Int? = null,
    val callsMade: Int? = null,
    val dateOfBirth: String? = null,
    val age: Int? = null,
    val gender: String? = null,
)
