package com.medtroniclabs.spice.ui.boarding.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.utils.ConnectivityManager
import com.medtroniclabs.spice.repo.HouseHoldRepository
import com.medtroniclabs.spice.repo.OfflineSyncRepository
import com.medtroniclabs.spice.ui.boarding.repo.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResourceLoadingViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val offlineSyncRepository: OfflineSyncRepository,
    private val connectivityManager: ConnectivityManager,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) :
    ViewModel() {

    val metaDataCompleteLiveData = MutableLiveData<Resource<Boolean>>()
    val householdsLiveData = MutableLiveData<Resource<Boolean>>()

    private val workflowNames = mutableListOf<Long>()
    private val meta = mutableListOf<String>()

    init {
        getMetaDataInformation()
    }

    fun getMetaDataInformation() {
        viewModelScope.launch(dispatcherIO) {
            if (!connectivityManager.isNetworkAvailable()) {
                metaDataCompleteLiveData.postError()
                return@launch
            }
            metaDataCompleteLiveData.postValue(loginRepository.getMetaDataInformation(workflowNames,meta))
        }
    }

    fun downloadInitialDetails() {
        viewModelScope.launch(dispatcherIO) {
            val requestIds = offlineSyncRepository.postOfflineUnSyncedChanges()
            if (requestIds == null) {
                householdsLiveData.postError()
            } else {
                offlineSyncRepository.getHouseholdAndMembers(householdsLiveData)
            }
        }
    }

}