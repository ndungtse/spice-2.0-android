package com.medtroniclabs.spice.ui.boarding.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.common.EncryptionUtil
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.LoginResponse
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.utils.ConnectivityManager
import com.medtroniclabs.spice.ui.boarding.repo.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {


    @Inject
    lateinit var connectivityManager: ConnectivityManager

    var loginResponseLiveData = MutableLiveData<Resource<LoginResponse>>()
    var noInternetResponse = MutableLiveData<Boolean>()

    fun doLogin(
        username: String,
        password: String,
        context: Context
    ) {
        viewModelScope.launch(dispatcherIO) {
            if (!connectivityManager.isNetworkAvailable()) {
                loginResponseLiveData.postError(context.getString(R.string.no_internet_error))
                val isToShowAlert =
                    (username == SecuredPreference.getString(SecuredPreference.EnvironmentKey.USERNAME.name)
                            && EncryptionUtil.getSecurePassword(password) == SecuredPreference.getString(
                        SecuredPreference.EnvironmentKey.PASSWORD.name
                    ))
                noInternetResponse.postValue(isToShowAlert)
                return@launch
            }
            loginRepository.doLogin(username, password, loginResponseLiveData)
        }
    }

}