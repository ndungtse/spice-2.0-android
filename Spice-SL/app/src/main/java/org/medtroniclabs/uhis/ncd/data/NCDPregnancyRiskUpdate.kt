package org.medtroniclabs.uhis.ncd.data

import org.medtroniclabs.uhis.data.offlinesync.model.ProvanceDto

data class NCDPregnancyRiskUpdate(
    var isPregnancyRisk: Boolean? = null,
    val memberReference: String? = null,
    val patientReference: String? = null,
    val provenance: ProvanceDto? = null,
)
