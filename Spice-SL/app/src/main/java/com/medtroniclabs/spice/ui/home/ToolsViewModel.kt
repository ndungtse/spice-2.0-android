package com.medtroniclabs.spice.ui.home

import android.content.Context
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.db.entity.MenuAdapterModel
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
    val menuListLiveData = MutableLiveData<Resource<List<MenuAdapterModel>>>()
    fun getMenuItemsList(context: Context): ArrayList<MenuAdapterModel> {
        val menuList = ArrayList<MenuAdapterModel>()
        menuList.add(
            MenuAdapterModel(
                id = 1,
                name = MenuConstants.SCREENER_MENU_ID,
                role = getString(context, R.string.chw),
                menuId = MenuConstants.SCREENER_MENU_ID,
                displayOrder = 1
            )
        )
        menuList.add(
            MenuAdapterModel(
                id = 12,
                name = MenuConstants.ICCM_MENU_ID,
                role = getString(context, R.string.chw),
                menuId = MenuConstants.ICCM_MENU_ID,
                displayOrder = 2
            )
        )
        menuList.add(
            MenuAdapterModel(
                id = 13,
                name = MenuConstants.CBS_MENU_ID,
                role = getString(context, R.string.chw),
                menuId = MenuConstants.CBS_MENU_ID,
                displayOrder = 3
            )
        )
        menuList.add(
            MenuAdapterModel(
                id = 14,
                name = MenuConstants.TB_MENU_ID,
                role = getString(context, R.string.chw),
                menuId = MenuConstants.TB_MENU_ID,
                displayOrder = 4
            )
        )
        menuList.add(
            MenuAdapterModel(
                id = 15,
                name = MenuConstants.NCD_MENU_ID,
                role = getString(context, R.string.chw),
                menuId = MenuConstants.NCD_MENU_ID,
                displayOrder = 5
            )
        )
        return menuList
    }

    fun getMenus(dashBoard: String) {
        viewModelScope.launch(dispatcherIO) {
            loginRepository.getMenu(menuListLiveData, dashBoard)
        }
    }

}