package com.medtroniclabs.spice.ncd.assessment.ui

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.common.CommonUtils.calculateAverageBloodPressure
import com.medtroniclabs.spice.common.CommonUtils.calculateBMI
import com.medtroniclabs.spice.common.CommonUtils.calculateBloodGlucose
import com.medtroniclabs.spice.common.CommonUtils.calculateCVDRiskFactor
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.data.model.RecommendedDosageListModel
import com.medtroniclabs.spice.databinding.ActivityAssessmentReadingBinding
import com.medtroniclabs.spice.db.entity.RiskClassificationModel
import com.medtroniclabs.spice.formgeneration.FormGenerator
import com.medtroniclabs.spice.formgeneration.config.ViewType
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.listener.FormEventListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.FormResultComposer
import com.medtroniclabs.spice.mappingkey.Screening
import com.medtroniclabs.spice.ncd.assessment.viewmodel.AssessmentReadingViewModel
import com.medtroniclabs.spice.ncd.assessment.viewmodel.BloodPressureViewModel
import com.medtroniclabs.spice.ncd.assessment.viewmodel.GlucoseViewModel
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDFormViewModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.MenuConstants
import dagger.hilt.android.AndroidEntryPoint
import java.lang.reflect.Type

@AndroidEntryPoint
class AssessmentReadingActivity : BaseActivity(), FormEventListener, View.OnClickListener {

    private lateinit var binding: ActivityAssessmentReadingBinding
    private val viewModel: AssessmentReadingViewModel by viewModels()
    private val ncdFormViewModel: NCDFormViewModel by viewModels()
    private val bpViewModel: BloodPressureViewModel by viewModels()
    private val glucoseViewModel: GlucoseViewModel by viewModels()

