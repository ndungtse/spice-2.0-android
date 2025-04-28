package com.medtroniclabs.spice.data.offlinesync.model

data class Prescription(
    val categoryName: String?,
    val prescribedDays: Long,
    val medicationName: String,
    val frequency: Int,
    val isActive: Boolean
)
