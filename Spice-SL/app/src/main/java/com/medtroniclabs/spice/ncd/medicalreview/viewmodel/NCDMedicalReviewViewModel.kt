package com.medtroniclabs.spice.ncd.medicalreview.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.app.analytics.model.UserDetail
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.db.entity.NCDDiagnosisEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.ncd.data.BadgeNotificationModel
import com.medtroniclabs.spice.ncd.data.MedicalReviewRequestResponse
import com.medtroniclabs.spice.ncd.data.MedicalReviewResponse
import com.medtroniclabs.spice.ncd.medicalreview.repo.NCDMedicalReviewRepository
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NCDMedicalReviewViewModel @Inject constructor(
    private var ncdMedicalReviewRepo: NCDMedicalReviewRepository,
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher
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

    fun createNCDMedicalReview(request: MedicalReviewRequestResponse, menuId: String? = null,initialMr:String) {
        viewModelScope.launch(dispatcherIO) {
            createMedicalReview.postLoading()
            setAnalyticsData(
                UserDetail.startDateTime,
                eventName = "$initialMr $menuId",
                isCompleted = true
            )
            createMedicalReview.postValue(
                ncdMedicalReviewRepo.createNCDMedicalReview(request)
            )
        }
    }

    fun getBadgeNotifications(request: BadgeNotificationModel) {
        viewModelScope.launch(dispatcherIO) {
            getBadgeNotificationLiveData.postLoading()
            getBadgeNotificationLiveData.postValue(
                ncdMedicalReviewRepo.getBadgeNotifications(request)
            )
        }
    }

    fun updateBadgeNotifications(request: BadgeNotificationModel) {
        viewModelScope.launch(dispatcherIO) {
            updateBadgeNotificationLiveData.postLoading()
            updateBadgeNotificationLiveData.postValue(
                ncdMedicalReviewRepo.updateBadgeNotifications(request)
            )
        }
    }

    fun ncdPatientDiagnosisStatus(request: HashMap<String, Any>) {
        viewModelScope.launch(dispatcherIO) {
            ncdPatientDiagnosisStatus.postLoading()
            ncdPatientDiagnosisStatus.postValue(
                ncdMedicalReviewRepo.ncdPatientDiagnosisStatus(
                    request
                )
            )
        }
    }
}