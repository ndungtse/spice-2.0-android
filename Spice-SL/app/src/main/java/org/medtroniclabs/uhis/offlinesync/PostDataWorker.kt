package org.medtroniclabs.uhis.offlinesync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.medtroniclabs.uhis.db.local.RoomHelper
import org.medtroniclabs.uhis.repo.OfflineSyncRepository

@HiltWorker
class PostDataWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted userParameter: WorkerParameters,
    val roomHelper: RoomHelper,
    val offlineSyncRepository: OfflineSyncRepository,
) : CoroutineWorker(context, userParameter) {
    override suspend fun doWork(): Result {
        println("Worker Starts : $id")
        for (i in 0..25) {
            if (isStopped) {
                println("Failed... Interrupt by Other worker")
                return Result.failure()
            }

            println("Index $i")
            Thread.sleep(2000)
        }
        println("Worker Ends")

        return Result.success()
    }
}
