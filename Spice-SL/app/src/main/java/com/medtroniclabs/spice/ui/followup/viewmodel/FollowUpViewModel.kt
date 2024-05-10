package com.medtroniclabs.spice.ui.followup.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.FollowUpPatientModel
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.db.entity.VillageEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.followup.FollowUpFilter
import com.medtroniclabs.spice.repo.FollowUpRepository
import com.medtroniclabs.spice.ui.followup.FollowUpDefinedParams
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

    private val villages = mutableListOf<VillageEntity>()
    private val filterLiveData = MutableLiveData<FollowUpFilter>()
    val followUpPatientListLiveData: LiveData<List<FollowUpPatientModel>> =
        filterLiveData.switchMap {
            followUpRepository.getFollowUpListLiveData(it)
        }

    init {
        viewModelScope.launch {
            villages.addAll(followUpRepository.getVillageIds())
            filterLiveData.postValue(
                FollowUpFilter(
                    type = DefinedParams.FU_TYPE_HH_VISIT,
                    villages = villages.map { it.id })
            )
        }
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
            1 -> DefinedParams.FU_TYPE_REFERRED
            2 -> DefinedParams.FU_TYPE_MEDICAL_REVIEW
            else -> DefinedParams.FU_TYPE_HH_VISIT
        }
    }

    fun getVillages(): List<VillageEntity> {
        return villages
    }

    fun getFilterData(): FollowUpFilter? {
        return filterLiveData.value
    }

    fun getDateRange(): List<String> {
        return listOf(
            FollowUpDefinedParams.FilterToday,
            FollowUpDefinedParams.FilterTomorrow,
            FollowUpDefinedParams.FilterCustomize
        )
    }
}