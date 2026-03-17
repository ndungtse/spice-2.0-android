package org.medtroniclabs.uhis.ui.medicalreview.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import dagger.hilt.android.lifecycle.HiltViewModel
import org.medtroniclabs.uhis.data.MedicalReviewMetaItems
import org.medtroniclabs.uhis.data.model.ChipViewItemModel
import org.medtroniclabs.uhis.ui.medicalreview.motherneonate.anc.viewmodel.PhysicalExaminationRepository
import javax.inject.Inject

@HiltViewModel
class PhysicalExaminationViewModel @Inject constructor(
    private var repository: PhysicalExaminationRepository,
) : ViewModel() {
    var congenitalDefect: Boolean? = null
    var exclusiveBreastFeeding: Boolean? = null
    var breastFeeding: Boolean? = null
    var selectedSystemicExaminations = ArrayList<ChipViewItemModel>()
    var cordExaminationMap = HashMap<String, Any>()
    val congenitalDefectMap = HashMap<String, Any>()
    val breastCondition = HashMap<String, Any>()
    val exclusiveBreastCondition = HashMap<String, Any>()
    private val systematicType = MutableLiveData<String>()

    val systemicExaminationListLiveData: LiveData<List<MedicalReviewMetaItems>> =
        systematicType.switchMap {
            val result = repository.getExaminationsComplaintByTypeLiveData(it)
            result
        }

    fun setType(category: String) {
        systematicType.value = category
    }
}
