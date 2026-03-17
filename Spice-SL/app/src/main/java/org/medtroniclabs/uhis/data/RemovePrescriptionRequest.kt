package org.medtroniclabs.uhis.data

import org.medtroniclabs.uhis.data.offlinesync.model.ProvanceDto

data class RemovePrescriptionRequest(
    val prescriptionId: String? = null,
    val provenance: ProvanceDto? = null,
    val discontinuedReason: String? = null,
    val regimenLine: Int? = null,
    val reasonsForChange: String? = null,
    val requestFrom: String? = null,
    val encounter: EncounterDetails? = null,
)
