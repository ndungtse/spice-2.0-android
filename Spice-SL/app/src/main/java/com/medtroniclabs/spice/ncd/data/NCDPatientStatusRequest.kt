package com.medtroniclabs.spice.ncd.data

import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto

data class NCDPatientStatusRequest(
    val id: String? = null,
    val memberReference: String? = null,
    val provenance: ProvanceDto? = null,
    val ncdPatientStatus: NcdPatientStatus? = null,
    val patientReference: String? = null,
)

data class NcdPatientStatus(
    val id: String? = null,
    val diabetesStatus: String? = null,
    val hypertensionStatus: String? = null,
    val hypertensionYearOfDiagnosis: String? = null,
    val diabetesYearOfDiagnosis: String? = null,
    val diabetesControlledType: String? = null,
    val diabetesDiagnosis: String? = null
)