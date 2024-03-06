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
import androidx.work.WorkManager
import com.medtroniclabs.spice.databinding.FragmentOfflineSyncBinding
import com.medtroniclabs.spice.offlinesync.PostSyncWorker
import dagger.hilt.android.AndroidEntryPoint


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

        binding.btnSync.setOnClickListener {
            startWorkManager()
        }
    }

    private fun startWorkManager() {
        val workManager = WorkManager.getInstance(requireContext())

        val data = Data.Builder()
            .putString(PostSyncWorker.KEY_INPUT,"Hello")
            .build()

        val constrain = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val postSyncWorker = OneTimeWorkRequestBuilder<PostSyncWorker>()
            .setInputData(data)
            .setConstraints(constrain)
            .build()

        workManager.enqueue(postSyncWorker)

        workManager.getWorkInfoByIdLiveData(postSyncWorker.id).observe(viewLifecycleOwner) { workerInfo ->
            Log.e("Test", "Test")
        }
    }
}