package com.medtroniclabs.spice.ncd.medicalreview.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.db.entity.NCDMedicalReviewMetaEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.Complications
import com.medtroniclabs.spice.ncd.medicalreview.repo.NCDMedicalReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class NCDComplicationsViewModel  @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val ncdMedicalReviewRepository: NCDMedicalReviewRepository
) :
    ViewModel() {

    var chips:ArrayList<ChipViewItemModel> = ArrayList()
    private val getChip = MutableLiveData<Boolean>()
    val getChipItems: LiveData<List<NCDMedicalReviewMetaEntity>> = getChip.switchMap {
        ncdMedicalReviewRepository.getComorbiditiesBasedOnType(category = Complications)
    }
    var comments: String = ""
    fun getChips() {
        getChip.value = true
    }

}