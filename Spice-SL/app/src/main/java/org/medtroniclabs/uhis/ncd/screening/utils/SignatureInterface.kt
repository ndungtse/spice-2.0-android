package org.medtroniclabs.uhis.ncd.screening.utils

import android.graphics.Bitmap

interface SignatureInterface {
    fun applySignature(
        signature: Bitmap?,
        initial: String?,
    )
}
