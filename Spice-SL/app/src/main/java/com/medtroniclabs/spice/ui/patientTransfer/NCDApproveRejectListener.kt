package com.medtroniclabs.spice.ui.patientTransfer

import com.medtroniclabs.spice.ncd.data.PatientTransfer

interface NCDApproveRejectListener {
    fun onTransferStatusUpdate(status: String, transfer: PatientTransfer)

    fun onViewDetail(patientID: Long)
}