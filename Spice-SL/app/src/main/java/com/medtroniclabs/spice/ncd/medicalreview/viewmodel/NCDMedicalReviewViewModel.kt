package com.medtroniclabs.spice.ncd.medicalreview.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.db.entity.NCDDiagnosisEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.ncd.data.BadgeNotificationModel
import com.medtroniclabs.spice.ncd.data.MedicalReviewRequestResponse
import com.medtroniclabs.spice.ncd.data.MedicalReviewResponse
import com.medtroniclabs.spice.ncd.medicalreview.repo.NCDMedicalReviewRepository
import com.medtroniclabs.spice.network.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import javax.inject.Inject

@HiltViewModel
class NCDMedicalReviewViewModel @Inject constructor(
    private var ncdMedicalReviewRepo: NCDMedicalReviewRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {

    val ncdMedicalReviewStaticLiveData = MutableLiveData<Resource<Boolean>>()
    val createMedicalReview = MutableLiveData<Resource<MedicalReviewResponse>>()
    var validationForStatus: List<NCDDiagnosisEntity>? = null
    var statusDiabetesValue: String? = null

    val getBadgeNotificationLiveData = MutableLiveData<Resource<BadgeNotificationModel>>()
    val updateBadgeNotificationLiveData = MutableLiveData<Resource<Boolean>>()

    fun getStaticMetaData() {
        viewModelScope.launch(dispatcherIO) {
            ncdMedicalReviewStaticLiveData.postLoading()
            ncdMedicalReviewStaticLiveData.postValue(ncdMedicalReviewRepo.getNcdMedicalReviewStaticData())
        }
    }

    fun createNCDMedicalReview(request: MedicalReviewRequestResponse) {
        viewModelScope.launch(dispatcherIO) {
            createMedicalReview.postLoading()
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
}