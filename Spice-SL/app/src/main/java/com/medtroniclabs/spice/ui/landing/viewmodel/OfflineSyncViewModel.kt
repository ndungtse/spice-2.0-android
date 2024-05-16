package com.medtroniclabs.spice.ui.landing.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.utils.ConnectivityManager
import com.medtroniclabs.spice.repo.OfflineSyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OfflineSyncViewModel @Inject constructor(
    private val offlineSyncRepository: OfflineSyncRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    val oldRequestIdsLiveData = MutableLiveData<List<String>?>()
    val progressLiveData = MutableLiveData<Int>()
    val postRequestIdsLiveData = MutableLiveData<List<String>>()
    val statusLiveData = MutableLiveData<Boolean>()

    private var progressJob: Job? = null

    init {
        viewModelScope.launch {
            val requestIds =
                SecuredPreference.getStringArray(SecuredPreference.EnvironmentKey.OFFLINE_SYNC_REQUEST_ID.name)
            requestIds?.let {
                oldRequestIdsLiveData.postValue(it.toList())
            }
        }
    }

    fun startUploadingData(minutes: Long) {
        viewModelScope.launch(dispatcherIO) {
            val requestIds = offlineSyncRepository.startSyncOfflineData()
            if (requestIds != null) {
                startProgress(minutes)
                if (requestIds.isNotEmpty()) { // Has some changes in local
                    SecuredPreference.saveStringArray(
                        SecuredPreference.EnvironmentKey.OFFLINE_SYNC_REQUEST_ID.name,
                        requestIds.toTypedArray()
                    )
                    postRequestIdsLiveData.postValue(requestIds)
                } else { // no changes in local. Need to download data from server.
                    postRequestIdsLiveData.postValue(listOf())
                }
            } else { // Post local change api has failed
                syncCompleted()
            }
        }
    }

    fun startProgress(minutes: Long) {
        val initialCounterGap = (minutes * 60 * 1000) / 90
        val retryCounterGap = (minutes * 60 * 1000) / 3
        progressJob = viewModelScope.launch(dispatcherIO) {
            repeat(90) {
                progressLiveData.postValue(it)
                delay(initialCounterGap)
            }

            repeat(10) {
                progressLiveData.postValue(90+it)
                delay(retryCounterGap)
            }
        }
    }

    fun syncCompleted(isSuccess: Boolean = false) {
        progressLiveData.postValue(100)
        progressJob?.cancel()
        statusLiveData.postValue(isSuccess)
    }

}