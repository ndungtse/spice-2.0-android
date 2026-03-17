package org.medtroniclabs.uhis.ncd.medicalreview.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.medtroniclabs.uhis.app.analytics.model.UserDetail
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsDefinedParams
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.data.APIResponse
import org.medtroniclabs.uhis.db.entity.TreatmentPlanEntity
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.ncd.data.NCDTreatmentPlanModel
import org.medtroniclabs.uhis.ncd.data.NCDTreatmentPlanModelDetails
import org.medtroniclabs.uhis.ncd.medicalreview.repo.NCDTreatmentPlanRepo
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.ui.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class NCDTreatmentPlanViewModel @Inject constructor(
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
    private val ncdTreatmentPlanRepo: NCDTreatmentPlanRepo,
) : BaseViewModel(dispatcherIO) {
    var patientReference: String? = null
    var memberReference: String? = null
    var carePlanId: String? = null

    var updateNCDTreatmentPlanLiveData =
        MutableLiveData<Resource<APIResponse<NCDTreatmentPlanModel>>>()
    var getNCDTreatmentPlanLiveData =
        MutableLiveData<Resource<APIResponse<NCDTreatmentPlanModelDetails>>>()

    var medicalReviewFrequency: TreatmentPlanEntity? = null
    var bpCheckFrequency: TreatmentPlanEntity? = null
    var bgCheckFrequency: TreatmentPlanEntity? = null
    var hba1cCheckFrequency: TreatmentPlanEntity? = null
    var choCheckFrequency: TreatmentPlanEntity? = null

    private var frequencies = MutableLiveData<Boolean>()
    val allFrequencies: LiveData<List<TreatmentPlanEntity>> =
        frequencies.switchMap { ncdTreatmentPlanRepo.getFrequencies() }

    fun getFrequencies() {
        frequencies.value = true
    }

    fun getNCDTreatmentPlan(request: NCDTreatmentPlanModelDetails) {
        viewModelScope.launch(dispatcherIO) {
            getNCDTreatmentPlanLiveData.postLoading()
            getNCDTreatmentPlanLiveData.postValue(
                ncdTreatmentPlanRepo.getNCDTreatmentPlan(
                    request,
                ),
            )
        }
    }

    fun updateNCDTreatmentPlan(request: NCDTreatmentPlanModel) {
        viewModelScope.launch(dispatcherIO) {
            updateNCDTreatmentPlanLiveData.postLoading()
            setAnalyticsData(
                UserDetail.startDateTime,
                eventName = AnalyticsDefinedParams.NCDTreatmentPlanCreation,
                isCompleted = true,
            )
            updateNCDTreatmentPlanLiveData.postValue(
                ncdTreatmentPlanRepo.updateNCDTreatmentPlan(
                    request,
                ),
            )
        }
    }
}
