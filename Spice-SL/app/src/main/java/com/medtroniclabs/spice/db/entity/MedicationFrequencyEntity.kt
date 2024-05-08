package com.medtroniclabs.spice.db.entity

data class MedicationFrequencyEntity(
    val id: Long,
    val description: String,
    val displayOrder: Long,
    val name: String,
    val frequency: Int
)