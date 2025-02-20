package com.medtroniclabs.spice.model.medicalreview

data class VaccinationDetail(
    val id: String? = null,
    val type: String,
    val value: Int = 0,
    var status: String? = null,
    val vaccineName: String,
    val scheduledDate: String,
    var vaccinatedDate: String? = null,
    val doseClosureWeeks: String,
    val reason: String? = null,
    val displayOrder: Int,
    var isEdited: Boolean? = false,
)

