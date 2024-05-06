package com.medtroniclabs.spice.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.db.entity.MenuEntity
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
    val resultRMNCHFlowHashMap = HashMap<String, Any>()
    val menuListLiveData = MutableLiveData<Resource<List<MenuEntity>>>()
    val resultANCFlowHashMap = HashMap<String, Any>()

    fun getMenuForClinicalWorkflows() {
        viewModelScope.launch(dispatcherIO) {
            menuListLiveData.postLoading()
            menuListLiveData.postValue(loginRepository.getMenuForClinicalWorkflows(selectedHouseholdMemberID))
        }
    }

    fun getMyPatientsMenuItemsList(): ArrayList<MenuEntity> {
        val menuList = ArrayList<MenuEntity>()
        menuList.add(
            MenuEntity(
                id = 1,
                name = MenuConstants.GENERAL_ID,
                roleName = SecuredPreference.getRole(),
                menuId = MenuConstants.GENERAL_ID,
                displayOrder = 1
            )
        )
        menuList.add(
            MenuEntity(
                id = 12,
                name = MenuConstants.MOTHER_AND_NEONATE_ID,
                roleName = SecuredPreference.getRole(),
                menuId = MenuConstants.MOTHER_AND_NEONATE_ID,
                displayOrder = 2
            )
        )
        menuList.add(
            MenuEntity(
                id = 13,
                name = MenuConstants.UNDER_AGE_FIVE_TO_TWO_MONTHS_ID,
                roleName = SecuredPreference.getRole(),
                menuId = MenuConstants.UNDER_AGE_FIVE_TO_TWO_MONTHS_ID,
                displayOrder = 3
            )
        )
        menuList.add(
            MenuEntity(
                id = 14,
                name = MenuConstants.UNDER_AGE_ABOVE_FIVE_YEAR_ID,
                roleName = SecuredPreference.getRole(),
                menuId = MenuConstants.UNDER_AGE_ABOVE_FIVE_YEAR_ID,
                displayOrder = 4
            )
        )
        menuList.add(
            MenuEntity(
                id = 15,
                name = MenuConstants.EPI_ID,
                roleName = SecuredPreference.getRole(),
                menuId = MenuConstants.EPI_ID,
                displayOrder = 5
            )
        )
        menuList.add(
            MenuEntity(
                id = 15,
                name = MenuConstants.TB_MENU_ID,
                roleName = SecuredPreference.getRole(),
                menuId = MenuConstants.TB_MENU_ID,
                displayOrder = 6
            )
        )
        return menuList
    }

}