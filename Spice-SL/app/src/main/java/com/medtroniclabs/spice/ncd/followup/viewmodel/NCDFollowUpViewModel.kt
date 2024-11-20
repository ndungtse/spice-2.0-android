package com.medtroniclabs.spice.ncd.followup.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.SortModel
import com.medtroniclabs.spice.ncd.data.CustomDate
import com.medtroniclabs.spice.ncd.data.FollowUpUpdateRequest
import com.medtroniclabs.spice.ncd.data.RegisterCallResponse
import com.medtroniclabs.spice.ncd.followup.adapter.NCDFollowUpDataSource
import com.medtroniclabs.spice.ncd.followup.fragment.NCDFollowUpFilterEnum
import com.medtroniclabs.spice.ncd.followup.repo.NCDFollowUpRepo
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.SingleLiveEvent
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NCDFollowUpViewModel @Inject constructor(
    private val apiHelper: ApiHelper,
    private val ncdFollowUpRepo: NCDFollowUpRepo,
    private var roomHelper: RoomHelper,
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher
) : BaseViewModel(dispatcherIO) {
    var spanCount: Int = DefinedParams.span_count_1
    var searchText = ""
    var type = ""
    var sortModel = SortModel()
    var customDate: CustomDate? = null
    var dateRange: String? = null
    var totalPatientCount = MutableLiveData<Int>()
    var filterCount = MutableLiveData<Int>()
    var searchLiveData = MutableLiveData<Boolean>()
    var getPatientRegisterResponse = SingleLiveEvent<Resource<RegisterCallResponse>>()
    var statusUpdateResponse = SingleLiveEvent<Resource<HashMap<String, Any>>>()
    var remainingAttempts = listOf<ChipViewItemModel>()
    val patientsDataSource =
        Pager(config = PagingConfig(pageSize = DefinedParams.LIST_LIMIT), pagingSourceFactory = {
            NCDFollowUpDataSource(
                apiHelper = apiHelper,
                ncdFollowUpRepo = ncdFollowUpRepo,
                roomHelper = roomHelper,
                searchText = searchText,
                sortModel = sortModel,
                customDate = if (dateRange == NCDFollowUpFilterEnum.CUSTOMISE.title) customDate else null,
                dateRange = dateRange,
                type = type,
                remainingAttempts = remainingAttempts.mapNotNull { it.id }
            ) { getPatientsCount ->
                totalPatientCount.postValue(getPatientsCount)
            }
        }).flow


    fun getPatientCallRegister() {
        viewModelScope.launch(dispatcherIO) {
            getPatientRegisterResponse.postLoading()
            getPatientRegisterResponse.postValue(ncdFollowUpRepo.getPatientCallRegister())
        }
    }


    fun updatePatientCallRegister(request: FollowUpUpdateRequest) {
        viewModelScope.launch(dispatcherIO) {
            statusUpdateResponse.postLoading()
            statusUpdateResponse.postValue(ncdFollowUpRepo.updatePatientCallRegister(request))
        }
    }


    val callResultHashMap = HashMap<String, Any>()
    val patientStatusHashMap = HashMap<String, Any>()
    val unSuccessfulHashMap = HashMap<String, Any>()

    fun searchLiveData(text: String) {
        this.searchText = text
        searchLiveData.postValue(true)
    }

    fun filterLiveData() {
        searchLiveData.postValue(true)
        val count = listOfNotNull(
            dateRange.takeIf { !it.isNullOrBlank() },
            remainingAttempts.takeIf { !it.isNullOrEmpty() }
        ).size

        if (count > 0) {
            filterCount.postValue(count)
        }
    }
}