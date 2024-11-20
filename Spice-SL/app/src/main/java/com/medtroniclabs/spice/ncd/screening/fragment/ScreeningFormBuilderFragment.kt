package com.medtroniclabs.spice.ncd.screening.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.forEach
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.spice.BuildConfig
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.CommonUtils.addAncEnableOrNot
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
import com.medtroniclabs.spice.common.DefinedParams
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
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.FormResultComposer
import com.medtroniclabs.spice.formgeneration.utility.CheckBoxDialog
import com.medtroniclabs.spice.mappingkey.Screening
import com.medtroniclabs.spice.ncd.assessment.viewmodel.BloodPressureViewModel
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.HIV
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDFormViewModel
import com.medtroniclabs.spice.ncd.screening.ui.DuplicationNudgeDialog
import com.medtroniclabs.spice.ncd.screening.ui.ScreeningActivity
import com.medtroniclabs.spice.ncd.screening.utils.ReferredReason
import com.medtroniclabs.spice.ncd.screening.viewmodel.GeneralDetailsViewModel
import com.medtroniclabs.spice.ncd.screening.viewmodel.ScreeningFormBuilderViewModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.rootSuffix
import com.medtroniclabs.spice.ui.common.GeneralInfoDialog
import com.medtroniclabs.spice.ui.home.AssessmentToolsActivity
import java.lang.reflect.Type

class ScreeningFormBuilderFragment : BaseFragment(), FormEventListener, View.OnClickListener {

