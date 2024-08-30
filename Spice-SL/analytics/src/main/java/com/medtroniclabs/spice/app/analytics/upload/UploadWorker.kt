package com.medtroniclabs.spice.app.analytics.upload

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class UploadWorker(context: Context, params: WorkerParameters): CoroutineWorker(context, params) {



    override suspend fun doWork(): Result {


        return Result.success()
    }

}