package com.medtroniclabs.spice.ncd.medicalreview

interface NCDDialogDismissListener {
    fun onDialogDismissed(isConfirmed: Boolean = false)
    fun closePage()
}