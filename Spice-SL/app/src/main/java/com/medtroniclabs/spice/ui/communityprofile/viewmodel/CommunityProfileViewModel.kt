package com.medtroniclabs.spice.ui.communityprofile.viewmodel

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.community.CommunityPopulationStatistics
import com.medtroniclabs.spice.data.community.CommunityProfileDetail
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineSyncStatus
import com.medtroniclabs.spice.db.entity.CommunityProfile
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.formgeneration.model.FormResponse
import com.medtroniclabs.spice.model.communityprofile.CommunityProfileDetails
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.CommunityProfileRepository
import com.medtroniclabs.spice.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityProfileViewModel @Inject constructor(
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
    private val repository: CommunityProfileRepository
) : BaseViewModel(dispatcherIO) {
    var chwHome = ""
    var isChwHome = false
    var nearestPhu = ""
    var isNetworkCoverage = false
    var networkName = ""
    var isOtherNetwork = false
    var otherNetwork = ""
    var currentFragment = MutableLiveData<Pair<Int, Bundle?>>()
    var communityStatistics = MutableLiveData<Resource<CommunityPopulationStatistics>>()
    var searchFilter = MutableLiveData<String>().apply { value = value ?: "" }
    val formLayoutLiveData = MutableLiveData<Resource<FormResponse>>()
    val nearestHealthFacilityLiveData = MutableLiveData<Resource<ArrayList<Map<String, Any>>>>()
    var marketDays = mutableListOf<Pair<String,Boolean>>()
    var selectedNetworks = mutableListOf<Pair<String,Boolean>>()
    val getCommunityDetailsLiveDataLocal = MutableLiveData<Resource<CommunityProfile>>()
    var saveCommunityDetailsLiveDataLocal = MutableLiveData<Resource<Long>>()
    val combinedLiveData = MediatorLiveData<Pair<
            Resource<CommunityPopulationStatistics>?,
            Resource<CommunityProfile>?
            >>().apply {
        addSource(communityStatistics) { stats ->
            value = Pair(stats, getCommunityDetailsLiveDataLocal.value)
        }
        addSource(getCommunityDetailsLiveDataLocal) { details ->
            value = Pair(communityStatistics.value, details)
        }
    }

    fun reinitSaveLiveData() {
        saveCommunityDetailsLiveDataLocal = MutableLiveData<Resource<Long>>()
    }

    fun updateCurrentFragment(fragment: Int, bundle: Bundle? = null) {
        val value = Pair(fragment, bundle)
        currentFragment.postValue(value)
    }

    fun getPopulationStatistics(villageId: Long) {
        viewModelScope.launch(dispatcherIO) {
            communityStatistics.postValue(
                repository.getCommunityStatistics(villageId)
            )
        }
    }

    val searchFilterLiveData: LiveData<List<CommunityProfileDetail>> =
        searchFilter.switchMap { search ->
            repository.getFilterVillageWithHouseholds(
                searchText = search
            )
        }

    fun setSearchFilter(search: String) {
        searchFilter.value = search
    }

    fun getSearchFilter(): String? {
        return searchFilter.value
    }

    fun getFormData(type: String) {
        viewModelScope.launch(dispatcherIO) {
            formLayoutLiveData.postValue(
                repository.getFormData(type)
            )
        }
    }

    fun getNearestHealthFacility(villageId: Long? = null) {
        viewModelScope.launch(dispatcherIO) {
            nearestHealthFacilityLiveData.postValue(
                repository.getNearestHealthFacility(villageId)
            )
        }
    }

    /* fun createCommunity(request:HashMap<String,Any>){
         viewModelScope.launch(dispatcherIO) {
             createCommunityLiveData.postLoading()
             createCommunityLiveData.postValue(
                 repository.createCommunityProfile(request)
             )
         }
     }

     fun getCommunityDetails(villageId:Long){
         viewModelScope.launch(dispatcherIO) {
             getCommunityDetailsLiveData.postValue(
                 repository.getCommunityProfileDetails(villageId)
             )
         }
     }

     fun updateCommunityDetails(request: HashMap<String, Any>){
         viewModelScope.launch(dispatcherIO) {
             repository.updateCommunityProfile(request)
         }
     }*/

    fun insertOrUpdateCommunityProfile(
        villageId: Long,
        description: String,
        regDate: String,
        payload: String
    ) {
        viewModelScope.launch(dispatcherIO) {
            saveCommunityDetailsLiveDataLocal.postLoading()
            saveCommunityDetailsLiveDataLocal.postValue(
                repository.insertOrUpdateCommunityProfile(
                    villageId,
                    description, regDate, payload,
                    getCommunityDetailsLiveDataLocal
                )
            )
        }
    }

    fun getCommunityDetailsLocal(villageId: Long) {
        viewModelScope.launch(dispatcherIO) {
            getCommunityDetailsLiveDataLocal.postLoading()
            getCommunityDetailsLiveDataLocal.postValue(
                repository.getCommunityProfileDetails(villageId)
            )
        }
    }

    fun updateUnSynStatus(villageId: Long){
        viewModelScope.launch(dispatcherIO) {
            repository.updateUnSynStatus(villageId, OfflineSyncStatus.NotSynced.name)
        }
    }
}