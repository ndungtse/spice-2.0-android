package org.medtroniclabs.uhis.ui.patientTransfer

import org.medtroniclabs.uhis.ncd.data.PatientTransfer

interface NCDApproveRejectListener {
    fun onTransferStatusUpdate(
        status: String,
        transfer: PatientTransfer,
    )

    fun onViewDetail(patientID: Long)
}
