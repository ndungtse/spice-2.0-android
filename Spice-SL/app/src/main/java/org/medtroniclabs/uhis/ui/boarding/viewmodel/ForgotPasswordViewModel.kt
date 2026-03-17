package org.medtroniclabs.uhis.ui.boarding.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.repo.ForgotPasswordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val forgotPasswordRepository: ForgotPasswordRepository,
) : ViewModel() {
    val resetTokenLiveData = MutableLiveData<String?>()
    var resetEmailResponseLiveData = MutableLiveData<Resource<Boolean>>()
    var verifyTokenLiveData = MutableLiveData<Resource<Boolean>>()
    var resetPasswordLiveData = MutableLiveData<Resource<Boolean>>()

    fun updateResetToken(token: String?) {
        resetTokenLiveData.postValue(token)
    }

    fun resetEmail(emailOrPhoneNumber: String) {
        viewModelScope.launch(dispatcherIO) {
            resetEmailResponseLiveData.postLoading()
            resetEmailResponseLiveData.postValue(forgotPasswordRepository.forgotPassword(emailOrPhoneNumber))
        }
    }

    fun validateToken(verifyToken: String) {
        viewModelScope.launch(dispatcherIO) {
            verifyTokenLiveData.postLoading()
            verifyTokenLiveData.postValue(forgotPasswordRepository.verifyToken(verifyToken))
        }
    }

    fun resetPassword(password: String) {
        viewModelScope.launch {
            resetTokenLiveData.value?.let { token ->
                resetPasswordLiveData.postLoading()
                resetPasswordLiveData.postValue(
                    forgotPasswordRepository.resetPassword(
                        token,
                        password,
                    ),
                )
            }
        }
    }
}
