package com.medtroniclabs.spice.ncd.data

import com.google.gson.annotations.SerializedName

data class NCDPatientTransferNotificationCountResponse (
    @SerializedName("count")
    val patientTransferCount: Long
)