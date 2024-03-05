package com.medtroniclabs.spice.ui.home

import android.content.Context
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.db.entity.MenuAdapterModel
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.MenuConstants
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
    val menuListLiveData = MutableLiveData<Resource<List<MenuAdapterModel>>>()
    val resultRMNCHFlowHashMap = HashMap<String, Any>()

    fun getMenus(dashBoard: String) {
        viewModelScope.launch(dispatcherIO) {
            loginRepository.getMenu(menuListLiveData, dashBoard)
        }
    }


}