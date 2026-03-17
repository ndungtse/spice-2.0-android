package org.medtroniclabs.uhis.ui.medicalreview.hiv.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.data.HivClinicalInfoResponse
import org.medtroniclabs.uhis.data.MedicalReviewMetaItems
import org.medtroniclabs.uhis.data.WhoClinicalStageCreateRequest
import org.medtroniclabs.uhis.data.model.ChipViewItemModel
import org.medtroniclabs.uhis.data.model.MedicalReviewEncounter
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.ui.medicalreview.hiv.repo.HivMedicalReviewRepo
import org.medtroniclabs.uhis.ui.medicalreview.utils.MedicalReviewTypeEnums
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
