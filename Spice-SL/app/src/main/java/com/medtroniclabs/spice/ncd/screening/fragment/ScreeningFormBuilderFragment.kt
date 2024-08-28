package com.medtroniclabs.spice.ncd.screening.fragment

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.color
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.spice.BuildConfig
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.CommonUtils.calculateAverageBloodPressure
import com.medtroniclabs.spice.common.CommonUtils.calculateBMI
import com.medtroniclabs.spice.common.CommonUtils.calculateBloodGlucose
import com.medtroniclabs.spice.common.CommonUtils.calculateCAGEAIDSCore
import com.medtroniclabs.spice.common.CommonUtils.calculateCVDRiskFactor
import com.medtroniclabs.spice.common.CommonUtils.calculatePHQScore
import com.medtroniclabs.spice.common.CommonUtils.calculateSuicidalIdeation
import com.medtroniclabs.spice.common.CommonUtils.checkAssessmentCondition
import com.medtroniclabs.spice.common.CommonUtils.getMeasurementTypeValues
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.SecuredPreference.getUnitMeasurementType
import com.medtroniclabs.spice.common.SpiceLocationManager
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.data.LocalSpinnerResponse
import com.medtroniclabs.spice.data.model.RecommendedDosageListModel
import com.medtroniclabs.spice.databinding.FragmentScreeningFormBuilderBinding
import com.medtroniclabs.spice.db.entity.RiskClassificationModel
import com.medtroniclabs.spice.formgeneration.FormGenerator
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.Days
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.Month
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.Week
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.Year
import com.medtroniclabs.spice.formgeneration.config.ViewType
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.listener.FormEventListener
import com.medtroniclabs.spice.formgeneration.model.BPModel
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.model.FormResponse
import com.medtroniclabs.spice.formgeneration.ui.FormResultComposer
import com.medtroniclabs.spice.formgeneration.utility.CheckBoxDialog
import com.medtroniclabs.spice.mappingkey.Screening
import com.medtroniclabs.spice.ncd.screening.viewmodel.GeneralDetailsViewModel
import com.medtroniclabs.spice.ncd.screening.viewmodel.ScreeningFormBuilderViewModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.rootSuffix
import com.medtroniclabs.spice.ui.common.GeneralInfoDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt
import java.lang.reflect.Type

class ScreeningFormBuilderFragment : BaseFragment(), FormEventListener, View.OnClickListener {

