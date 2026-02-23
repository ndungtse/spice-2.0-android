package com.medtroniclabs.spice.ui.patientTransfer.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.app.analytics.model.UserDetail
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.ncd.data.NCDPatientTransferValidate
import com.medtroniclabs.spice.ncd.data.NCDRegionSiteModel
import com.medtroniclabs.spice.ncd.data.NCDSiteRoleModel
import com.medtroniclabs.spice.ncd.data.NCDSiteRoleResponse
import com.medtroniclabs.spice.ncd.data.NCDTransferCreateRequest
import com.medtroniclabs.spice.ncd.data.RegionSiteResponse
import com.medtroniclabs.spice.network.SingleLiveEvent
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.BaseViewModel
import com.medtroniclabs.spice.ui.patientEdit.NCDPatientEditRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NCDPatientTransferViewModel @Inject constructor(
    private var ncdPatientEditRepo: NCDPatientEditRepository,
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
) : BaseViewModel(dispatcherIO) {
    val validateTransferResponse = SingleLiveEvent<Resource<HashMap<String, Any>>>()
    var patientTransferResponse = SingleLiveEvent<Resource<String>>()
    val searchSiteResponse = MutableLiveData<Resource<ArrayList<RegionSiteResponse>>>()
    val searchRoleUserResponse = MutableLiveData<Resource<ArrayList<NCDSiteRoleResponse>>>()

    fun validatePatientTransfer(request: NCDPatientTransferValidate) {
        viewModelScope.launch(dispatcherIO) {
            validateTransferResponse.postLoading()
            validateTransferResponse.postValue(
                ncdPatientEditRepo.validatePatientTransfer(request),
            )
        }
    }

    fun createPatientTransfer(request: NCDTransferCreateRequest) {
        viewModelScope.launch(dispatcherIO) {
            patientTransferResponse.postLoading()
            setAnalyticsData(
                UserDetail.startDateTime,
                eventName = AnalyticsDefinedParams.NCDPatientTransfer,
                isCompleted = true,
            )
            patientTransferResponse.postValue(
                ncdPatientEditRepo.createPatientTransfer(request),
            )
        }
    }

    fun searchSite(request: NCDRegionSiteModel) {
        viewModelScope.launch(dispatcherIO) {
            searchSiteResponse.postLoading()
            searchSiteResponse.postValue(
                ncdPatientEditRepo.searchSite(request),
            )
        }
    }

    fun searchRoleUser(request: NCDSiteRoleModel) {
        viewModelScope.launch(dispatcherIO) {
            searchRoleUserResponse.postLoading()
            searchRoleUserResponse.postValue(
                ncdPatientEditRepo.searchRoleUser(request),
            )
        }
    }
}
