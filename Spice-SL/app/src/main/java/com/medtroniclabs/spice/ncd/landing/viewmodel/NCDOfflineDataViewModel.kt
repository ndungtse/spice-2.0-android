package com.medtroniclabs.spice.ncd.landing.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.medtroniclabs.spice.ui.boarding.repo.MetaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NCDOfflineDataViewModel @Inject constructor(
    private val metaRepository: MetaRepository,
) : ViewModel() {
    private var toGetCount = MutableLiveData<Boolean>()
    val screeningCount: LiveData<Long> =
        toGetCount.switchMap {
            metaRepository.getUnSyncedDataCountForNCDScreening()
        }

    val assessmentType: LiveData<Long> =
        toGetCount.switchMap {
            metaRepository.getUnSyncedNCDAssessmentCount()
        }

    val followUpType: LiveData<Long> = toGetCount.switchMap {
        metaRepository.getUnSyncedNCDFollowUpCount()
    }

    fun getCountOfflineData() {
        toGetCount.value = true
    }
}