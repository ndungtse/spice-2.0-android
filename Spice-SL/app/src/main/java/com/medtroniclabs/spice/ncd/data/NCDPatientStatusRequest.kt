package com.medtroniclabs.spice.ncd.data

import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto

data class NCDPatientStatusRequest(
    val relatedPersonId: String? = null,
    val provenance: ProvanceDto? = null,
    val ncdPatientStatus: NcdPatientStatus? = null,
    val patientId: String? = null,
)

data class NcdPatientStatus(
    val diabetesStatus: String? = null,
    val hypertensionStatus: String? = null,
    val hypertensionYearOfDiagnosis: String? = null,
    val diabetesYearOfDiagnosis: String? = null,
    val diabetesControlledType: String? = null,
    val diabetesDiagnosis: String? = null
)