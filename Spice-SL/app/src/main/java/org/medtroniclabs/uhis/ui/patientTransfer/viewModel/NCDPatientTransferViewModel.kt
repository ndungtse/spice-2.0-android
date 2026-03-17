package org.medtroniclabs.uhis.ui.patientTransfer.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.medtroniclabs.uhis.app.analytics.model.UserDetail
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsDefinedParams
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.ncd.data.NCDPatientTransferValidate
import org.medtroniclabs.uhis.ncd.data.NCDRegionSiteModel
import org.medtroniclabs.uhis.ncd.data.NCDSiteRoleModel
import org.medtroniclabs.uhis.ncd.data.NCDSiteRoleResponse
import org.medtroniclabs.uhis.ncd.data.NCDTransferCreateRequest
import org.medtroniclabs.uhis.ncd.data.RegionSiteResponse
import org.medtroniclabs.uhis.network.SingleLiveEvent
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.ui.BaseViewModel
import org.medtroniclabs.uhis.ui.patientEdit.NCDPatientEditRepository
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
