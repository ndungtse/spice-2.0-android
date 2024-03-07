package com.medtroniclabs.spice.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.db.entity.MenuEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.boarding.repo.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ToolsViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {

    var selectedHouseholdMemberID = -1L
    val resultRMNCHFlowHashMap = HashMap<String, Any>()
    val menuListLiveData = MutableLiveData<Resource<List<MenuEntity>>>()

    fun getMenuForClinicalWorkflows() {
        viewModelScope.launch(dispatcherIO) {
            loginRepository.getMenuForClinicalWorkflows(menuListLiveData,selectedHouseholdMemberID)
        }
    }


}