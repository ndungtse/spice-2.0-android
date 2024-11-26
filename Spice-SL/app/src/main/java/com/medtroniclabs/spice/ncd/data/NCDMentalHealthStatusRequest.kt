package com.medtroniclabs.spice.ncd.data

import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto

data class NCDMentalHealthStatusRequest(
    val mentalHealthStatus: MentalHealthStatus? = null,
    val substanceUseStatus: MentalHealthStatus? = null,
    val provenance: ProvanceDto? = null,
    val ncdPatientStatus: NcdPatientStatus? = null,
    val patientReference: String? = null,
    val memberReference: String? = null
)

data class MentalHealthStatus(
    val status: String? = null,
    val comments: String? = null,
    val yearOfDiagnosis: Int? = null,
    val mentalHealthDisorder: ArrayList<String>? = null,
)
