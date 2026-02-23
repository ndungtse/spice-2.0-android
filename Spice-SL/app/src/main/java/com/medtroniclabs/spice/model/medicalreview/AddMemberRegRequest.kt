package com.medtroniclabs.spice.model.medicalreview

import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto

data class AddMemberRegRequest(
    var dateOfBirth: String? = null,
    var gender: String? = null,
    val householdId: Int? = null,
    var name: String? = null,
    val patientId: String? = null,
    val motherPatientId: Int? = null,
    val child: Boolean? = null,
    var isPregnant: Boolean? = null,
    var phoneNumber: String? = null,
    var phoneNumberCategory: String? = null,
    var provenance: ProvanceDto? = null,
    var village: String? = null,
    var villageId: String? = null,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
)
