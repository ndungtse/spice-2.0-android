package com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.ui.medicalreview.hiv.repo.HivMedicalReviewRepo
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class WhoClinicalStageViewModel @Inject constructor(
    private val hivMedicalReviewRepo: HivMedicalReviewRepo,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {

    var lastLocation: Location? = null
    var whoStageChip: ArrayList<ChipViewItemModel> = ArrayList()

    private val getMeta = MutableLiveData<String>()
    val getWhoStageLiveData: LiveData<List<MedicalReviewMetaItems>> =
        getMeta.switchMap {
            hivMedicalReviewRepo.getExaminationsComplaints(it, MedicalReviewTypeEnums.HIV.name)
        }

    fun setWhoStage(category: String) {
        getMeta.value = category
    }
}