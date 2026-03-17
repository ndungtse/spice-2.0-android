package org.medtroniclabs.uhis.network

import android.content.Context
import android.os.Build
import android.provider.Settings
import org.medtroniclabs.uhis.common.AppConstants.ANDROID
import org.medtroniclabs.uhis.ncd.data.DeviceDetails

object DeviceInformation {
    fun getDeviceDetails(context: Context): DeviceDetails =
        DeviceDetails(
            deviceId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID,
            ),
            name = Build.MANUFACTURER,
            model = Build.MODEL,
            type = ANDROID,
            version = Build.VERSION.RELEASE,
        )
}
