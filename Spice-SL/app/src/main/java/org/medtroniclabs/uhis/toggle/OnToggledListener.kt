package org.medtroniclabs.uhis.toggle

interface OnToggledListener {
    fun onSwitched(
        toggleableView: ToggleableView?,
        isOn: Boolean,
    )
}
