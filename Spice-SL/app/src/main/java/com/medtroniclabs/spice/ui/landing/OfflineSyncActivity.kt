package com.medtroniclabs.spice.ui.landing

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.syncWorkerName
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineConstant.KEY_REQUESTS_ID
import com.medtroniclabs.spice.databinding.FragmentOfflineSyncBinding
import com.medtroniclabs.spice.offlinesync.GetSyncStatusWorker
import com.medtroniclabs.spice.ui.SpiceRootActivity
import com.medtroniclabs.spice.ui.landing.adapter.OfflineSyncEntitiesAdapter
import com.medtroniclabs.spice.ui.landing.viewmodel.OfflineSyncViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit


@AndroidEntryPoint
class OfflineSyncActivity : SpiceRootActivity() {

    private val viewModel: OfflineSyncViewModel by viewModels()
    private lateinit var binding: FragmentOfflineSyncBinding
    private lateinit var unSyncedCountAdapter: OfflineSyncEntitiesAdapter
    private val getStatusStartTimer = 30L // Seconds
    private var isBackgroundSyncRunning: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentOfflineSyncBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overrideBackPress()

        setListener()
        initObserver()
        checkBGSyncStatus()
        viewModel.setUserJourney(getString(R.string.offline_sync))
    }

    private fun checkBGSyncStatus() {
        val workManager = WorkManager.getInstance(this)
        workManager.getWorkInfosForUniqueWorkLiveData(syncWorkerName).observe(this) {
            isBackgroundSyncRunning = !it.isNullOrEmpty() && it[0].state == WorkInfo.State.RUNNING
        }
    }

    private fun setListener() {
        binding.rvUnSyncedDetail.layoutManager = LinearLayoutManager(this)
        unSyncedCountAdapter = OfflineSyncEntitiesAdapter()
        binding.rvUnSyncedDetail.adapter = unSyncedCountAdapter

        binding.btnStart.setOnClickListener {
            if (isBackgroundSyncRunning) {
                showErrorDialogue(
                    getString(R.string.alert),
                    getString(R.string.background_sync_in_progress),
                    isNegativeButtonNeed = false
                ) {
                    finish()
                }
            } else {
                initiateUpload()
            }
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.btnOkay.setOnClickListener {
            if (binding.btnOkay.text.toString() == getString(R.string.retry)) {
                val requestIds =
                    SecuredPreference.getStringArray(SecuredPreference.EnvironmentKey.OFFLINE_SYNC_REQUEST_ID.name)
                requestIds?.let {
                    initiateGetStatus(it)
                }
            } else
                finish()
        }
    }

    private fun initiateUpload() {
        if (viewModel.connectivityManager.isNetworkAvailable()) {
            showProgressView()
            viewModel.startUploadingData()
        } else {
            showErrorDialogue(
                getString(R.string.title_no_network),
                getString(R.string.message_no_network),
                isNegativeButtonNeed = false
            ) { _ -> }
        }
    }

    private fun initObserver() {
        viewModel.unSyncedCountLiveData.observe(this) {
            unSyncedCountAdapter.updateList(it)
        }

        viewModel.lastSyncedAtLiveData.observe(this) {
            binding.tvLastSyncedAt.text = it
        }

        viewModel.oldRequestIdsLiveData.observe(this) {
            it?.let {
                initiateGetStatus(it)
            }
        }

        viewModel.postRequestIdsLiveData.observe(this) { requestIds ->
            if (requestIds.isNotEmpty()) {
                startGetSyncStatusWorkManager(requestIds.toTypedArray(), getStatusStartTimer)
            } else {
                startGetSyncStatusWorkManager(arrayOf(), 0)
            }
        }

        viewModel.progressLiveData.observe(this) {
            binding.progressBar.progress = it
            binding.tvOfflineSyncProgress.text = "$it%"
        }

        viewModel.statusLiveData.observe(this) {
            showCompletionView(it.first, it.second)
        }
    }

    private fun initiateGetStatus(arr: Array<String>) {
        showProgressView()
        viewModel.startProgress(getStatusStartTimer)
        startGetSyncStatusWorkManager(arr, 0)
    }

    private fun startGetSyncStatusWorkManager(requestIds: Array<String>, duration: Long) {
        val workManager = WorkManager.getInstance(this)

        val data = Data.Builder()
            .putStringArray(KEY_REQUESTS_ID, requestIds)
            .build()

        val constrain = Constraints.Builder()
            //.setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val getSyncStatusWorker = OneTimeWorkRequestBuilder<GetSyncStatusWorker>()
            .setInitialDelay(duration, TimeUnit.SECONDS)
            .setInputData(data)
            .setConstraints(constrain)
            .build()

        workManager.enqueue(getSyncStatusWorker)

        workManager.getWorkInfoByIdLiveData(getSyncStatusWorker.id).observe(this) { workerInfo ->
            if (workerInfo.state == WorkInfo.State.SUCCEEDED) {
                binding.btnOkay.text = getString(R.string.okay)
                viewModel.syncCompleted(true)
            } else if (workerInfo.state == WorkInfo.State.FAILED) {
                val errorData = workerInfo.outputData
                val errorMessage = errorData.getString("failureReason")
                if (errorMessage != null) {
                    binding.btnOkay.text = getString(R.string.retry)
                    viewModel.syncCompleted(message = getString(R.string.message_no_network))
                } else {
                    viewModel.syncCompleted()
                }
            }
        }
    }

    private fun showProgressView() {
        binding.clBeforeSync.gone()
        binding.clAfterSync.gone()
        binding.clSyncInProgress.visible()
    }

    private fun showCompletionView(isSuccess: Boolean = false, message: String? = null) {
        binding.clAfterSync.gone()
        binding.clSyncInProgress.gone()
        binding.clAfterSync.visible()
        if (isSuccess) {
            binding.statusImage.setImageDrawable(getDrawable(R.drawable.success_icon))
            binding.tvOfflineSyncCompleted.text = getString(R.string.offline_data_completion)
        } else {
            binding.statusImage.setImageDrawable(getDrawable(R.drawable.ic_failed))
            binding.tvOfflineSyncCompleted.text = message ?: getString(R.string.offline_data_failed)
        }
    }

    private fun overrideBackPress() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button event
                // For example, finish the activity
                // finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }
}