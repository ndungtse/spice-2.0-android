package com.medtroniclabs.spice.appextensions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineConstant
import com.medtroniclabs.spice.offlinesync.GetSyncStatusWorker
import com.medtroniclabs.spice.ui.assessment.referrallogic.utils.ReferralStatus
import java.util.concurrent.TimeUnit

private val getStatusWorker = "GetStatusWorker"

fun Context.isGpsEnabled(): Boolean {
    val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
}

fun Context.isFineAndCoarseLocationPermissionGranted(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

fun Context.getPatientStatus(status: String?): String? {
    status?.let {
        return when (status) {
            ReferralStatus.OnTreatment.name -> {
                this.getString(R.string.on_treatment)
            }

            ReferralStatus.Referred.name -> {
                this.getString(R.string.referred)
            }

            ReferralStatus.Recovered.name -> {
                this.getString(R.string.recovered)
            }

            else -> {
                return null
            }
        }
    } ?: return null
}

fun Context.getSyncStatusWithDelay(requestIds: Array<String>, delay: Long) {
    val workManager = WorkManager.getInstance(this)

    val data = Data.Builder()
        .putStringArray(OfflineConstant.KEY_REQUESTS_ID, requestIds)
        .build()

    val constrain = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val getSyncStatusWorker = OneTimeWorkRequestBuilder<GetSyncStatusWorker>()
        .setInitialDelay(delay, TimeUnit.SECONDS)
        .setInputData(data)
        .setConstraints(constrain)
        .build()

    workManager.enqueueUniqueWork(getStatusWorker, ExistingWorkPolicy.REPLACE, getSyncStatusWorker)
}