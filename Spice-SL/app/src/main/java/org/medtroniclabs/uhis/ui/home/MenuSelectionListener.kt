package org.medtroniclabs.uhis.ui.home

interface MenuSelectionListener {
    fun onMenuSelected(
        menuId: String,
        subModule: String?,
    )
}
