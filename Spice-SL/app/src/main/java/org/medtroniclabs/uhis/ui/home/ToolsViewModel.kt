package org.medtroniclabs.uhis.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.db.entity.MenuEntity
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.ui.BaseViewModel
import org.medtroniclabs.uhis.ui.boarding.repo.MetaRepository
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
