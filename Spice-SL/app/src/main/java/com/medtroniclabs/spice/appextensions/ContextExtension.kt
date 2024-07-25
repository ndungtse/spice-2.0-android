package com.medtroniclabs.spice.appextensions

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.offlinesync.ScheduledSyncWork
import com.medtroniclabs.spice.ui.assessment.referrallogic.utils.ReferralStatus
import java.util.concurrent.TimeUnit

const val syncWorkerName = "SyncWorker"
private const val durationGap = 15L // 1 * 60L - 1 hour once. Need to give in minutes
private const val notificationId = 99 // Unique ID for the notification

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

fun Context.changePatientStatus(input: String): String {
    val onTreatmentString = getString(R.string.on_treatment)
    val regex = Regex(ReferralStatus.OnTreatment.name, RegexOption.IGNORE_CASE)
    return input.replace(regex, onTreatmentString)
}

fun Context.scheduleSyncWorker() {
    val workManager = WorkManager.getInstance(this)

    val constraint = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val periodicWorkRequest =
        PeriodicWorkRequestBuilder<ScheduledSyncWork>(durationGap, TimeUnit.MINUTES)
            .setInitialDelay(0, TimeUnit.SECONDS)
            .setConstraints(constraint)
            .build()

    // Enqueue the periodic work request with a unique name and policy
    workManager.enqueueUniquePeriodicWork(
        syncWorkerName,
        ExistingPeriodicWorkPolicy.KEEP,
        periodicWorkRequest
    )

}
private fun observerStatus(workManager: WorkManager) {
    workManager.getWorkInfosForUniqueWorkLiveData(syncWorkerName).observeForever {
        for (woker in it) {
            Log.e("WORKER_TEST","Status "+woker.state.name)
        }
    }
}

fun Context.isBackgroundWorkerRunning(): Boolean {
    val workManager = WorkManager.getInstance(this)
    val list = workManager.getWorkInfosForUniqueWork(syncWorkerName).get()
    if (list.isNotEmpty()) {
        return list[0].state == WorkInfo.State.RUNNING
    }
    return false
}

fun Context.cancelAllWorker() {
    val workManager = WorkManager.getInstance(this)
    workManager.cancelUniqueWork(syncWorkerName)
}

fun Context.showNotification(title: String = "Background Task", message: String = "Syncing Offline data") {
    val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = "worker_channel"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, "Worker Notifications", NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)
    }

    val notification = NotificationCompat.Builder(this, channelId)
        .setContentTitle(title)
        .setContentText(message)
        .setSmallIcon(R.drawable.ic_spice_logo)
        .build()

    notificationManager.notify(notificationId, notification)
}

fun Context.hideNotification() {
    val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancel(notificationId)
}