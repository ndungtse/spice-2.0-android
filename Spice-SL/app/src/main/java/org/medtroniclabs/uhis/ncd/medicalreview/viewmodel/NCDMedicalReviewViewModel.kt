package org.medtroniclabs.uhis.ncd.medicalreview.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import org.medtroniclabs.uhis.app.analytics.model.UserDetail
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.db.entity.NCDDiagnosisEntity
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.ncd.data.BadgeNotificationModel
import org.medtroniclabs.uhis.ncd.data.MedicalReviewRequestResponse
import org.medtroniclabs.uhis.ncd.data.MedicalReviewResponse
import org.medtroniclabs.uhis.ncd.medicalreview.repo.NCDMedicalReviewRepository
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NCDMedicalReviewViewModel @Inject constructor(
    private var ncdMedicalReviewRepo: NCDMedicalReviewRepository,
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
) : BaseViewModel(dispatcherIO) {
    val ncdPatientDiagnosisStatus = MutableLiveData<Resource<HashMap<String, Any>>>()

    val ncdMedicalReviewStaticLiveData = MutableLiveData<Resource<Boolean>>()
    val createMedicalReview = MutableLiveData<Resource<MedicalReviewResponse>>()
    var validationForStatus: List<NCDDiagnosisEntity>? = null
    var statusDiabetesValue: String? = null

    val getBadgeNotificationLiveData = MutableLiveData<Resource<BadgeNotificationModel>>()
    val updateBadgeNotificationLiveData = MutableLiveData<Resource<Boolean>>()

    var isPatientStatusCompleted = false

    fun getStaticMetaData() {
        viewModelScope.launch(dispatcherIO) {
            ncdMedicalReviewStaticLiveData.postLoading()
            ncdMedicalReviewStaticLiveData.postValue(ncdMedicalReviewRepo.getNcdMedicalReviewStaticData())
        }
    }

    fun createNCDMedicalReview(
        request: MedicalReviewRequestResponse,
        menuId: String? = null,
        initialMr: String,
    ) {
        viewModelScope.launch(dispatcherIO) {
            createMedicalReview.postLoading()
            setAnalyticsData(
                UserDetail.startDateTime,
                eventName = "$initialMr $menuId",
                isCompleted = true,
            )
            createMedicalReview.postValue(
                ncdMedicalReviewRepo.createNCDMedicalReview(request),
            )
        }
    }

    fun getBadgeNotifications(request: BadgeNotificationModel) {
        viewModelScope.launch(dispatcherIO) {
            getBadgeNotificationLiveData.postLoading()
            getBadgeNotificationLiveData.postValue(
                ncdMedicalReviewRepo.getBadgeNotifications(request),
            )
        }
    }

    fun updateBadgeNotifications(request: BadgeNotificationModel) {
        viewModelScope.launch(dispatcherIO) {
            updateBadgeNotificationLiveData.postLoading()
            updateBadgeNotificationLiveData.postValue(
                ncdMedicalReviewRepo.updateBadgeNotifications(request),
            )
        }
    }

    fun ncdPatientDiagnosisStatus(request: HashMap<String, Any>) {
        viewModelScope.launch(dispatcherIO) {
            ncdPatientDiagnosisStatus.postLoading()
            ncdPatientDiagnosisStatus.postValue(
                ncdMedicalReviewRepo.ncdPatientDiagnosisStatus(
                    request,
                ),
            )
        }
    }
}
