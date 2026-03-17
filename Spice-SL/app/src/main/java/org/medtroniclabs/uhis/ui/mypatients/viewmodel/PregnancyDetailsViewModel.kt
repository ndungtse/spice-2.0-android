package org.medtroniclabs.uhis.ui.mypatients.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.medtroniclabs.uhis.data.MedicalReviewMetaItems
import org.medtroniclabs.uhis.data.PregnancyDetailsModel
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.ui.BaseViewModel
import org.medtroniclabs.uhis.ui.medicalreview.motherneonate.anc.repo.MotherNeonateANCRepo
import org.medtroniclabs.uhis.ui.medicalreview.utils.MedicalReviewTypeEnums
import javax.inject.Inject

@HiltViewModel
class PregnancyDetailsViewModel @Inject constructor(
    private val motherNeonateANCRepo: MotherNeonateANCRepo,
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
) : BaseViewModel(dispatcherIO) {
    val pregnancyDetailsModel: PregnancyDetailsModel = PregnancyDetailsModel()
    val checkSubmitBtn = MutableLiveData<Boolean>()
    private val getAncMetaForBloodGroup = MutableLiveData<String>()
    val ancMetaLiveDataForBloodGroup: LiveData<List<MedicalReviewMetaItems>> =
        getAncMetaForBloodGroup.switchMap {
            motherNeonateANCRepo.getExaminationsComplaintsForAnc(it, MedicalReviewTypeEnums.ANC_REVIEW.name)
        }

    fun checkSubmitBtn() {
        checkSubmitBtn.value = true
    }

    fun setAncReqToGetMetaForBloodGroup(category: String) {
        getAncMetaForBloodGroup.value = category
    }

    private val _sharedValueLmp = MutableStateFlow("")
    val sharedValueLmp: StateFlow<String> = _sharedValueLmp.asStateFlow()

    fun setSharedValue(value: String) {
        _sharedValueLmp.value = value
    }
}
