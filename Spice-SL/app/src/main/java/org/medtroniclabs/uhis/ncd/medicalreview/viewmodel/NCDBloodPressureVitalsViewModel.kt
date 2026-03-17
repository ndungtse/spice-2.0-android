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
import org.medtroniclabs.uhis.data.model.ChipViewItemModel
import org.medtroniclabs.uhis.db.entity.RiskFactorEntity
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.formgeneration.model.BPModel
import org.medtroniclabs.uhis.formgeneration.model.FormLayout
import org.medtroniclabs.uhis.mappingkey.Screening
import org.medtroniclabs.uhis.ncd.assessment.repo.BloodPressureRepo
import org.medtroniclabs.uhis.ncd.assessment.repo.GlucoseRepo
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.repo.AssessmentRepository
import org.medtroniclabs.uhis.ui.BaseViewModel
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class NCDBloodPressureVitalsViewModel @Inject constructor(
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
    private val bloodPressureRepo: BloodPressureRepo,
    private var assessmentRepository: AssessmentRepository,
    private val glucoseRepo: GlucoseRepo,
) : BaseViewModel(dispatcherIO) {
    val resultHashMap = HashMap<String, Any>()
    var bpLog: FormLayout? = null
    var height: FormLayout? = null
    var weight: FormLayout? = null

    private val getRiskEntityList = MutableLiveData<Boolean>()
    val getRiskEntityListLiveData: LiveData<List<RiskFactorEntity>> = getRiskEntityList.switchMap {
        bloodPressureRepo.riskFactorListing()
    }

    fun getRiskEntityList() {
        getRiskEntityList.value = true
    }

    fun getNcdFormData(
        formType: String,
        workFlow: String,
    ) {
        getNcdFormData.value = Pair(formType, workFlow)
    }

    private val getNcdFormData = MutableLiveData<Pair<String, String>>()
    val formLayoutsNcdLiveData: LiveData<String> = getNcdFormData.switchMap {
        assessmentRepository.getAssessmentFormData(it.first, it.second)
    }

    var bpLogCreateResponseLiveData = MutableLiveData<Resource<APIResponse<HashMap<String, Any>>>>()

    private var systolicAverageSummary: Int? = null
    private var diastolicAverageSummary: Int? = null

    fun getSystolicAverage(): Int? = systolicAverageSummary

    fun getDiastolicAverage(): Int? = diastolicAverageSummary

    fun createBpLog(hashMap: HashMap<String, Any>) {
        viewModelScope.launch(dispatcherIO) {
            bpLogCreateResponseLiveData.postLoading()
            setAnalyticsData(
                UserDetail.startDateTime,
                eventName = AnalyticsDefinedParams.NCDBloodPressureCreationForNurse,
                isCompleted = true,
            )
            bpLogCreateResponseLiveData.postValue(bloodPressureRepo.createBpLogForNurse(hashMap))
        }
    }

    fun calculateBPValues(
        formLayout: FormLayout,
        resultMap: Map<String, Any>,
    ) {
        formLayout.apply {
            var systolic = 0.0
            var diastolic = 0.0
            if (resultMap.containsKey(id)) {
                val actualMapList = resultMap[id]
                if (actualMapList is java.util.ArrayList<*>) {
                    var systolicEntries = 0
                    var diastolicEntries = 0
                    actualMapList.forEach { map ->
                        if (map is BPModel) {
                            map.systolic?.let {
                                systolic += it
                                systolicEntries++
                            }
                            map.diastolic?.let {
                                diastolic += it
                                diastolicEntries++
                            }
                        } else {
                            validateMap(map, Screening.Systolic)?.let {
                                systolic += it
                                systolicEntries++
                            }
                            validateMap(map, Screening.Diastolic)?.let {
                                diastolic += it
                                diastolicEntries++
                            }
                        }
                    }
                    updateAverage(
                        actualMapList,
                        systolicEntries,
                        diastolicEntries,
                        systolic,
                        diastolic,
                    )
                }
            }
        }
    }

    private fun validateMap(
        map: Any?,
        value: String,
    ): Double? = if (map is Map<*, *> && map.containsKey(value)) (map[value] as String).toDoubleOrNull() else null

    private fun updateAverage(
        actualMapList: java.util.ArrayList<*>,
        systolicEntries: Int,
        diastolicEntries: Int,
        systolic: Double,
        diastolic: Double,
    ) {
        if (actualMapList.size > 0 && systolicEntries > 0 && diastolicEntries > 0) {
            systolicAverageSummary = (systolic / systolicEntries).roundToInt()
            diastolicAverageSummary = (diastolic / diastolicEntries).roundToInt()
        }
    }

    var bloodGlucose: FormLayout? = null
    var hbA1c: FormLayout? = null
    var selectedChips: ArrayList<ChipViewItemModel> = ArrayList()
    val bgResultHashMap = HashMap<String, Any>()
    var glucoseLogCreateResponseLiveData =
        MutableLiveData<Resource<APIResponse<HashMap<String, Any>>>>()

    fun glucoseLogCreate(result: HashMap<String, Any>) {
        viewModelScope.launch(dispatcherIO) {
            glucoseLogCreateResponseLiveData.postLoading()
            setAnalyticsData(
                UserDetail.startDateTime,
                eventName = AnalyticsDefinedParams.NCDBloodGlucoseCreationForNurse,
                isCompleted = true,
            )
            glucoseLogCreateResponseLiveData.postValue(glucoseRepo.glucoseLogCreateForNurse(result))
        }
    }
}
