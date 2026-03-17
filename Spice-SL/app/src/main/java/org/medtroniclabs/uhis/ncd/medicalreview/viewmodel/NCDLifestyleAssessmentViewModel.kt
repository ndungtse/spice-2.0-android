package org.medtroniclabs.uhis.ncd.medicalreview.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import dagger.hilt.android.lifecycle.HiltViewModel
import org.medtroniclabs.uhis.db.entity.LifeStyleUIModel
import org.medtroniclabs.uhis.db.entity.LifestyleEntity
import org.medtroniclabs.uhis.ncd.data.InitialLifeStyle
import org.medtroniclabs.uhis.ncd.medicalreview.repo.NCDMedicalReviewRepository
import javax.inject.Inject

@HiltViewModel
class NCDLifestyleAssessmentViewModel @Inject constructor(
    private val ncdMedicalReviewRepository: NCDMedicalReviewRepository,
) : ViewModel() {
    var lifestyle: ArrayList<InitialLifeStyle>? = null
    private val getLifestyleAssessment = MutableLiveData<Boolean>()
    val getLifeStyleLiveData: LiveData<List<LifestyleEntity>> = getLifestyleAssessment.switchMap {
        ncdMedicalReviewRepository.getLifeStyle()
    }
    var lifeStyleListUIModel: List<LifeStyleUIModel>? = null

    fun getLifestyleAssessment(isTrigger: Boolean) {
        getLifestyleAssessment.value = isTrigger
    }
}
