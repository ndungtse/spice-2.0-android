package com.medtroniclabs.spice.data

data class FollowUpPatientModel(
    val id: Long,
    val localPatientId: Long,
    val name: String?,
    val patientId: String?,
    val dateOfBirth: String?,
    val gender: String?,
    val reason: String?,
    val patientStatus: String?,
    val village: String?,
    val householdName: String?,
    val landmark: String?,
    val type: String?,
    val encounterType: String?,
    val remainingRetryAttempt: Int?,
    val nextVisitDate: String? = null,
    val referredDate: String? = null,
)
