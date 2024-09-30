package com.medtroniclabs.spice.ncd.medicalreview.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.db.entity.NCDMedicalReviewMetaEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.Comorbidity
import com.medtroniclabs.spice.ncd.medicalreview.repo.NCDMedicalReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class NCDComorbiditiesViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val ncdMedicalReviewRepository: NCDMedicalReviewRepository
) :
    ViewModel() {

    var chips: ArrayList<ChipViewItemModel> = ArrayList()
    private val getChip = MutableLiveData<String?>()
    val getChipItems: LiveData<List<NCDMedicalReviewMetaEntity>> = getChip.switchMap {
        ncdMedicalReviewRepository.getComorbiditiesBasedOnType(it, Comorbidity)
    }
    var comments: String = ""
    fun getChips(type: String?) {
        getChip.value = type
    }

}