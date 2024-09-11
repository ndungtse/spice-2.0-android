package com.medtroniclabs.spice.ui.assessment.fragment

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.text.color
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.triggerOneTimeWorker
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.databinding.FragmentAssessmentNCDSummaryBinding
import com.medtroniclabs.spice.databinding.SummaryLayoutBinding
import com.medtroniclabs.spice.formgeneration.FormSummaryReporter
import com.medtroniclabs.spice.formgeneration.extension.capitalizeFirstChar
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.model.FormResponse
import com.medtroniclabs.spice.formgeneration.ui.FormResultComposer
import com.medtroniclabs.spice.mappingkey.Screening
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.assessment.MedicalComplianceResponse
import com.medtroniclabs.spice.ui.assessment.SymptomResponse
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel

class AssessmentNCDSummaryFragment : BaseFragment(), View.OnClickListener {

    private lateinit var binding: FragmentAssessmentNCDSummaryBinding
    private val viewModel: AssessmentViewModel by activityViewModels()
    private lateinit var formSummaryReporter: FormSummaryReporter
    private val patientDetailViewModel: PatientDetailViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAssessmentNCDSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObserver()
    }

    private fun initView() {
        binding.btnDone.safeClickListener(this)
        showPatientInfoCard()
        formSummaryReporter = FormSummaryReporter(requireContext(), binding.llFamilyRoot)
    }

    private fun showPatientInfoCard() {
        patientDetailViewModel.patientDetailsLiveData.value?.data?.let { screeningDetailsModel ->
            binding.llPatientInfo.visibility = View.VISIBLE

            binding.patientName.tvKey.text = getString(R.string.name)
            screeningDetailsModel?.name?.let { name ->
                binding.patientName.tvValue.text = name
            }

            binding.nationalId.tvKey.text = getString(R.string.national_id)
            screeningDetailsModel.identityValue?.let { identityValue ->
                binding.nationalId.tvValue.text = identityValue
            }

            binding.gender.tvKey.text = getString(R.string.gender)
            screeningDetailsModel?.gender?.let {
                binding.gender.tvValue.text = it.replaceFirstChar(Char::titlecase)
            }

            binding.dobAge.tvKey.text = getString(R.string.age)
            screeningDetailsModel?.age?.let { age ->
                binding.dobAge.tvValue.text = CommonUtils.getDecimalFormatted(age)
            }
        }
    }

    private fun attachObserver() {
        viewModel.formLayoutsNcdLiveData.value.let { form ->
            viewModel.assessmentSaveResponse.value?.data?.let {
                StringConverter.convertStringToMap(it.assessmentDetails)?.let { map ->
                    val formFieldsType = object : TypeToken<FormResponse>() {}.type
                    val formFields: FormResponse = Gson().fromJson(form, formFieldsType)
                    formSummaryReporter.populateAssessmentSummary(
                        formFields.formLayout,
                        map,
                        false
                    )
                    calculateOtherMetrics(formFields.formLayout, map)
                }
            }
        }
    }

    private fun calculateOtherMetrics(
        serverData: List<FormLayout>,
        map: Map<String, Any>
    ) {
        showBloodGlucoseValue(serverData, map)
        showBMIValue(serverData, map)
        showFurtherAssessment(map)
        showCVDRiskValue(map)
        showSymptoms(map)
        showMedicalCompliance(map)
    }

    private fun showMedicalCompliance(map: Map<String, Any>) {
        val linkedTreeMapList: List<ArrayList<LinkedTreeMap<String, String>>> = map.filter {
            it.key == AssessmentDefinedParams.compliance
        }.map { it.value as ArrayList<LinkedTreeMap<String, String>> }
        val symptomResponseList: ArrayList<MedicalComplianceResponse> = arrayListOf()
        linkedTreeMapList.flatten().forEach { linkedTreeMap ->
            val name = linkedTreeMap[DefinedParams.NAME] as? String ?: ""
            val otherCompliance = linkedTreeMap[AssessmentDefinedParams.other_compliance] as? String

            val complianceResponse = MedicalComplianceResponse(
                name = name,
                otherCompliance = otherCompliance
            )
            symptomResponseList.add(complianceResponse)
        }
        if (getSelectedMedicalComplianceText(symptomResponseList).isNotEmpty()) {
            showBindingValue(
                getString(R.string.medical_adherence),
                getSelectedMedicalComplianceText(symptomResponseList)
            )
        }
    }

    private fun showSymptoms(map: Map<String, Any>) {
        val linkedTreeMapList: List<ArrayList<LinkedTreeMap<String, String>>> = map.filter {
            it.key == AssessmentDefinedParams.symptomsDTO
        }.map { it.value as ArrayList<LinkedTreeMap<String, String>> }
        val symptomResponseList: ArrayList<SymptomResponse> = arrayListOf()

        linkedTreeMapList.flatten().forEach { linkedTreeMap ->
            val name = linkedTreeMap[DefinedParams.NAME] as? String ?: ""
            val type = linkedTreeMap[Screening.type] as? String
            val otherSymptom = linkedTreeMap[AssessmentDefinedParams.other_symptom] as? String

            val symptomResponse = SymptomResponse(
                name = name,
                type = type,
                otherSymptom = otherSymptom
            )
            symptomResponseList.add(symptomResponse)
        }
        showBindingValue(
            getString(R.string.symptoms),
            getSelectedSymptomsText(symptomResponseList)
        )
    }

    private fun getSelectedMedicalComplianceText(list: ArrayList<MedicalComplianceResponse>): String {
        val resultString = StringBuilder()
        list.forEachIndexed { index, medicalComplianceResponse ->
            resultString.append(medicalComplianceResponse.name)
            if (list.size > 1 && index == 0) {
                resultString.append(getString(R.string.empty_space))
                resultString.append(getString(R.string.separator_hyphen))
                resultString.append(getString(R.string.empty_space))
            }
            if (!medicalComplianceResponse.otherCompliance.isNullOrBlank()) {
                resultString.append(getString(R.string.empty_space))
                resultString.append(getString(R.string.separator_hyphen))
                resultString.append(getString(R.string.empty_space))
                resultString.append(
                    medicalComplianceResponse.otherCompliance.trim().capitalizeFirstChar()
                )
            }
            if (index > 0 && index != list.size - 1) {
                resultString.append(getString(R.string.comma_symbol))
            }
            resultString.append(getString(R.string.empty_space))
        }
        return resultString.toString()
    }

    private fun getSelectedSymptomsText(list: ArrayList<SymptomResponse>): String {
        val resultString = StringBuilder()
        list.forEachIndexed { index, symptomResponse ->
            resultString.append(symptomResponse.name)
            if (!symptomResponse.otherSymptom.isNullOrBlank()) {
                resultString.append(getString(R.string.empty_space))
                resultString.append(getString(R.string.separator_hyphen))
                resultString.append(getString(R.string.empty_space))
                resultString.append(symptomResponse.otherSymptom.trim().capitalizeFirstChar())
            } else if (symptomResponse.name.startsWith(
                    AssessmentDefinedParams.NoSymptoms,
                    true
                ) && !symptomResponse.type.isNullOrBlank()
            ) {
                resultString.append(getString(R.string.empty_space))
                resultString.append(getString(R.string.separator_hyphen))
                resultString.append(getString(R.string.empty_space))
                resultString.append(symptomResponse.type.trim().capitalizeFirstChar())
            }
            if (index != list.size - 1) {
                resultString.append(getString(R.string.comma_symbol))
            }
            resultString.append(getString(R.string.empty_space))
        }
        return resultString.toString()
    }

    private fun showCVDRiskValue(map: Map<String, Any>) {
        if (map.containsKey(Screening.CVD_Risk_Score_Display)) {
            val cvdRiskScoreDisplay = map[Screening.CVD_Risk_Score_Display]
            val cvdRiskScore = map[Screening.CVD_Risk_Score] as Double
            if (cvdRiskScoreDisplay is String) {
                showBindingValue(
                    getString(R.string.cvd_risk_level),
                    "$cvdRiskScoreDisplay",
                    CommonUtils.cvdRiskColorCode(
                        cvdRiskScore,
                        context = requireContext()
                    )
                )
            }
        }
    }

    private fun checkAssessmentCondition(map: Map<String, Any>): Boolean {
        var visibility = false

        if (!visibility && map.containsKey(Screening.ReferAssessment)) {
            visibility = map[Screening.ReferAssessment] as? Boolean ?: false
        }
        return visibility
    }

    private fun showFurtherAssessment(map: Map<String, Any>) {
        val assessmentCondition = checkAssessmentCondition(map)
        binding.riskResultLayout.isEnabled = assessmentCondition
        if (assessmentCondition) {
            setReferForFurtherAssessment()
        } else {
            binding.riskResultLayout.text = getString(R.string.no_assessment_required)
        }
        binding.riskResultLayout.visibility = View.VISIBLE
    }

    private fun setReferForFurtherAssessment() {
        val text = getString(R.string.referred_for_nfurther_assessment)
        binding.riskResultLayout.text = text
    }

    private fun showBMIValue(serverData: List<FormLayout>, map: Map<String, Any>) {
        //In future backend may take bio metrics
        val subMap = map[Screening.BioMetrics] as Map<String, Any>
        if (subMap.containsKey(Screening.BMI)) {
            val bmiValue = subMap[Screening.BMI]

            if (bmiValue is Double) {
                val bmiInfo = CommonUtils.getBMIInformation(requireContext(), bmiValue)
                val bmiFormattedValue = CommonUtils.getDecimalFormatted(bmiValue)
                if (bmiInfo == null) {
                    showBindingValue(
                        getString(R.string.bmi),
                        bmiFormattedValue
                    )
                } else {
                    val spannableStringBuilder =
                        SpannableStringBuilder().append(bmiFormattedValue)
                            .color(ContextCompat.getColor(requireContext(), bmiInfo.second)) {
                                append(" (${bmiInfo.first})")
                            }
                    showBindingValue(
                        getString(R.string.bmi),
                        spannableStringBuilder
                    )
                }
            }
        }
    }

    private fun showBloodGlucoseValue(serverData: List<FormLayout>, map: Map<String, Any>) {

        FormResultComposer.findGroupIdForNCD(serverData, Screening.Glucose_Type)?.let {
            val subMap = map[it] as Map<String, Any>
            if (subMap.containsKey(Screening.Glucose_Type)) {
                val type = subMap[Screening.Glucose_Type] as String
                val glucoseValue = (subMap[Screening.Glucose_Value] as Double)
                var unitType: String? = null
                if (subMap.containsKey(Screening.BloodGlucoseID + Screening.unitMeasurement_KEY)) {
                    val unitTypeKey =
                        subMap[Screening.BloodGlucoseID + Screening.unitMeasurement_KEY]
                    if (unitTypeKey is String) {
                        unitType = unitTypeKey
                    }
                }
                if (type.lowercase() == Screening.rbs) {
                    showBindingValue(
                        getString(R.string.blood_glucose_rbs),
                        "${CommonUtils.getDecimalFormatted(glucoseValue)} ${
                            CommonUtils.getGlucoseUnit(
                                unitType,
                                true
                            )
                        }"
                    )
                } else if (type.lowercase() == Screening.fbs) {
                    showBindingValue(
                        getString(R.string.blood_glucose_fbs),
                        "${CommonUtils.getDecimalFormatted(glucoseValue)} ${
                            CommonUtils.getGlucoseUnit(
                                unitType,
                                true
                            )
                        }"
                    )
                }
            }
        }
    }

    private fun showBindingValue(
        title: String,
        value: SpannableStringBuilder,
        valueTextColor: Int? = null
    ) {
        val summaryBinding = SummaryLayoutBinding.inflate(layoutInflater)
        summaryBinding.tvKey.text = title
        summaryBinding.tvValue.text = value
        valueTextColor?.let {
            summaryBinding.tvValue.setTextColor(it)
        }
        binding.root.findViewWithTag<LinearLayout>(formSummaryReporter.getFormResultView())
            ?.addView(summaryBinding.root)
    }

    private fun showBindingValue(title: String, value: String, valueTextColor: Int? = null) {
        val summaryBinding = SummaryLayoutBinding.inflate(layoutInflater)
        summaryBinding.tvKey.text = title
        summaryBinding.tvValue.text = value
        valueTextColor?.let {
            summaryBinding.tvValue.setTextColor(it)
        }
        binding.root.findViewWithTag<LinearLayout>(formSummaryReporter.getFormResultView())
            ?.addView(summaryBinding.root)
    }

    companion object {
        const val TAG = "AssessmentNCDSummaryFragment"
        fun newInstance(): AssessmentNCDSummaryFragment {
            return AssessmentNCDSummaryFragment()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnDone.id -> {
                requireContext().triggerOneTimeWorker()
                (activity as? BaseActivity)?.startActivityWithoutSplashScreen()
            }
        }
    }
}