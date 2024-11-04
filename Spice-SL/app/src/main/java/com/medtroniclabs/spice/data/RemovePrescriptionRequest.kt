package com.medtroniclabs.spice.data

import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto

data class RemovePrescriptionRequest(
    val prescriptionId: String? = null,
    val provenance: ProvanceDto? = null,
    val discontinuedReason: String? = null,
    val requestFrom: String? = null
)