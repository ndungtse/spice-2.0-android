package com.medtroniclabs.spice.ui.dashboard.ncd.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.NCDUserDashboardRequest
import com.medtroniclabs.spice.data.NCDUserDashboardResponse
import com.medtroniclabs.spice.db.entity.MenuEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.boarding.repo.MetaRepository
import com.medtroniclabs.spice.ui.dashboard.ncd.repository.NCDDashBoardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class NCDDashBoardViewModel @Inject constructor(
    private val ncdDashBoardRepository: NCDDashBoardRepository,
    private val metaRepository: MetaRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {

    var userDashboardDetails = MutableLiveData<Resource<NCDUserDashboardResponse>>()
    val menuListLiveData = MutableLiveData<List<String>?>()

    fun getUserDashboardDetails(request: NCDUserDashboardRequest) {
        viewModelScope.launch(dispatcherIO) {
            userDashboardDetails.postLoading()
            userDashboardDetails.postValue(ncdDashBoardRepository.getUserDashboardDetails(request))
        }
    }

    fun getMenus() {
        viewModelScope.launch(dispatcherIO) {
            val response = metaRepository.getMenu()
            val menus = response.data?.map { it.name }
            menuListLiveData.postValue(menus)
        }
    }
}