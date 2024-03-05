package com.medtroniclabs.spice.ui.boarding.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.utils.ConnectivityManager
import com.medtroniclabs.spice.ui.boarding.repo.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResourceLoadingViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val connectivityManager: ConnectivityManager,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) :
    ViewModel() {

    val metaDataCompleteLiveData = MutableLiveData<Resource<Boolean>>()
    private val workflowNames = mutableListOf<String>()
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
            loginRepository.getMetaDataInformation(metaDataCompleteLiveData,workflowNames,meta)
        }
    }

}