package com.medtroniclabs.spice.ui.followup.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.FollowUpPatientModel
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.data.offlinesync.model.FollowUpCallStatus
import com.medtroniclabs.spice.db.entity.VillageEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.followup.FollowUpFilter
import com.medtroniclabs.spice.repo.FollowUpRepository
import com.medtroniclabs.spice.ui.assessment.referrallogic.utils.ReferralStatus
import com.medtroniclabs.spice.ui.followup.FollowUpDefinedParams
import com.medtroniclabs.spice.ui.followup.FollowUpDefinedParams.FU_TYPE_HH_VISIT
import com.medtroniclabs.spice.ui.followup.FollowUpDefinedParams.FU_TYPE_MEDICAL_REVIEW
import com.medtroniclabs.spice.ui.followup.FollowUpDefinedParams.FU_TYPE_REFERRED
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FollowUpViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val followUpRepository: FollowUpRepository
) : ViewModel() {

    val callResultHashMap = HashMap<String, Any>()
    val patientStatusHashMap = HashMap<String, Any>()
    val unSuccessfulHashMap = HashMap<String, Any>()
    var selectedFollowUpDetail: FollowUpPatientModel? = null

    private val villages = mutableListOf<VillageEntity>()
    private val filterLiveData = MutableLiveData<FollowUpFilter>()
    val followUpPatientListLiveData: LiveData<List<FollowUpPatientModel>> =
        filterLiveData.switchMap {
            followUpRepository.getFollowUpListLiveData(it)
        }

    private val maxSuccessfulCallLimit: Int
    private val maxUnSuccessfulCallLimit: Int

    init {
        val followUpCriteria = SecuredPreference.getFollowUpCriteria()
        maxSuccessfulCallLimit = followUpCriteria.successfulAttempts
        maxUnSuccessfulCallLimit = followUpCriteria.unsuccessfulAttempts

        viewModelScope.launch {
            villages.addAll(followUpRepository.getVillageIds())
            createNewFollowUpFilter(0)
        }
    }

    fun createNewFollowUpFilter(pageType: Int) {
        val filter =
            FollowUpFilter(type = getFollowUpType(pageType), villages = villages.map { it.id })
        filterLiveData.postValue(filter)
    }

    fun updateFollowUpFilter(
        pageType: Int? = null,
        search: String? = null,
        selectedVillages: List<ChipViewItemModel>? = null,
        selectedDateRange: List<ChipViewItemModel>? = null,
        fromDate: String? = null,
        toDate: String? = null
    ) {
        val filter = filterLiveData.value ?: FollowUpFilter()
        filter.apply {

            //Update Page
            pageType?.let {
                this.type = getFollowUpType(it)
            }

            //Update search
            search?.let {
                this.search = it
            }

            //Update Village Ids
            selectedVillages?.let {
                this.selectedVillages = it
            }

            selectedDateRange?.let {
                this.selectedDateRange = it
                this.fromDate = ""
                this.toDate = ""
            }

            //Update Date Filter
            fromDate?.let {
                this.fromDate = it
            }

            toDate?.let {
                this.toDate = it
            }

            filterLiveData.value = this
        }
    }

    private fun getFollowUpType(type: Int): String {
        return when (type) {
            1 -> FU_TYPE_REFERRED
            2 -> FU_TYPE_MEDICAL_REVIEW
            else -> FU_TYPE_HH_VISIT
        }
    }

    fun getVillages(): List<VillageEntity> {
        return villages
    }

    fun getFilterData(): FollowUpFilter? {
        return filterLiveData.value
    }

    fun getFilterDataLiveData(): LiveData<FollowUpFilter> {
        return filterLiveData
    }

    fun getDateRange(): List<String> {
        return listOf(
            FollowUpDefinedParams.FilterToday,
            FollowUpDefinedParams.FilterTomorrow,
            FollowUpDefinedParams.FilterCustomize
        )
    }

    fun addCallHistory() {
        viewModelScope.launch(dispatcherIO) {
            selectedFollowUpDetail?.let {
                val callStatus =
                    getCallStatus(callResultHashMap[DefinedParams.CallResult] as String)
                followUpRepository.addCallHistory(
                    maxSuccessfulCallLimit,
                    maxUnSuccessfulCallLimit,
                    it.id,
                    callStatus,
                    getPatientStatus(callStatus),
                    getUnSuccessfulReason(callStatus)
                )
            }
        }
    }

    private fun getCallStatus(status: String) : FollowUpCallStatus {
        if (status == "Successful")
            return FollowUpCallStatus.SUCCESSFUL
        return FollowUpCallStatus.UNSUCCESSFUL
    }

    private fun getPatientStatus(status: FollowUpCallStatus): String? {
        if (status == FollowUpCallStatus.SUCCESSFUL && patientStatusHashMap.isNotEmpty()) {
            return when(patientStatusHashMap[DefinedParams.PatientStatus] as String) {
                DefinedParams.OnTreatment -> ReferralStatus.OnTreatment.name
                DefinedParams.REFERRED -> ReferralStatus.Referred.name
                else -> ReferralStatus.Recovered.name
            }
        }

        return null
    }

    private fun getUnSuccessfulReason(status: FollowUpCallStatus): String? {
        if (status == FollowUpCallStatus.UNSUCCESSFUL) {
            val reason = unSuccessfulHashMap[DefinedParams.UnSuccessful] as String
            return if (reason.equals(FollowUpDefinedParams.WrongNumber, true)) {
                FollowUpDefinedParams.WRONG_NUMBER
            } else {
                FollowUpDefinedParams.UNREACHABLE
            }
        }
        return null
    }
}