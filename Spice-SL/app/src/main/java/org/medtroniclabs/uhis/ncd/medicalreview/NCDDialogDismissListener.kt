package org.medtroniclabs.uhis.ncd.medicalreview

interface NCDDialogDismissListener {
    fun onDialogDismissed(isConfirmed: Boolean = false)

    fun closePage()
}
