package com.medtroniclabs.spice.model.medicalreview

data class VaccinationGroupItem(
    val groupName: String,
    val scheduleDate: String,
    val isFuture: Boolean,
    val vaccinationItems: List<VaccinationDetail>
)
