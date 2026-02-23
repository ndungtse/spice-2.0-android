package com.medtroniclabs.spice.ui.medicalreview

import android.graphics.Bitmap

interface SignatureListener {
    fun applySignature(signature: Bitmap)
}
