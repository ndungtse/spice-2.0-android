package com.medtroniclabs.spice.model.medicalreview

data class EpiCatchUpPolicyItem(
    val vaccineName: String,
    val minimumAge: String,
    val maximumAge: String,
    val numberAndInterval: String,
)
