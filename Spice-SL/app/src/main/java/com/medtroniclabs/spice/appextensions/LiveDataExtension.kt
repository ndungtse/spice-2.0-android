package com.medtroniclabs.spice.appextensions

import androidx.lifecycle.MutableLiveData
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState

fun <T> MutableLiveData<Resource<T>>.setSuccess(data: T? = null) {
    value = Resource(ResourceState.SUCCESS, data)
}

fun <T> MutableLiveData<Resource<T>>.postSuccess(
    data: T? = null,
    optionalData: Boolean? = null,
) {
    postValue(Resource(ResourceState.SUCCESS, data, optionalData = optionalData))
}

fun <T> MutableLiveData<Resource<T>>.setLoading() {
    value = Resource(ResourceState.LOADING, value?.data)
}

fun <T> MutableLiveData<Resource<T>>.postLoading() {
    postValue(Resource(ResourceState.LOADING, value?.data))
}

fun <T> MutableLiveData<Resource<T>>.setError(message: String? = null) {
    value = Resource(ResourceState.ERROR, value?.data, message)
}

fun <T> MutableLiveData<Resource<T>>.postError(
    message: String? = null,
    optionalData: Boolean? = null,
) {
    postValue(Resource(ResourceState.ERROR, value?.data, message, optionalData = optionalData))
}
