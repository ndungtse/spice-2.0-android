package com.medtroniclabs.spice.app.analytics

import android.content.Context
import android.widget.Toast

object ToastMessage {

    fun showAnalyticsToast(context: Context, message:String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}