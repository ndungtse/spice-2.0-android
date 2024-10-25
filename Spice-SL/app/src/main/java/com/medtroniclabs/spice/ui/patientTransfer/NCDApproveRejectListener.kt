package com.medtroniclabs.spice.ui.patientTransfer

interface NCDApproveRejectListener {
    fun onTransferStatusUpdate(
        status: String,
        id: Long,
        tenantId: Long,
        reason: String
    )

    fun onViewDetail(patientID: Long)
}