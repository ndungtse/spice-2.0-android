package org.medtroniclabs.uhis.ui.dashboard.ncd.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.medtroniclabs.uhis.app.analytics.model.UserDetail
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsDefinedParams
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.data.NCDUserDashboardRequest
import org.medtroniclabs.uhis.data.NCDUserDashboardResponse
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.ui.BaseViewModel
import org.medtroniclabs.uhis.ui.boarding.repo.MetaRepository
import org.medtroniclabs.uhis.ui.dashboard.ncd.repository.NCDDashBoardRepository
import javax.inject.Inject

@HiltViewModel
class NCDDashBoardViewModel @Inject constructor(
    private val ncdDashBoardRepository: NCDDashBoardRepository,
    private val metaRepository: MetaRepository,
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
) : BaseViewModel(dispatcherIO) {
    var userDashboardDetails = MutableLiveData<Resource<NCDUserDashboardResponse>>()
    val menuListLiveData = MutableLiveData<List<String>?>()

    fun getUserDashboardDetails(request: NCDUserDashboardRequest) {
        viewModelScope.launch(dispatcherIO) {
            userDashboardDetails.postLoading()
            setAnalyticsData(
                UserDetail.startDateTime,
                eventName = AnalyticsDefinedParams.NCDDashBoardCount,
                isCompleted = true,
            )
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