    private lateinit var formGenerator: FormGenerator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssessmentReadingBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root, isToolbarVisible = true, title = getString(R.string.add_new_reading)
        )
        init()
        renderAssessmentForm()
        attachObservers()
    }

    private fun init() {
        intent.extras?.let { bundle ->
            viewModel.apply {
                formTypeId = bundle.getString(DefinedParams.FORM_TYPE_ID)
                patientId = bundle.getString(DefinedParams.PATIENT_ID)
                relatedPersonFhirId = bundle.getString(DefinedParams.RelatedPersonFhirId)
            }
            bpViewModel.apply {
                isRegularSmoker = bundle.getBoolean(Screening.is_regular_smoker)
                dateOfBirth = bundle.getString(Screening.DateOfBirth)
                gender = bundle.getString(DefinedParams.Gender)
            }
        }

        binding.btnSubmit.safeClickListener(this)
    }

    private fun renderAssessmentForm() {
        formGenerator = FormGenerator(
            this, binding.llForm, listener = this, scrollView = binding.scrollView
        ) { map, id ->
            when (id) {
                Screening.Weight, Screening.Height -> {
                    bpViewModel.renderBMIValue(this, formGenerator, map)
                }
            }
        }

        ncdFormViewModel.getNCDForm(MenuConstants.ASSESSMENT.lowercase())
        bpViewModel.getRiskEntityList()
    }

    private fun attachObservers() {
        ncdFormViewModel.ncdFormResponse.observe(this) { resources ->
            when (resources.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    resources.data?.let { responseData ->
                        val filter = responseData.filter {
                            it.id == viewModel.formTypeId || it.family == viewModel.formTypeId
                        }
                        formGenerator.populateViews(filter)
                    }
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }
        bpViewModel.getRiskEntityListLiveData.observe(this) {}
        bpViewModel.bpLogCreateResponseLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    showSuccessDialogue(
                        title = getString(R.string.blood_pressure),
                        message = resourceState.data?.message ?: "",
                    ) {
                        bpViewModel.bpLogListResponseLiveData.postError()
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                }

                ResourceState.ERROR -> {
                    hideLoading()
                    showErrorDialogue(
                        title = getString(R.string.error),
                        message = resourceState.message
                            ?: getString(R.string.something_went_wrong_try_later),
                        positiveButtonName = getString(R.string.ok),
                    ) {}
                }
            }
        }
        glucoseViewModel.glucoseLogCreateResponseLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    showSuccessDialogue(
                        title = getString(R.string.blood_glucose),
                        message = resourceState.data?.message ?: "",
                    ) {
                        glucoseViewModel.glucoseLogListResponseLiveData.postError()
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                }

                ResourceState.ERROR -> {
                    hideLoading()
                    showErrorDialogue(
                        title = getString(R.string.error),
                        message = resourceState.message
                            ?: getString(R.string.something_went_wrong_try_later),
                        positiveButtonName = getString(R.string.ok),
                    ) {}
                }
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.btnSubmit.id -> {
                formGenerator.formSubmitAction(view)
            }
        }
    }

    override fun loadLocalCache(id: String, localDataCache: Any, selectedParent: Long?) {/*
        Never used
         */
    }

    override fun onPopulate(targetId: String) {/*
        Never used
         */
    }

    override fun onCheckBoxDialogueClicked(
        id: String, serverViewModel: FormLayout, resultMap: Any?
    ) {/*
        Never used
         */
    }

    override fun onInstructionClicked(
        id: String,
        title: String,
        informationList: ArrayList<String>?,
        description: String?,
        dosageListModel: ArrayList<RecommendedDosageListModel>?
    ) {/*
        Never used
         */
    }

    override fun onFormSubmit(resultMap: HashMap<String, Any>?, serverData: List<FormLayout?>?) {
        withNetworkCheck(connectivityManager, {
            resultMap?.let { map ->
                processValuesAndProceed(map, serverData)
            }
        })
    }

    override fun onRenderingComplete() {/*
        Never used
         */
    }

    override fun onUpdateInstruction(id: String, selectedId: Any?) {/*
        Never used
         */
    }

    override fun onInformationHandling(
        id: String, noOfDays: Int, enteredDays: Int?, resultMap: HashMap<String, Any>?
    ) {/*
        Never used
         */
    }

    override fun onAgeCheckForPregnancy() {/*
        Never used
         */
    }

    override fun handleMandatoryCondition(serverData: FormLayout?) {

    }

    private fun processValuesAndProceed(
        resultMap: HashMap<String, Any>, serverData: List<FormLayout?>?
    ) {
        val map = HashMap<String, Any>()
        map.putAll(resultMap)

        when (viewModel.formTypeId) {
            DefinedParams.BP_LOG -> {
                //Average Systolic and Diastolic Calculation
                serverData?.first { it?.viewType == ViewType.VIEW_TYPE_FORM_BP }?.let {
                    bpViewModel.calculateBPValues(it, map)
                }
                calculateAverageBloodPressure(map)

                //BMI Calculation
                calculateBMI(map)

                //CVD Risk Calculation
                bpViewModel.let {
                    it.isRegularSmoker?.let { regularSmoke ->
                        map[Screening.is_regular_smoker] = regularSmoke
                    }
                    it.dateOfBirth?.let { dob ->
                        map[Screening.DateOfBirth] = dob
                    }
                    it.gender?.let { sex ->
                        map[DefinedParams.Gender] = sex
                    }
                }
                val resultOne = bpViewModel.getRiskEntityListLiveData.value
                val baseType: Type =
                    object : TypeToken<ArrayList<RiskClassificationModel>>() {}.type
                if (resultOne?.isNotEmpty() == true) {
                    val resultList = Gson().fromJson<ArrayList<RiskClassificationModel>>(
                        resultOne[0].nonLabEntity, baseType
                    )
                    calculateCVDRiskFactor(
                        map, ArrayList(resultList), bpViewModel.getSystolicAverage()
                    )
                }

                //BP Log Create - API
                val result = serverData?.let {
                    FormResultComposer().groupValues(
                        context = this, serverData = it, map
                    )
                }
                result?.first?.let {
                    StringConverter.stringToMap(it).let { requestMap ->
                        //Removing unwanted params
                        val bpLog = requestMap[DefinedParams.BP_LOG]
                        (bpLog as? LinkedTreeMap<String, Any>?)?.let { value ->
                            requestMap.putAll(value)
                            requestMap.remove(DefinedParams.BP_LOG)
                        }
                        requestMap.remove(Screening.DateOfBirth)
                        requestMap.remove(DefinedParams.Gender)

                        bpViewModel.createBpLog(
                            requestMap, viewModel.relatedPersonFhirId, viewModel.patientId
                        )
                    }
                }
            }

            DefinedParams.GLUCOSE_LOG -> {
                //Glucose Calculation
                calculateBloodGlucose(map) {}

                //Glucose Log Create - API
                val result = serverData?.let {
                    FormResultComposer().groupValues(
                        context = this, serverData = it, map
                    )
                }
                result?.first?.let {
                    StringConverter.stringToMap(it).let { requestMap ->
                        //Removing unwanted params
                        val glucoseLog = requestMap[DefinedParams.GLUCOSE_LOG]
                        (glucoseLog as? LinkedTreeMap<String, Any>?)?.let { value ->
                            requestMap.putAll(value)
                            requestMap.remove(DefinedParams.GLUCOSE_LOG)
                        }

                        glucoseViewModel.glucoseLogCreate(
                            requestMap, viewModel.relatedPersonFhirId, viewModel.patientId
                        )
                    }
                }
            }
        }
    }
}