package com.medtroniclabs.spice.toggle

interface OnToggledListener {
    fun onSwitched(toggleableView: ToggleableView?, isOn: Boolean)
}