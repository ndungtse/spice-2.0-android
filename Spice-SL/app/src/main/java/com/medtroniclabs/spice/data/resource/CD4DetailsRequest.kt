package com.medtroniclabs.spice.data.resource

data class CD4DetailsRequest(
    val patientReference: String? = null,
    val isCD4: Boolean = false,
    val isCD4Percentage: Boolean = false,
)

data class CD4DetailsResponse(
    val dateValue: String? = null,
    val doubleValue: Double? = null,
)
