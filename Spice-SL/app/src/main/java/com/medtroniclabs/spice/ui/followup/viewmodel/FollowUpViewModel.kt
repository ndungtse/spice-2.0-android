package com.medtroniclabs.spice.ui.followup.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.FollowUpPatientModel
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.followup.FollowUpFilter
import com.medtroniclabs.spice.repo.FollowUpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class FollowUpViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val followUpRepository: FollowUpRepository,
) : ViewModel() {

    val callResultHashMap = HashMap<String, Any>()
    val patientStatusHashMap = HashMap<String, Any>()
    val unSuccessfulHashMap = HashMap<String, Any>()


    private val filterLiveData = MutableLiveData<FollowUpFilter>()
    val followUpPatientListLiveData: LiveData<List<FollowUpPatientModel>> =
        filterLiveData.switchMap {
            followUpRepository.getFollowUpListLiveData(it)
        }

    init {
        filterLiveData.value = FollowUpFilter(type = DefinedParams.FU_TYPE_HH_VISIT)
    }

    fun updateFollowUpFilter(type: Int) {
        val filter = filterLiveData.value
        filter?.let {
            //Update Page
            it.type = getFollowUpType(type)

            filterLiveData.value = it
        }
    }

    private fun getFollowUpType(type: Int): String {
        return when (type) {
            1 -> DefinedParams.FU_TYPE_REFERRED
            2 -> DefinedParams.FU_TYPE_MEDICAL_REVIEW
            else -> DefinedParams.FU_TYPE_HH_VISIT
        }
    }
}