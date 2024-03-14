package com.medtroniclabs.spice.ui.landing

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.medtroniclabs.spice.databinding.FragmentOfflineSyncBinding
import com.medtroniclabs.spice.offlinesync.GetSyncStatusWorker
import com.medtroniclabs.spice.offlinesync.PostSyncWorker
import com.medtroniclabs.spice.offlinesync.utils.OfflineConstant.KEY_REQUESTS_ID
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit


@AndroidEntryPoint
class OfflineSyncFragment : Fragment() {

    companion object {
        const val TAG = "OfflineSyncFragment"
    }

    private lateinit var binding: FragmentOfflineSyncBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOfflineSyncBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSync.isEnabled = false

        binding.btnSync.setOnClickListener {
            //(requireActivity() as BaseActivity).showLoading()
            binding.loadingProgress.visibility = View.VISIBLE
            binding.btnSync.visibility = View.GONE
            startPostWorkManager()
        }
    }

    private fun startPostWorkManager() {
        val workManager = WorkManager.getInstance(requireContext())

        val constrain = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val postSyncWorker = OneTimeWorkRequestBuilder<PostSyncWorker>()
            .setConstraints(constrain)
            .build()

        workManager.enqueue(postSyncWorker)

        workManager.getWorkInfoByIdLiveData(postSyncWorker.id).observe(viewLifecycleOwner) { workerInfo ->
            if (workerInfo.state == WorkInfo.State.SUCCEEDED) {
                val requestIds = workerInfo.outputData.getStringArray(KEY_REQUESTS_ID)
                if (!requestIds.isNullOrEmpty())
                    startGetSyncStatusWorkManager(requestIds, 1)
            }
        }
    }

    private fun startGetSyncStatusWorkManager(requestIds: Array<String>, duration: Long) {
        val workManager = WorkManager.getInstance(requireContext())

        val data = Data.Builder()
            .putStringArray(KEY_REQUESTS_ID, requestIds)
            .build()

        val constrain = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val getSyncStatusWorker = OneTimeWorkRequestBuilder<GetSyncStatusWorker>()
            .setInitialDelay(duration, TimeUnit.MINUTES)
            .setInputData(data)
            .setConstraints(constrain)
            .build()

        workManager.enqueue(getSyncStatusWorker)

        workManager.getWorkInfoByIdLiveData(getSyncStatusWorker.id).observe(viewLifecycleOwner) { workerInfo ->
            if (workerInfo.state == WorkInfo.State.SUCCEEDED) {
                Log.e("Test","Success")
                binding.loadingProgress.visibility = View.GONE
                binding.btnSync.visibility = View.VISIBLE
            }
        }
    }
}