    private lateinit var binding: FragmentScreeningFormBuilderBinding
    private lateinit var formGenerator: FormGenerator
    private val viewModel: ScreeningFormBuilderViewModel by activityViewModels()
    private val generalDetailsViewModel: GeneralDetailsViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScreeningFormBuilderBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "ScreeningFormBuilderFragment"
        fun newInstance() =
            ScreeningFormBuilderFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObservers()
        setListeners()
        getCurrentLocation()
    }

    private fun getCurrentLocation() {
        val locationManager = SpiceLocationManager(requireContext())
        locationManager.getCurrentLocation {
            viewModel.setCurrentLocation(it)
        }
    }

    private fun setListeners() {
        binding.btnNext.safeClickListener(this)
        binding.btnCancel.safeClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnNext -> {
                formGenerator.formSubmitAction(v)
            }
        }
    }

    private fun initView() {
        showProgress()
        binding.btnNext.text = getString(R.string.submit)
        formGenerator =
            FormGenerator(
                requireContext(), binding.llForm, listener = this, scrollView = binding.scrollView
            ) { map, id ->
                when (id) {
                    Screening.Weight, Screening.Height -> {
                        renderBMIValue(map)
                        showBGCardOrNot(map)
                    }

                    Screening.DateOfBirth, Screening.BloodGlucoseID,Screening.diabetes -> {
                        showBGCardOrNot(map)
                    }

                }
            }

        viewModel.getFormData(MenuConstants.SCREENING.lowercase())
        viewModel.getRiskEntityList()
    }

    private var screeningJSON: List<FormLayout>? = null
    private fun attachObservers() {
        viewModel.formLayoutsLiveData.observe(viewLifecycleOwner) { data ->
            showProgress()
            val formFieldsType = object : TypeToken<FormResponse>() {}.type
            val formFields: FormResponse = Gson().fromJson(data, formFieldsType)
            formGenerator.populateViews(formFields.formLayout)
            screeningJSON = formFields.formLayout
            hideProgress()
        }

        viewModel.getMentalQuestions.observe(viewLifecycleOwner) { mentalQuestions ->
            val mhResponse: HashMap<String, LocalSpinnerResponse> = HashMap()
            viewModel.getIdOfMentalHealth()?.first?.let {
                viewModel.getIdOfMentalHealth()?.second?.let { second ->
                    mhResponse[second] = LocalSpinnerResponse(tag = it, response = mentalQuestions)
                }
            }
            mhResponse.forEach {
                formGenerator.loadMentalHealthQuestions(it.value)
            }
        }

        viewModel.screeningSaveResponse.observe(viewLifecycleOwner) { resources ->
            when (resources.state) {
                ResourceState.LOADING -> {
                    (activity as? BaseActivity)?.showLoading()
                }

                ResourceState.SUCCESS -> {
                    (activity as? BaseActivity)?.hideLoading()
                    resources.data?.let {
                        replaceFragmentIfExists<ScreeningSummaryFragment>(
                            R.id.screeningParentLayout,
                            bundle = null,
                            tag = ScreeningSummaryFragment.TAG
                        )
                    }
                    viewModel.screeningSaveResponse.postError()
                }

                ResourceState.ERROR -> {
                    (activity as? BaseActivity)?.hideLoading()
                }
            }
        }
        viewModel.getRiskEntityListLiveData.observe(viewLifecycleOwner){

        }
    }

    override fun loadLocalCache(id: String, localDataCache: Any, selectedParent: Long?) {
        if (localDataCache is String) {
            when (localDataCache) {
                Screening.PHQ4 -> {
                    viewModel.getMentalQuestion(id, localDataCache)
                }
            }
        }
    }

    override fun onPopulate(targetId: String) {
        // TODO("Not yet implemented")
    }

    override fun onCheckBoxDialogueClicked(
        id: String,
        serverViewModel: FormLayout,
        resultMap: Any?
    ) {
        CheckBoxDialog.newInstance(id, resultMap) { map ->
            formGenerator.validateCheckboxDialogue(id, serverViewModel, map)
        }.show(childFragmentManager, CheckBoxDialog.TAG)
    }

    override fun onInstructionClicked(
        id: String,
        title: String,
        informationList: ArrayList<String>?,
        description: String?,
        dosageListModel: ArrayList<RecommendedDosageListModel>?
    ) {
        informationList?.let { informationList ->
            GeneralInfoDialog.newInstance(
                title,
                description,
                informationList
            ).show(childFragmentManager, GeneralInfoDialog.TAG)
        }
    }

    override fun onFormSubmit(resultMap: HashMap<String, Any>?, serverData: List<FormLayout?>?) {
        resultMap?.let {
            viewModel.getCurrentLocation()?.let { location ->
                resultMap[Screening.Latitude] = location.latitude.toString()
                resultMap[Screening.Longitude] = location.longitude.toString()
            } ?: kotlin.run {
                resultMap[Screening.Latitude] = SecuredPreference.getDouble(
                    SecuredPreference.EnvironmentKey.CURRENT_LATITUDE.name,
                    0.0
                ).toString()
                resultMap[Screening.Longitude] = SecuredPreference.getDouble(
                    SecuredPreference.EnvironmentKey.CURRENT_LONGITUDE.name,
                    0.0
                ).toString()
            }
            resultMap[Screening.Screening_Date_Time] =
                DateUtils.getCurrentDateTimeInUserTimeZone(DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ)
            resultMap[Screening.AppVersion] = BuildConfig.VERSION_NAME
            processValuesAndProceed(resultMap, serverData)
        }
    }

    private fun processValuesAndProceed(
        resultMap: HashMap<String, Any>,
        serverData: List<FormLayout?>?
    ) {
        val map = HashMap<String, Any>()
        map.putAll(resultMap)
        calculateBMI(map)
        calculateAverageBloodPressure(map)
        viewModel.setPhQ4Score(calculatePHQScore(map))
        calculateBloodGlucose(map) {( fbs, rbs )->
            if (fbs != null) {
                viewModel.setFbsBloodGlucose(fbs)
            }
            if (rbs != null) {
                viewModel.setRbsBloodGlucose(rbs)
            }
        }
        calculateSuicidalIdeation(map)
        calculateCAGEAIDSCore(map, serverData)
        calculateFurtherAssessment(map, getMeasurementTypeValues(map))
        val resultOne = viewModel.getRiskEntityListLiveData.value
        val baseType: Type = object : TypeToken<ArrayList<RiskClassificationModel>>() {}.type
        if (resultOne?.isNotEmpty() == true) {
            val resultList = Gson().fromJson<ArrayList<RiskClassificationModel>>(
                resultOne[0].nonLabEntity,
                baseType
            )
            calculateCVDRiskFactor(map, ArrayList(resultList), viewModel.getSystolicAverage())
        }
        var isReferred = false
        if (map.containsKey(Screening.ReferAssessment)) {
            val referralString = map[Screening.ReferAssessment] as Boolean
            referralString.let {
                isReferred = it
            }
        }
        SecuredPreference.getDeviceID()?.let {
            map[Screening.Device_Info_Id] = it
        }

        map[Screening.UnitMeasurement] = getUnitMeasurementType()
        val unwantedKeys = setOf(Week, Year, Month, Days)
        map.keys.removeAll(unwantedKeys)
        var result = screeningJSON?.let {
            FormResultComposer().groupValues(
                context = requireContext(),
                serverData = it,
                map,
                bmiCategoryGroupId = Screening.BioMetrics
            )
        }
        val bioDataMap = result?.second?.get(Screening.bioData) as HashMap<String, Any>
        bioDataMap[Screening.identityType] = Screening.nationalId
        result = Pair(StringConverter.convertGivenMapToString(result.second), result.second)
        val siteDetail = Gson().toJson(generalDetailsViewModel.siteDetail)
        if (result != null) {
            result.first?.let {
                viewModel.savePatientScreeningInformation(
                    it,
                    siteDetail,
                    isReferred = isReferred
                )
            }
        }
    }
    override fun onRenderingComplete() {
        // TODO("Not yet implemented")
    }

    override fun onUpdateInstruction(id: String, selectedId: Any?) {
        // TODO("Not yet implemented")
    }

    override fun onInformationHandling(
        id: String,
        noOfDays: Int,
        enteredDays: Int?,
        resultMap: HashMap<String, Any>?
    ) {
        // TODO("Not yet implemented")
    }

    override fun onAgeCheckForPregnancy() {
        // TODO("Not yet implemented")
    }

    private fun calculateFurtherAssessment(map: HashMap<String, Any>, unitGenericType: String) {
        screeningJSON?.first { it.viewType == ViewType.VIEW_TYPE_FORM_BP }?.let {
            calculateBPValues(it, map)
        }

        val assessmentConditionResult = checkAssessmentCondition(
            viewModel.getSystolicAverage(),
            viewModel.getDiastolicAverage(),
            viewModel.getPhQ4Score(),
            Pair(viewModel.getFbsBloodGlucose(), viewModel.getRbsBloodGlucose()),
            unitGenericType,
            getPregnancySymptomCount(map),
            Pair(null, map)
        )
        map[Screening.ReferAssessment] =
            if (assessmentConditionResult.first) Screening.PositiveValue else Screening.NegativeValue

        if (assessmentConditionResult.second.isNotEmpty())
            map[Screening.referredReasons] = assessmentConditionResult.second
    }

    private fun getPregnancySymptomCount(resultHashMap: HashMap<String, Any>): Int {
        if (resultHashMap.containsKey(Screening.PregnancySymptoms)) {
            return CommonUtils.calculatePregnancySymptomCount(resultHashMap[Screening.PregnancySymptoms] as java.util.ArrayList<Map<*, *>>)
        }
        return 0
    }
    private fun calculateBPValues(formLayout: FormLayout, resultMap: Map<String, Any>) {
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

    private fun updateAverage(
        actualMapList: java.util.ArrayList<*>,
        systolicEntries: Int,
        diastolicEntries: Int,
        systolic: Double,
        diastolic: Double
    ) {
        if (actualMapList.size > 0 && systolicEntries > 0 && diastolicEntries > 0) {
            viewModel.systolicAverageSummary = (systolic / systolicEntries).roundToInt()
            viewModel.diastolicAverageSummary = (diastolic / diastolicEntries).roundToInt()
        }
    }

    private fun validateMap(map: Any?, value: String): Double? {
        return if (map is Map<*, *> && map.containsKey(value)) ( map[value] as String).toDoubleOrNull() else null
    }

    private fun renderBMIValue(resultHashMap: HashMap<String, Any>) {
        val bmiView = formGenerator.getViewByTag(Screening.BMI) as? AppCompatTextView
        bmiView?.let { view ->
            if (!resultHashMap.containsKey(Screening.Weight) || !resultHashMap.containsKey(Screening.Height)) {
                view.text = getString(R.string.hyphen_symbol)
                formGenerator.removeIfContains(Screening.BMI)
            } else {
                if (resultHashMap.containsKey(Screening.Weight) && resultHashMap.containsKey(
                        Screening.Height
                    )
                ) {
                    val weight = resultHashMap[Screening.Weight] as? Double
                    val height = resultHashMap[Screening.Height] as? Double

                    if (weight == null || height == null) {
                        view.text = getString(R.string.hyphen_symbol)
                    } else {
                        val bmi = CommonUtils.getBMIForNcd(height, weight)
                        CommonUtils.getBMIInformation(requireContext(), bmi?.toDoubleOrNull())
                            ?.let { info ->
                                resultHashMap[Screening.BMI_CATEGORY] = info.first

                                val bmiWithInfoSpannableStringBuilder = if (bmi == null) {
                                    requireContext().getString(R.string.hyphen_symbol)
                                } else {
                                    SpannableStringBuilder().append(bmi)
                                        .color(requireContext().getColor(info.second)) {
                                            append(" (${info.first})")
                                        }
                                }
                                view.text = bmiWithInfoSpannableStringBuilder
                            }
                    }
                }
            }
        }
    }

    private fun showBGCardOrNot(resultHashMap: HashMap<String, Any>) {
        val dob = (resultHashMap[Screening.DateOfBirth] as? String)?.let {
            DateUtils.getV2YearMonthAndWeek(it)
        }
        val bmi = calculateBMI(resultHashMap)
        val diabetes = resultHashMap[Screening.diabetes]
        val shouldShow =
            (dob != null && dob.years > 40) || (bmi != null && bmi > 25) || formGenerator.checkIfNoSymptomsPresent(
                diabetes
            )
        showGlucoseValues(shouldShow)
    }

    private fun showGlucoseValues(status: Boolean) {
        formGenerator.getViewByTag(Screening.BloodGlucoseID + formGenerator.rootSuffix)
            ?.let { view ->
                if (status) {
                    view.visibility = View.VISIBLE
                } else {
                    formGenerator.getViewByTag(Screening.BloodGlucoseID)?.let { editText ->
                        if (editText is AppCompatEditText) {
                            editText.setText("")
                            formGenerator.removeIfContains(Screening.BloodGlucoseID)
                        }
                    }
                    view.visibility = View.GONE
                }
            }
        formGenerator.getViewByTag(Screening.lastMealTime + rootSuffix)?.let { view ->
            view.visibility = if (status) View.VISIBLE else {
                formGenerator.getViewByTag(R.id.etHour)?.let { editText ->
                    if (editText is AppCompatEditText) {
                        editText.setText("")
                    }
                }
                formGenerator.getViewByTag(R.id.etMinute)?.let { editText ->
                    if (editText is AppCompatEditText) {
                        editText.setText("")
                    }
                }
                formGenerator.removeIfContains(Screening.lastMealTime)
                formGenerator.removeIfContains(Screening.lastMealTime + formGenerator.lastMealTypeMeridiem)
                formGenerator.removeIfContains(Screening.lastMealTime + formGenerator.lastMealTypeDateSuffix)
                View.GONE
            }
        }
    }
}