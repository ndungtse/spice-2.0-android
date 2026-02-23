package com.medtroniclabs.spice.ncd.data

import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto

data class NCDPatientTransferUpdateRequest(
    val id: Long,
    val rejectReason: String? = null,
    val transferStatus: String,
    val memberReference: String? = null,
    val provenance: ProvanceDto = ProvanceDto(),
    val transferSite: SiteObject? = null,
)
