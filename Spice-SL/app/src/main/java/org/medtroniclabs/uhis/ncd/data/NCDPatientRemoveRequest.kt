package org.medtroniclabs.uhis.ncd.data

import org.medtroniclabs.uhis.data.offlinesync.model.ProvanceDto

data class NCDPatientRemoveRequest(
    val patientId: String? = null,
    val reason: String? = null,
    val provenance: ProvanceDto = ProvanceDto(),
    val otherReason: String? = null,
    val memberId: String? = null,
)