    private lateinit var binding: FragmentScreeningFormBuilderBinding
    private lateinit var formGenerator: FormGenerator
    private val viewModel: ScreeningFormBuilderViewModel by activityViewModels()
    private val bpViewModel: BloodPressureViewModel by activityViewModels()
    private val ncdFormViewModel: NCDFormViewModel by activityViewModels()
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
                        bpViewModel.renderBMIValue(requireContext(), formGenerator, map)
                        showBGCardOrNot(map)
                    }

                    Screening.DateOfBirth, Screening.BloodGlucoseID,Screening.diabetes -> {
                        showBGCardOrNot(map)
                    }
                    DefinedParams.Gender -> {
                        showHidePregnancyCard(map)
                    }
                    Screening.lastMenstrualPeriod ->{
                        getGestationalPeriod(map,id)
                    }

                }
            }
        ncdFormViewModel.getNCDForm(MenuConstants.SCREENING.lowercase())
        bpViewModel.getRiskEntityList()
    }

    override fun onResume() {
        super.onResume()

        if ((activity as? ScreeningActivity?)?.ableToGetLocation() == true)
            getCurrentLocation()
    }

    private fun getGestationalPeriod(resultHashMap: HashMap<String, Any>, id: String) {
        if (!resultHashMap.containsKey(id)) {
            return
        }
        val gesDate = resultHashMap[id] as? String ?: return

        formGenerator.getViewByTag(Screening.GestationalAge)?.let { view ->
            try {
                val lastMenstrualDate = DateUtils.getLastMenstrualDate(gesDate)
                val gestationWeek =
                    lastMenstrualDate.let { DateUtils.calculateGestationalAge(it).first.toInt() }
                val totalWeeks = gestationWeek.coerceAtMost(Screening.PregnancyANCMaxValue)
                if (view is TextView) {
                    view.text = when {
                        totalWeeks > 1 -> "$totalWeeks ${getString(R.string.weeks)}"
                        else -> "$totalWeeks ${getString(R.string.week)}"
                    }
                    resultHashMap[Screening.GestationalAge] = totalWeeks
                }

            } catch (e: Exception) {
                if (view is TextView) {
                    view.text = getString(R.string.hyphen_symbol)
                }
                e.printStackTrace()
            }
        }
    }
    private fun showHidePregnancyCard(resultHashMap: HashMap<String, Any>) {
        val gender = resultHashMap[DefinedParams.Gender]
        if (gender != null && gender is String && gender.equals(Screening.Female, true)) {
            formGenerator.showHideCardFamily(true, Screening.pregnancyAnc)
        } else {
            formGenerator.showHideCardFamily(false, Screening.pregnancyAnc)
        }
    }

    private var screeningJSON: List<FormLayout>? = null
    private fun attachObservers() {
        ncdFormViewModel.ncdFormResponse.observe(viewLifecycleOwner) { resources ->
            when (resources.state) {
                ResourceState.LOADING -> {
                    (activity as? BaseActivity)?.showLoading()
                }

                ResourceState.SUCCESS -> {
                    (activity as? BaseActivity)?.hideLoading()
                    resources.data?.let {
                        screeningJSON = it
                        formGenerator.populateViews(it)
                    }
                }

                ResourceState.ERROR -> {
                    (activity as? BaseActivity)?.hideLoading()
                }
            }
        }

        viewModel.getMentalQuestions.observe(viewLifecycleOwner) { mentalQuestions ->
            mentalQuestions?.let {
                val mhResponse: HashMap<String, LocalSpinnerResponse> = HashMap()
                viewModel.getIdOfMentalHealth()?.first?.let {
                    viewModel.getIdOfMentalHealth()?.second?.let { second ->
                        mhResponse[second] =
                            LocalSpinnerResponse(tag = it, response = mentalQuestions)
                    }
                }
                mhResponse.forEach {
                    formGenerator.loadMentalHealthQuestions(it.value)
                }
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
                }

                ResourceState.ERROR -> {
                    (activity as? BaseActivity)?.hideLoading()
                }
            }
        }
        bpViewModel.getRiskEntityListLiveData.observe(viewLifecycleOwner){

        }
        viewModel.validatePatientResponseLiveDate.observe(viewLifecycleOwner) { resources ->
            when (resources.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    proceedScreening(resources.data)
                }

                ResourceState.ERROR -> {
                    hideProgress()
                    if (resources.optionalData == true && resources.data is Pair<*, *>) {
                        resources.data.first.let { responseMap ->
                            val dialog =
                                DuplicationNudgeDialog.newInstance(
                                    StringConverter.convertGivenMapToString(
                                        responseMap
                                    ), isFromEnrollment = false
                                ) {
                                    proceedAssessment(responseMap)
                                }
                            dialog.show(childFragmentManager, DuplicationNudgeDialog.TAG)
                        }
                    } else
                        proceedScreening(resources.data)
                }
            }
        }
    }

    private fun proceedScreening(data: Pair<java.util.HashMap<String, Any>, List<FormLayout?>?>?) {
        data?.let { processValuesAndProceed(it.first, it.second) }
    }

    private fun proceedAssessment(data: HashMap<String, Any>?) {
        data?.let { map ->
            map[AssessmentDefinedParams.memberReference]?.toString().let { fhirId ->
                val intent = Intent(requireContext(), AssessmentToolsActivity::class.java)
                intent.putExtra(DefinedParams.FhirId, fhirId)
                intent.putExtra(DefinedParams.ORIGIN, MenuConstants.ASSESSMENT)
                intent.putExtra(DefinedParams.Gender, data[DefinedParams.Gender]?.toString()?.lowercase())
                startActivity(intent)
                activity?.finish()
            }
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
        /*
        Never used
         */
    }

    override fun onCheckBoxDialogueClicked(
        id: String,
        serverViewModel: FormLayout,
        resultMap: Any?
    ) {
        CheckBoxDialog.newInstance(
            id,
            resultMap,
            valueKeyNotNeeded = serverViewModel.valueKeyNotNeeded == true
        ) { map ->
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

            withNetworkAvailability(online = {
                viewModel.validatePatient(resultMap, serverData)
            }, offline = {
                processValuesAndProceed(resultMap, serverData)
            }, requireErrorDialog = false)
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
        calculateBloodGlucose(map, removeUnwantedKeys = true) {( fbs, rbs )->
            if (fbs != null) {
                viewModel.setFbsBloodGlucose(fbs)
            }
            if (rbs != null) {
                viewModel.setRbsBloodGlucose(rbs)
            }
        }
        getPregnancySymptomCount(resultMap)
        calculateSuicidalIdeation(map)
        calculateCAGEAIDSCore(map, serverData)
        calculateFurtherAssessment(map, getMeasurementTypeValues(map), serverData)
        val resultOne = bpViewModel.getRiskEntityListLiveData.value
        val baseType: Type = object : TypeToken<ArrayList<RiskClassificationModel>>() {}.type
        if (resultOne?.isNotEmpty() == true) {
            val resultList = Gson().fromJson<ArrayList<RiskClassificationModel>>(
                resultOne[0].nonLabEntity,
                baseType
            )
            calculateCVDRiskFactor(map, ArrayList(resultList), bpViewModel.getSystolicAverage())
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
        SecuredPreference.getCountryId()?.let { countryId ->
            map[Screening.CountryId] = countryId
        }
        map[Screening.UnitMeasurement] = getUnitMeasurementType()
        val unwantedKeys =
            setOf(Week, Year, Month, Days, DefinedParams.Country, Screening.identityType)
        map.keys.removeAll(unwantedKeys)
        var result = screeningJSON?.let {
            FormResultComposer().groupValues(
                context = requireContext(),
                serverData = it,
                map,
                bmiCategoryGroupId = Screening.BioMetrics
            )
        }
        result?.second?.let {
            addAncEnableOrNot(it, Screening.pregnancyAnc)
        }
        val bioDataMap = result?.second?.get(Screening.bioData) as HashMap<String, Any>
        bioDataMap[Screening.identityType] = Screening.nationalId
        arguments?.getString(Screening.Initial)?.let { initial ->
            bioDataMap[Screening.Initial] = initial
        }
        bioDataMap[DefinedParams.Country] = CommonUtils.getCountryMap()

        result = Pair(StringConverter.convertGivenMapToString(result.second), result.second)
        val siteDetail = Gson().toJson(generalDetailsViewModel.siteDetail)
        result.first?.let {
            viewModel.savePatientScreeningInformation(
                it,
                siteDetail,
                eSignature = arguments?.getByteArray(Screening.Signature),
                isReferred = isReferred
            )
        }
    }

    override fun onRenderingComplete() {
        /*
        Never used
         */
    }

    override fun onUpdateInstruction(id: String, selectedId: Any?) {
        /*
        Never used
         */
    }

    override fun onInformationHandling(
        id: String,
        noOfDays: Int,
        enteredDays: Int?,
        resultMap: HashMap<String, Any>?
    ) {
        /*
        Never used
         */
    }

    override fun onAgeCheckForPregnancy() {
        /*
        Never used
         */
    }

    override fun handleMandatoryCondition(serverData: FormLayout?) {

    }

    override fun onAgeUpdateListener(
        age: String?,
        serverData: List<FormLayout?>?,
        resultHashMap: HashMap<String, Any>
    ) {
        age?.toIntOrNull()?.let { inputAge ->
            var matchingCondition: String? = null
            val hivViewBasedOnAge =
                serverData?.filter {
                    val (matches, condition) = it?.ageCondition?.let { ageConditionList ->
                        getAgeConditionCategory(inputAge, ageConditionList)
                    } ?: Pair(false, null)
                    if (matches) {
                        matchingCondition = condition
                    }
                    matches
                }
            val getRemainingHIVBasedViews =
                serverData?.filter { it?.ageCondition?.contains(matchingCondition)?.not() ?: false }
            hivViewBasedOnAge?.forEach { formItem ->
                if (formGenerator.isViewGone(formItem?.id + rootSuffix)) {
                    formGenerator.getViewByTag(formItem?.id + rootSuffix)?.visibility =
                        View.VISIBLE
                }
            }
            getRemainingHIVBasedViews?.forEach { formItem ->
                if (formGenerator.isViewVisible(formItem?.id ?: "")) {
                    formGenerator.getViewByTag(formItem?.id + rootSuffix)?.visibility =
                        View.GONE
                    formGenerator.getViewByTag(formItem?.id + rootSuffix)
                        ?.let { formGenerator.resetChildViews(it) }
                }
            }
        } ?: kotlin.run {
            resetAllHIVCategoryView(serverData)
        }
    }

    private fun getAgeConditionCategory(age: Int, ageCondition: ArrayList<String>): Pair<Boolean, String> {
        for (condition in ageCondition) {
            when {
                condition.contains("-") -> {
                    val (start, end) = condition.split("-").map { it.toInt() }
                    if (age in start..end) return Pair(true, condition)
                }
                condition.startsWith(">=") -> {
                    val minAge = condition.removePrefix(">=").toInt()
                    if (age >= minAge) return Pair(true, condition)
                }
                condition.startsWith("<=") -> {
                    val maxAge = condition.removePrefix("<=").toInt()
                    if (age <= maxAge) return Pair(true, condition)
                }
                condition.startsWith(">") -> {
                    val minAge = condition.removePrefix(">").toInt()
                    if (age > minAge) return Pair(true, condition)
                }
                condition.startsWith("<") -> {
                    val maxAge = condition.removePrefix("<").toInt()
                    if (age < maxAge) return Pair(true, condition)
                }
            }
        }
        return Pair(false, "")
    }

    private fun resetAllHIVCategoryView(serverData: List<FormLayout?>?) {
        val hivViewBasedOnAge = serverData?.filter { it?.ageCondition?.isNotEmpty() == true && it.workflowType?.contains(
            HIV) == true }
        hivViewBasedOnAge?.forEach { formItem ->
            if (formGenerator.isViewVisible(formItem?.id + "")) {
                formGenerator.getViewByTag(formItem?.id + rootSuffix)?.visibility =
                    View.GONE
                formGenerator.getViewByTag(formItem?.id + rootSuffix)
                    ?.let { formGenerator.resetChildViews(it) }
            }
        }
    }

    private fun calculateFurtherAssessment(
        map: HashMap<String, Any>,
        unitGenericType: String,
        serverData: List<FormLayout?>?
    ) {
        screeningJSON?.first { it.viewType == ViewType.VIEW_TYPE_FORM_BP }?.let {
            bpViewModel.calculateBPValues(it, map)
        }

        val assessmentConditionResult = checkAssessmentCondition(
            bpViewModel.getSystolicAverage(),
            bpViewModel.getDiastolicAverage(),
            viewModel.getPhQ4Score(),
            Pair(viewModel.getFbsBloodGlucose(), viewModel.getRbsBloodGlucose()),
            unitGenericType,
            getPregnancySymptomCount(map),
            Pair(null, map),
            serverData
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
                (formGenerator.getViewByTag(Screening.lastMealTime + formGenerator.lastMealTypeDateSuffix) as? ViewGroup)?.let { parentLayout ->
                    parentLayout.forEach { view ->
                        if (view is TextView) {
                            view.isSelected = false
                        }
                        if (view is ViewGroup) {
                            view.forEach { child ->
                                if (child is TextView) {
                                    child.isSelected = false
                                }
                            }
                        }
                    }
                    formGenerator.removeIfContains(Screening.lastMealTime + formGenerator.lastMealTypeDateSuffix)
                }
                (formGenerator.getViewByTag(Screening.lastMealTime + formGenerator.lastMealTypeMeridiem) as? ViewGroup)?.let { parentLayout ->
                    parentLayout.forEach { view ->
                        if (view is TextView) {
                            view.isSelected = false
                        }
                        if (view is ViewGroup) {
                            view.forEach { child ->
                                if (child is TextView) {
                                    child.isSelected = false
                                }
                            }
                        }
                    }
                    formGenerator.removeIfContains(Screening.lastMealTime + formGenerator.lastMealTypeMeridiem)
                }
                formGenerator.removeIfContains(Screening.lastMealTime + formGenerator.lastMealTypeMeridiem)
                View.GONE
            }
        }
    }
}