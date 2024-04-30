package com.medtroniclabs.spice.ui.landing

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineConstant.KEY_REQUESTS_ID
import com.medtroniclabs.spice.databinding.FragmentOfflineSyncBinding
import com.medtroniclabs.spice.model.landing.LoadingDialogFragment
import com.medtroniclabs.spice.offlinesync.GetSyncStatusWorker
import com.medtroniclabs.spice.offlinesync.PostSyncWorker
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.landing.adapter.OfflineSyncEntitiesAdapter
import com.medtroniclabs.spice.ui.landing.viewmodel.OfflineSyncViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit


@AndroidEntryPoint
class OfflineSyncActivity : BaseActivity() {

    private val viewModel: OfflineSyncViewModel by viewModels()
    private lateinit var binding: FragmentOfflineSyncBinding
    private lateinit var adapter: OfflineSyncEntitiesAdapter
    private val getStatusStartTimer = 1L // Mintues

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentOfflineSyncBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            isToolbarVisible = true,
            title = getString(R.string.offline_sync)
        )

        initView()
        initObserver()
    }

    private fun initView() {
        //binding.btnSync.isEnabled = false

        adapter = OfflineSyncEntitiesAdapter()
        binding.rvEntityList.adapter = adapter

        binding.btnSync.setOnClickListener {
            showLoadingDialog()
            startPostWorkManager()
        }
    }

    private fun showLoadingDialog() {
        LoadingDialogFragment.newInstance().show(supportFragmentManager, LoadingDialogFragment.TAG)
    }

    private fun dismissLoadingDialog() {
        (supportFragmentManager.findFragmentByTag(LoadingDialogFragment.TAG) as? LoadingDialogFragment)?.dismiss()
    }
    private fun initObserver() {
        viewModel.unSyncedCountLiveData.observe(this) { list ->
            val totalUnSyncedCount = list.sumOf { it.unSyncedCount }
            binding.btnSync.isEnabled = totalUnSyncedCount > 0
            adapter.updateList(list)
        }
    }

    private fun startPostWorkManager() {
        val workManager = WorkManager.getInstance(this)

        val constrain = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val postSyncWorker = OneTimeWorkRequestBuilder<PostSyncWorker>()
            .setConstraints(constrain)
            .build()

        workManager.enqueue(postSyncWorker)

        workManager.getWorkInfoByIdLiveData(postSyncWorker.id).observe(this) { workerInfo ->
            if (workerInfo.state == WorkInfo.State.SUCCEEDED) {
                val requestIds =
                    SecuredPreference.getStringArray(SecuredPreference.EnvironmentKey.OFFLINE_SYNC_REQUEST_ID.name)
                if (!requestIds.isNullOrEmpty()) {
                    startGetSyncStatusWorkManager(requestIds, getStatusStartTimer)
                } else {
                    dismissLoadingDialog()
                    showErrorDialogue(
                        getString(R.string.title_sync_status),
                        getString(R.string.message_sync_error)
                    ) {}
                }
            }
        }
    }

    private fun startGetSyncStatusWorkManager(requestIds: Array<String>, duration: Long) {
        val workManager = WorkManager.getInstance(this)

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

        workManager.getWorkInfoByIdLiveData(getSyncStatusWorker.id).observe(this) { workerInfo ->
            if (workerInfo.state == WorkInfo.State.SUCCEEDED) {
                viewModel.getUnSyncedCount()
                dismissLoadingDialog()
                Toast.makeText(this, "Sync Success!", Toast.LENGTH_LONG).show()
            } else if (workerInfo.state == WorkInfo.State.FAILED) {
                dismissLoadingDialog()
                Toast.makeText(this, "Sync Failed! Please try again later", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }
}