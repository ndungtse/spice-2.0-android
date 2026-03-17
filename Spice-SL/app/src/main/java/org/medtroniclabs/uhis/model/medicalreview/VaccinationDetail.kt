package org.medtroniclabs.uhis.model.medicalreview

import java.time.LocalDate

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
    val category: String,
    val vaccineOrder: Int,
    var updatedScheduleDate: LocalDate? = null,
    var isEdited: Boolean? = false,
)
