package com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.HivClinicalInfoResponse
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.data.WhoClinicalStageCreateRequest
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.data.model.MedicalReviewEncounter
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.medicalreview.hiv.repo.HivMedicalReviewRepo
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WhoClinicalStageViewModel @Inject constructor(
    private val hivMedicalReviewRepo: HivMedicalReviewRepo,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
) : ViewModel() {
    var lastLocation: Location? = null
    var whoStageChip: ArrayList<ChipViewItemModel> = ArrayList()
    val whoStageCreateLiveData = MutableLiveData<Resource<HivClinicalInfoResponse>>()
    val whoValue: String? = null

    private val getMeta = MutableLiveData<String>()
    val getWhoStageLiveData: LiveData<List<MedicalReviewMetaItems>> =
        getMeta.switchMap {
            hivMedicalReviewRepo.getExaminationsComplaints(it, MedicalReviewTypeEnums.HIV.name)
        }

    fun setWhoStage(category: String) {
        getMeta.value = category
    }

    fun createWhoClinicalStage(encounter: MedicalReviewEncounter) {
        viewModelScope.launch(dispatcherIO) {
            whoStageCreateLiveData.postLoading()
            whoStageCreateLiveData.postValue(
                hivMedicalReviewRepo.createWhoClinicalStage(
                    WhoClinicalStageCreateRequest(
                        encounter = encounter,
                        stringValue = whoStageChip[0].value ?: "",
                    ),
                ),
            )
        }
    }
}
