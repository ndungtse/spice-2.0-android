package com.medtroniclabs.spice.ui.boarding.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.common.EncryptionUtil
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.LoginResponse
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.utils.ConnectivityManager
import com.medtroniclabs.spice.repo.AssessmentRepository
import com.medtroniclabs.spice.repo.FollowUpRepository
import com.medtroniclabs.spice.repo.HouseHoldRepository
import com.medtroniclabs.spice.ui.boarding.repo.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val houseHoldRepository: HouseHoldRepository,
    private val assessmentRepository: AssessmentRepository,
    private val followUpRepository: FollowUpRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {


    @Inject
    lateinit var connectivityManager: ConnectivityManager

    var loginResponseLiveData = MutableLiveData<Resource<LoginResponse>>()
    val unSyncedDataCountLiveData = MutableLiveData<Int>()

    init {
        getUnSyncedDataCount()
    }

    fun doLogin(
        username: String,
        password: String
    ) {
        viewModelScope.launch(dispatcherIO) {
            loginResponseLiveData.postLoading()
            loginResponseLiveData.postValue(loginRepository.doLogin(username,password))
        }
    }

    private fun getUnSyncedDataCount() {
        viewModelScope.launch(dispatcherIO) {
            val count = houseHoldRepository.getUnSyncedHouseholdCount() +
                    houseHoldRepository.getUnSyncedHouseholdMemberCount() +
                    assessmentRepository.getUnSyncedAssessmentCount() +
                    followUpRepository.getUnSyncedFollowUpCount()
            unSyncedDataCountLiveData.postValue(count)
        }
    }

}