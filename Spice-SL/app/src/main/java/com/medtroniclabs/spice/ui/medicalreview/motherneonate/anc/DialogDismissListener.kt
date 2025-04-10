package com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc

interface DialogDismissListener {
    fun onDialogDismissed(isBp:Boolean, isHeight:Boolean)
}

interface DialogDismissListenerForTb {
    fun onDialogDismissedForTb(isPatientType:Boolean)
}