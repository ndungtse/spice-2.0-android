package org.medtroniclabs.uhis.ncd.medicalreview.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import dagger.hilt.android.lifecycle.HiltViewModel
import org.medtroniclabs.uhis.data.model.ChipViewItemModel
import org.medtroniclabs.uhis.db.entity.NCDMedicalReviewMetaEntity
import org.medtroniclabs.uhis.ncd.medicalreview.NCDMRUtil.PhysicalExamination
import org.medtroniclabs.uhis.ncd.medicalreview.repo.NCDMedicalReviewRepository
import javax.inject.Inject

@HiltViewModel
class NCDObstetricExaminationViewModel @Inject constructor(
    private val ncdMedicalReviewRepository: NCDMedicalReviewRepository,
) : ViewModel() {
    var comments: String = ""
    var chips: ArrayList<ChipViewItemModel> = ArrayList()
    private val getChip = MutableLiveData<String?>()
    val getChipItems: LiveData<List<NCDMedicalReviewMetaEntity>> = getChip.switchMap {
        ncdMedicalReviewRepository.getComorbiditiesBasedOnType(it, PhysicalExamination)
    }

    fun getChips(type: String?) {
        getChip.value = type
    }
}
