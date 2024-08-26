package com.medtroniclabs.spice.ncd.screening.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.medtroniclabs.spice.db.entity.HealthFacilityEntity
import com.medtroniclabs.spice.ncd.data.SiteDetails
import com.medtroniclabs.spice.ncd.screening.repo.ScreeningRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GeneralDetailsViewModel @Inject constructor(
    private val screeningRepository: ScreeningRepository
) : ViewModel() {
    val siteDetail = SiteDetails()
    private var getSites = MutableLiveData<Boolean>()
    val getSitesLiveData: LiveData<List<HealthFacilityEntity>> =
        getSites.switchMap {
            screeningRepository.getUserHealthFacilityEntity()
        }

    fun getSites(isTrigger: Boolean) {
        getSites.value = isTrigger
    }
}