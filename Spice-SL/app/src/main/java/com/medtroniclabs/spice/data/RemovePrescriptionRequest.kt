package com.medtroniclabs.spice.data

import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto

data class RemovePrescriptionRequest(
    val prescriptionId: String,
    val provenance: ProvanceDto,
    val reason: String? = null
)