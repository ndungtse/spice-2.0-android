package org.medtroniclabs.uhis.ui.mypatients.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import org.medtroniclabs.uhis.data.MedicalReviewMetaItems
import org.medtroniclabs.uhis.data.model.ChipViewItemModel
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.ui.medicalreview.motherneonate.anc.repo.MotherNeonateANCRepo
import org.medtroniclabs.uhis.ui.medicalreview.utils.MedicalReviewTypeEnums
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class PregnancyPastObstetricHistoryViewModel@Inject constructor(
    private val motherNeonateANCRepo: MotherNeonateANCRepo,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
) : ViewModel() {
    var pregnancyHistoryNotes: String? = null
    val resultFlowHashMap = HashMap<String, Any>()
    var pregnancyHistoryOther: ChipViewItemModel? = null
    var deliveryKit: Boolean? = null
    var checkSubmitBtn = MutableLiveData<Boolean>()
    var pregnancyHistoryChip: ArrayList<ChipViewItemModel> = ArrayList()
    private val getAncMetaForPregnancyHistory = MutableLiveData<String>()
    val ancMetaLiveDataForPregnancyHistory: LiveData<List<MedicalReviewMetaItems>> = getAncMetaForPregnancyHistory.switchMap {
        motherNeonateANCRepo.getExaminationsComplaintsForAnc(it, MedicalReviewTypeEnums.ANC_REVIEW.name)
    }

    fun setAncReqToGetMetaForPregnancyHistory(category: String) {
        getAncMetaForPregnancyHistory.value = category
    }

    fun checkSubmitBtn() {
        checkSubmitBtn.value = true
    }
}
