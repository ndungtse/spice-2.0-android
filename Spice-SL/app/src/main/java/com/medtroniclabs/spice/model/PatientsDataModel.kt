package com.medtroniclabs.spice.model

data class PatientsDataModel(
    var skip: Long? = null,
    var limit: Int? = null,
    val villageIds: List<Long>? = null,
    val searchText: String? = null,
    val districtId: Long? = null,
    val referencePatientId: String? = null,
    val filter: MedicalReviewFilterModel? = null
)

data class MedicalReviewFilterModel(
    val patientStatus: List<String>? = null,
    val visitDate: List<String>? = null
)
