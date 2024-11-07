package com.medtroniclabs.spice.ui.dashboard.ncd.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.NCDUserDashboardRequest
import com.medtroniclabs.spice.data.NCDUserDashboardResponse
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.dashboard.ncd.repository.NCDDashBoardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class NCDDashBoardViewModel @Inject constructor(
    private val ncdDashBoardRepository: NCDDashBoardRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {

    var userDashboardDetails = MutableLiveData<Resource<NCDUserDashboardResponse>>()

    fun getUserDashboardDetails(request: NCDUserDashboardRequest) {
        viewModelScope.launch(dispatcherIO) {
            userDashboardDetails.postLoading()
            userDashboardDetails.postValue(ncdDashBoardRepository.getUserDashboardDetails(request))
        }
    }
}