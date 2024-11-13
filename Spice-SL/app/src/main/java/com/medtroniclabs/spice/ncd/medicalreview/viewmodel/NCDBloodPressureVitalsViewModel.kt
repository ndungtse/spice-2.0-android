package com.medtroniclabs.spice.ncd.medicalreview.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.APIResponse
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.db.entity.RiskFactorEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.formgeneration.model.BPModel
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.mappingkey.Screening
import com.medtroniclabs.spice.ncd.assessment.repo.BloodPressureRepo
import com.medtroniclabs.spice.ncd.assessment.repo.GlucoseRepo
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.AssessmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class NCDBloodPressureVitalsViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val bloodPressureRepo: BloodPressureRepo,
    private var assessmentRepository: AssessmentRepository,
    private val glucoseRepo: GlucoseRepo
) : ViewModel() {

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

    fun getNcdFormData(formType: String, workFlow: String) {
        getNcdFormData.value = Pair(formType, workFlow)
    }
    private val getNcdFormData = MutableLiveData<Pair<String, String>>()
    val formLayoutsNcdLiveData: LiveData<String> = getNcdFormData.switchMap {
        assessmentRepository.getAssessmentFormData(it.first, it.second)
    }

    var bpLogCreateResponseLiveData = MutableLiveData<Resource<APIResponse<HashMap<String, Any>>>>()

    private var systolicAverageSummary: Int? = null
    private var diastolicAverageSummary: Int? = null

    fun getSystolicAverage(): Int? {
        return systolicAverageSummary
    }

    fun getDiastolicAverage(): Int? {
        return diastolicAverageSummary
    }

    fun createBpLog(
        hashMap: HashMap<String, Any>
    ) {
        viewModelScope.launch(dispatcherIO) {
            bpLogCreateResponseLiveData.postLoading()
            bpLogCreateResponseLiveData.postValue(bloodPressureRepo.createBpLogForNurse(hashMap))
        }
    }

    fun calculateBPValues(formLayout: FormLayout, resultMap: Map<String, Any>) {
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
                        actualMapList, systolicEntries, diastolicEntries, systolic, diastolic
                    )
                }
            }
        }
    }

    private fun validateMap(map: Any?, value: String): Double? {
        return if (map is Map<*, *> && map.containsKey(value)) (map[value] as String).toDoubleOrNull() else null
    }

    private fun updateAverage(
        actualMapList: java.util.ArrayList<*>,
        systolicEntries: Int,
        diastolicEntries: Int,
        systolic: Double,
        diastolic: Double
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
            glucoseLogCreateResponseLiveData.postValue(glucoseRepo.glucoseLogCreateForNurse(result))
        }
    }
}