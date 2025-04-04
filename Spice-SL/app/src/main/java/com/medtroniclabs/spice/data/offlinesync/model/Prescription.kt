package com.medtroniclabs.spice.data.offlinesync.model

data class Prescription(
    val category: String?,
    val prescribedDays: Long,
    val medicationName: String,
    val frequency: Int,
    val isActive: Boolean
)
