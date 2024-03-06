package com.medtroniclabs.spice.offlinesync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay

class PostSyncWorker(context: Context, userParameter: WorkerParameters) :
    CoroutineWorker(context, userParameter) {

    companion object {
        const val KEY_INPUT = "key_input"
    }

    override suspend fun doWork(): Result {
        val text = inputData.getString(KEY_INPUT)

        try {

            delay(5000)

            val outputData = Data.Builder().putString(KEY_INPUT, getUserEmotion(text)).build()
            return Result.success(outputData)
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }
    }


    fun getUserEmotion(userText: String?): String {
        val emotionList = listOf("Sad", "Happy", "Angry", "Surprise", "Tired", "Bored")
        return emotionList.random()
    }

}