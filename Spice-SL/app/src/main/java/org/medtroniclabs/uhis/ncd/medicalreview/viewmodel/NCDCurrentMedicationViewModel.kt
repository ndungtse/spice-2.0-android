package org.medtroniclabs.uhis.ncd.medicalreview.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import org.medtroniclabs.uhis.data.model.ChipViewItemModel
import org.medtroniclabs.uhis.db.entity.NCDMedicalReviewMetaEntity
import org.medtroniclabs.uhis.ncd.medicalreview.NCDMRUtil
import org.medtroniclabs.uhis.ncd.medicalreview.repo.NCDMedicalReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NCDCurrentMedicationViewModel @Inject constructor(
    private val ncdMedicalReviewRepository: NCDMedicalReviewRepository,
) : ViewModel() {
    var chips: ArrayList<ChipViewItemModel> = ArrayList()
    private val getChip = MutableLiveData<Boolean>()
    val getChipItems: LiveData<List<NCDMedicalReviewMetaEntity>> = getChip.switchMap {
        ncdMedicalReviewRepository.getComorbiditiesBasedOnType(category = NCDMRUtil.CurrentMedication)
    }
    var drugAllergies: Boolean? = null
    var adheringCurrentMed: Boolean? = null
    var allergiesComment: String? = null
    var adheringMedComment: String? = null

    var comments: String = ""

    fun getChips() {
        getChip.value = true
    }
}
