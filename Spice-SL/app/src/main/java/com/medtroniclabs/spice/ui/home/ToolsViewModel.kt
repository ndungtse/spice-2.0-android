package com.medtroniclabs.spice.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.db.entity.MenuEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.BaseViewModel
import com.medtroniclabs.spice.ui.boarding.repo.MetaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ToolsViewModel @Inject constructor(
    private val metaRepository: MetaRepository,
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
) : BaseViewModel(dispatcherIO) {
    var selectedHouseholdId = -1L
    var selectedHouseholdMemberID = -1L
    var selectedMemberDob: String? = null
    var followUpId = -1L
    val resultRMNCHFlowHashMap = HashMap<String, Any>()
    val menuListLiveData = MutableLiveData<Resource<List<MenuEntity>>>()
    val resultANCFlowHashMap = HashMap<String, Any>()
    var isEMTCT = false
    var isMenutypeHiv: Boolean = false

    fun getMenuForClinicalWorkflows(gender: String?) {
        viewModelScope.launch(dispatcherIO) {
            menuListLiveData.postLoading()
            menuListLiveData.postValue(
                metaRepository.getMenuForClinicalWorkflows(
                    selectedHouseholdMemberID,
                    gender,
                ),
            )
        }
    }

    suspend fun getANCPNCStatus(): String? = metaRepository.getANCPNCStatus(selectedHouseholdMemberID)

    fun getMyPatientsMenuItemsList() {
        viewModelScope.launch(dispatcherIO) {
            menuListLiveData.postLoading()
            menuListLiveData.postValue(
                metaRepository.getMenu(),
            )
        }
    }
}
