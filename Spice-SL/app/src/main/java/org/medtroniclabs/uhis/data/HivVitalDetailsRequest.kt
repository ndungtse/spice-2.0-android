package org.medtroniclabs.uhis.data

data class HivVitalDetailsRequest(
    val patientReference: String? = null,
    val memberId: String? = null,
    val types: ArrayList<String>,
)

data class HivVitalDetailsResponse(
    val cd4: String? = null,
    val whoClinicalStage: String? = null,
    val weight: Double? = null,
    val cd4Percentage: String? = null,
    val height: Double? = null,
)
