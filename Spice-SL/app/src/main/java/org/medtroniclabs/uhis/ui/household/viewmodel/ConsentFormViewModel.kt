package org.medtroniclabs.uhis.ui.household.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.appextensions.textOrEmpty
import org.medtroniclabs.uhis.common.CommonUtils
import org.medtroniclabs.uhis.common.SecuredPreference
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.repo.HouseHoldRepository
import org.medtroniclabs.uhis.ui.BaseViewModel
import org.medtroniclabs.uhis.ui.medicalreview.hiv.repo.HivMedicalReviewRepo
import javax.inject.Inject

@HiltViewModel
class ConsentFormViewModel @Inject constructor(
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
    private val houseHoldRepository: HouseHoldRepository,
    private val hivRepository: HivMedicalReviewRepo,
    @ApplicationContext private val context: Context,
) : BaseViewModel(dispatcherIO) {
    var enableConfirmLiveData = MutableLiveData<Pair<Boolean, Boolean>>()
    val termsAndConditionStringLiveData = MutableLiveData<String>()
    var isHivFlow: Boolean = false
    var isHouseHoldFlow: Boolean = false

    init {
        viewModelScope.launch(dispatcherIO) {
            if (isHivFlow) {
                val content = hivRepository.getConsentForm()?.content ?: ""
                termsAndConditionStringLiveData.postValue(CommonUtils.formatConsent(content))
            } else {
                val content = houseHoldRepository.getConsentForm()?.content ?: ""
                val formattedConsent = CommonUtils.formatConsent(content)
                val userProfile = SecuredPreference.getUserDetails()
                val name = context.getString(
                    R.string.firstname_lastname,
                    userProfile?.firstName.textOrEmpty(),
                    userProfile?.lastName.textOrEmpty(),
                )
                val tnc = formattedConsent.replace("##LOGGEDIN_USER_NAME##", name)
                termsAndConditionStringLiveData.postValue(tnc)
            }
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
