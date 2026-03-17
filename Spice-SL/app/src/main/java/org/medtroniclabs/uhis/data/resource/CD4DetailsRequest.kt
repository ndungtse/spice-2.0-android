package org.medtroniclabs.uhis.data.resource

data class CD4DetailsRequest(
    val patientReference: String? = null,
    val isCD4: Boolean = false,
    val isCD4Percentage: Boolean = false,
    val skip: Int = 0,
    val limit: Int = 5,
)

data class CD4DetailsResponse(
    val dateValue: String? = null,
    val cd4: Double? = null,
    val cd4Percentage: Double? = null,
)
