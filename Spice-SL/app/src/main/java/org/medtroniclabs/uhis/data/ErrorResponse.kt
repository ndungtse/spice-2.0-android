package org.medtroniclabs.uhis.data

data class ErrorResponse(var code: Int?, var message: String?, var exception: String? = null)
