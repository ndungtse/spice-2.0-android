package com.medtroniclabs.spice.ui.boarding.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.utils.ConnectivityManager
import com.medtroniclabs.spice.repo.OfflineSyncRepository
import com.medtroniclabs.spice.ui.boarding.repo.MetaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResourceLoadingViewModel @Inject constructor(
    private val metaRepository: MetaRepository,
    private val offlineSyncRepository: OfflineSyncRepository,
    private val connectivityManager: ConnectivityManager,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {

    val metaDataCompleteLiveData = MutableLiveData<Resource<Boolean>>()
    val householdsLiveData = MutableLiveData<Resource<Boolean>>()

    private val workflowNames = mutableListOf<Long>()
    private val meta = mutableListOf<String>()
    private val syncDelay = 20 * 1000L // 20 Sec

    init {
        getMetaDataInformation()
    }

    fun getMetaDataInformation() {
        viewModelScope.launch(dispatcherIO) {
            metaDataCompleteLiveData.postLoading()
            if (!connectivityManager.isNetworkAvailable()) {
                metaDataCompleteLiveData.postError()
                return@launch
            }
            metaDataCompleteLiveData.postValue(metaRepository.getMetaDataInformation(workflowNames,meta))
        }
    }

    fun downloadInitialDetails() {
        viewModelScope.launch(dispatcherIO) {
            householdsLiveData.postLoading()
            // 1. Check any existing UUID in Pref
            // 2. If exists -> Start getting status of existing uuid with retry
            if (!getSyncStatus()) {
                householdsLiveData.postError()
                return@launch
            }

            // 3. Post local change with new UUID
            // 4. Get FHIR Id with new UUID
            if (!postLocalChanges()) {
                return@launch
            }

            // 5. Check village change check
            if (!checkAndProceedVillageChange()) {
                return@launch
            }

            // 6. GET Fetch synced data - Update recent
            offlineSyncRepository.getInsertOrUpdateLocalData(householdsLiveData)
        }
    }


    private suspend fun getSyncStatus(): Boolean {
        val requestIds =
            SecuredPreference.getStringArray(SecuredPreference.EnvironmentKey.OFFLINE_SYNC_REQUEST_ID.name)
        if (requestIds.isNullOrEmpty()) {
            return true
        }

        val uuid = requestIds[0]

        repeat(4) {
            if (offlineSyncRepository.getSyncStatusForOffline(uuid)) {
                SecuredPreference.remove(SecuredPreference.EnvironmentKey.OFFLINE_SYNC_REQUEST_ID.name)
                return true
            }
            delay(syncDelay)
        }

        return false
    }


    private suspend fun postLocalChanges(): Boolean {
        val requestIds = offlineSyncRepository.postOfflineUnSyncedChanges()
        if (requestIds == null) {
            householdsLiveData.postError()
            return false
        }

        //Save request id in Preference
        if (requestIds.isNotEmpty()) {
            SecuredPreference.saveStringArray(
                SecuredPreference.EnvironmentKey.OFFLINE_SYNC_REQUEST_ID.name,
                requestIds.toTypedArray()
            )

            // 4. Start getting status of new uuid with retry
            delay(syncDelay)
            if (!getSyncStatus()) {
                householdsLiveData.postError()
                return false
            }
        }

        return true
    }

    private suspend fun checkAndProceedVillageChange(): Boolean {
        val prefKey = SecuredPreference.EnvironmentKey.VILLAGE_IDS.name
        val oldVillageIds = SecuredPreference.getLongList(prefKey)
        val newVillageIds = metaRepository.getAllVillageIds()
        if (oldVillageIds.isNotEmpty()) {
            val changedVillage = oldVillageIds.subtract(newVillageIds.toSet())
            val newlyAddedVillage = newVillageIds.subtract(oldVillageIds.toSet())
            // Existing village changed remove all data and fresh download for all villages
            if (changedVillage.isNotEmpty()) {
                SecuredPreference.remove(SecuredPreference.EnvironmentKey.LAST_SYNCED_AT.name)
            } else if (newlyAddedVillage.isNotEmpty()) {
                if (!offlineSyncRepository.fetchSyncedData(newlyAddedVillage.toList(), 0L)) {
                    householdsLiveData.postError()
                    return false
                }
            }
        }

        SecuredPreference.saveLongList(prefKey, newVillageIds)
        return true
    }

}