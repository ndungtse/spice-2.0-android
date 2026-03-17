package org.medtroniclabs.uhis.network.resource

sealed class ResourceState {
    object LOADING : ResourceState()

    object SUCCESS : ResourceState()

    object ERROR : ResourceState()
}
