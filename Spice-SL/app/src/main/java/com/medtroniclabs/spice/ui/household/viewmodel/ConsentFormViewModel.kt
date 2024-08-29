package com.medtroniclabs.spice.ui.household.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.repo.HouseHoldRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConsentFormViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val houseHoldRepository: HouseHoldRepository
) : ViewModel() {

    var enableConfirmLiveData = MutableLiveData<Pair<Boolean, Boolean>>()
    val termsAndConditionStringLiveData = MutableLiveData<String>()

    init {
        viewModelScope.launch(dispatcherIO) {
            val content = houseHoldRepository.getConsentForm()?.content ?: ""
            termsAndConditionStringLiveData.postValue(CommonUtils.formatConsent(content))
        }
    }

    fun disableConfirm() {
        enableConfirmLiveData.postValue(Pair(false, false))
    }

    fun enableForInitial(flag: Boolean) {
        val old = enableConfirmLiveData.value ?: Pair(false, false)
        enableConfirmLiveData.postValue(Pair(flag, old.second))
    }

    fun enableForSignature(flag: Boolean) {
        val old = enableConfirmLiveData.value ?: Pair(false, false)
        enableConfirmLiveData.postValue(Pair(old.first, flag))
    }
}