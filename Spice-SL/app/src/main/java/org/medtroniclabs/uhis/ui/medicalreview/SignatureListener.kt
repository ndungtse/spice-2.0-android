package org.medtroniclabs.uhis.ui.medicalreview

import android.graphics.Bitmap

interface SignatureListener {
    fun applySignature(signature: Bitmap)
}
