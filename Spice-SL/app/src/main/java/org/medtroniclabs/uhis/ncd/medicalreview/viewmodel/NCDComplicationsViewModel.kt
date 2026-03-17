package org.medtroniclabs.uhis.ncd.medicalreview.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import org.medtroniclabs.uhis.data.model.ChipViewItemModel
import org.medtroniclabs.uhis.db.entity.NCDMedicalReviewMetaEntity
import org.medtroniclabs.uhis.ncd.medicalreview.NCDMRUtil.Complications
import org.medtroniclabs.uhis.ncd.medicalreview.repo.NCDMedicalReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NCDComplicationsViewModel @Inject constructor(
    private val ncdMedicalReviewRepository: NCDMedicalReviewRepository,
) : ViewModel() {
    var chips: ArrayList<ChipViewItemModel> = ArrayList()
    private val getChip = MutableLiveData<Boolean>()
    val getChipItems: LiveData<List<NCDMedicalReviewMetaEntity>> = getChip.switchMap {
        ncdMedicalReviewRepository.getComorbiditiesBasedOnType(category = Complications)
    }
    var comments: String = ""

    fun getChips() {
        getChip.value = true
    }
}
