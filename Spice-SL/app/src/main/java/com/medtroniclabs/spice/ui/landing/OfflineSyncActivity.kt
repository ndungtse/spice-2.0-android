package com.medtroniclabs.spice.ui.landing

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineConstant.KEY_REQUESTS_ID
import com.medtroniclabs.spice.databinding.FragmentOfflineSyncBinding
import com.medtroniclabs.spice.offlinesync.GetSyncStatusWorker
import com.medtroniclabs.spice.ui.landing.viewmodel.OfflineSyncViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit


@AndroidEntryPoint
class OfflineSyncActivity : AppCompatActivity() {

    private val viewModel: OfflineSyncViewModel by viewModels()
    private lateinit var binding: FragmentOfflineSyncBinding
    private val getStatusStartTimer = 2L // Mintues

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentOfflineSyncBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListener()
        initObserver()
    }

    private fun showNetworkNotAvailableError() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.title_no_network)
            .setMessage(R.string.message_no_network)
            .setPositiveButton(R.string.ok, null)

        val dialog = builder.create()
        dialog.show()
    }

    private fun setListener() {
        binding.btnStart.setOnClickListener {
            if (viewModel.connectivityManager.isNetworkAvailable()) {
                hideStartCancelButton()
                showProgressView()
                viewModel.startUploadingData(getStatusStartTimer)
            } else {
                showNetworkNotAvailableError()
            }
        }
        binding.btnCancel.setOnClickListener {
            finish()
        }
        binding.btnOkay.setOnClickListener {
            finish()
        }
    }

    private fun initObserver() {
        viewModel.oldRequestIdsLiveData.observe(this) {
            if (!it.isNullOrEmpty()) {
                hideStartCancelButton()
                showProgressView()
                viewModel.startProgress(getStatusStartTimer)
                startGetSyncStatusWorkManager(it.toTypedArray(), 0)
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
            hideProgressView()
            showCompletionView(it)
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
                viewModel.syncCompleted(true)
            } else if (workerInfo.state == WorkInfo.State.FAILED) {
                //dismissLoadingDialog()
                viewModel.syncCompleted()
            }
        }
    }

    private fun hideStartCancelButton() {
        binding.tvOfflineSync.gone()
        binding.btnCancel.gone()
        binding.btnStart.gone()
    }

    private fun showProgressView() {
        binding.tvOfflineSyncStarted.visible()
        binding.tvOffline.visible()
        binding.tvOfflineSyncProgress.visible()
        binding.progressBar.visible()
    }

    fun hideProgressView() {
        binding.progressBar.gone()
        binding.tvOfflineSyncStarted.gone()
        binding.tvOffline.gone()
        binding.tvOfflineSyncProgress.gone()
    }

    private fun showCompletionView(isSuccess: Boolean = false) {
        if (isSuccess) {
            binding.statusImage.visible()
            binding.tvOfflineSyncCompleted.text = getString(R.string.offline_data_completion)
        } else {
            binding.statusImage.invisible()
            binding.tvOfflineSyncCompleted.text = getString(R.string.offline_data_failed)
        }
        binding.tvOfflineSyncCompleted.visible()
        binding.btnOkay.visible()
    }
}