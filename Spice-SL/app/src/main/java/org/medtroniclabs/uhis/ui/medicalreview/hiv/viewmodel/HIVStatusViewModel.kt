package org.medtroniclabs.uhis.ui.medicalreview.hiv.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.data.MedicalReviewMetaItems
import org.medtroniclabs.uhis.data.model.HivStatus
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.ui.BaseViewModel
import org.medtroniclabs.uhis.ui.medicalreview.hiv.repo.HivMedicalReviewRepo
import javax.inject.Inject

@HiltViewModel
class HIVStatusViewModel @Inject constructor(
    private val hivMedicalReviewRepo: HivMedicalReviewRepo,
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
) : BaseViewModel(dispatcherIO) {
    val resultPregnantStatus = HashMap<String, Any>()
    val resultAHD = HashMap<String, Any>()
    val resultDSD = HashMap<String, Any>()
    val getHivStatusMetaList = MutableLiveData<Resource<List<MedicalReviewMetaItems>>>()
    var selectModel: String? = null
    val request: HivStatus? = null
    var tbStatus: String? = null
    var isEMTCT: Boolean = false

    fun getHivStatusMeta(type: String) {
        viewModelScope.launch(dispatcherIO) {
            getHivStatusMetaList.postLoading()
            getHivStatusMetaList.postValue(hivMedicalReviewRepo.getComplaintsListByType(type))
        }
    }
}
