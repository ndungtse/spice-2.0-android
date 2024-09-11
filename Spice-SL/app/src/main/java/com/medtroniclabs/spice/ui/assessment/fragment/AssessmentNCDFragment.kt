package com.medtroniclabs.spice.ui.assessment.fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.color
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.isGone
import com.medtroniclabs.spice.appextensions.isVisible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.CommonUtils.calculateBloodGlucose
import com.medtroniclabs.spice.common.CommonUtils.calculateCVDRiskFactor
import com.medtroniclabs.spice.common.CommonUtils.calculateProvisionalDiagnosis
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.data.model.RecommendedDosageListModel
import com.medtroniclabs.spice.data.model.SymptomModel
import com.medtroniclabs.spice.databinding.FragmentAssessmentNCDBinding
import com.medtroniclabs.spice.db.entity.MedicalComplianceEntity
import com.medtroniclabs.spice.db.entity.RiskClassificationModel
import com.medtroniclabs.spice.formgeneration.FormGenerator
import com.medtroniclabs.spice.formgeneration.config.ViewType
import com.medtroniclabs.spice.formgeneration.extension.dp
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.extension.textSizeSsp
import com.medtroniclabs.spice.formgeneration.listener.FormEventListener
import com.medtroniclabs.spice.formgeneration.model.BPModel
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.model.FormResponse
import com.medtroniclabs.spice.formgeneration.ui.FormResultComposer
import com.medtroniclabs.spice.formgeneration.utility.CheckBoxDialog
import com.medtroniclabs.spice.mappingkey.Screening
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.ncd.assessment.viewmodel.BloodPressureViewModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.assessment.ComplianceSpinnerAdapter
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import com.medtroniclabs.spice.ui.common.GeneralInfoDialog
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import java.lang.reflect.Type
import kotlin.math.roundToInt

class AssessmentNCDFragment : BaseFragment(), FormEventListener, View.OnClickListener {

    private lateinit var binding: FragmentAssessmentNCDBinding
    private lateinit var formGenerator: FormGenerator
    private val viewModel: AssessmentViewModel by activityViewModels()
    private val patientDetailViewModel: PatientDetailViewModel by activityViewModels()
    private val bpViewModel: BloodPressureViewModel by activityViewModels()
    private var assessmentJSON: List<FormLayout>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAssessmentNCDBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getPatientDetails()
        initializeFormGenerator()
        attachObserver()
        loadSymptomsAndCompliance()
    }

    private fun initializeFormGenerator() {
        binding.btnSubmit.safeClickListener(this)
        formGenerator = FormGenerator(
            requireContext(), binding.llForm, listener = this, scrollView = binding.scrollView
        ) { map, id ->
            when (id) {
                Screening.Weight, Screening.Height -> {
                    bpViewModel.renderBMIValue(requireContext(), formGenerator, map)
                }
            }
        }
        viewModel.assessmentType = requireArguments().getString(Screening.type)
        viewModel.assessmentType?.let { viewModel.getNcdFormData(DefinedParams.Assessment, it) }
        bpViewModel.getRiskEntityList()
    }

    private fun getPatientDetails() {
        patientDetailViewModel.origin = requireArguments().getString(DefinedParams.ORIGIN)
        requireArguments().getString(DefinedParams.FhirId)?.let { id ->
            patientDetailViewModel.getPatients(
                id,
                origin = patientDetailViewModel.origin?.lowercase()
            )
        }

    }

    private fun showBGCardOrNot(resultHashMap: HashMap<String, Any>) {
        val dob = (resultHashMap[Screening.DateOfBirth] as? String)?.let {
            DateUtils.getV2YearMonthAndWeek(it)
        }
        val bmi = CommonUtils.calculateBMI(resultHashMap)
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
        formGenerator.getViewByTag(Screening.lastMealTime + AssessmentDefinedParams.rootSuffix)
            ?.let { view ->
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

    private fun loadSymptomsAndCompliance() {
        if (viewModel.assessmentType.equals(MenuConstants.NCD_MENU_ID)) {
            binding.symptomCard.root.visibility = View.VISIBLE
            binding.symptomCard.cardSymptomTitle.text = getString(R.string.symptoms)
            binding.symptomCard.symptomsSpinner.tvTitle.text =
                getString(R.string.since_the_last_assessment_symptom_info)
            binding.symptomCard.symptomsSpinner.tvTitle.markMandatory()
            binding.symptomCard.symptomsSpinner.etUserInput.safeClickListener {
                SymptomsChooseDialog.newInstance()
                    .show(childFragmentManager, SymptomsChooseDialog.TAG)
            }
            binding.symptomCard.complianceSpinner.tvTitle.text =
                getString(R.string.complaince_title)
            viewModel.getMedicationParentComplianceList()
            binding.symptomCard.complianceSpinner.etUserInput
            binding.symptomCard.complianceSpinner.etUserInput.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        p0: AdapterView<*>?,
                        p1: View?,
                        pos: Int,
                        p3: Long
                    ) {
                        binding.symptomCard.complianceSpinner.etUserInput.adapter?.let { adapter ->
                            adapter as ComplianceSpinnerAdapter
                            adapter.getData(pos)?.let {
                                viewModel.selectedMedication.value = if (it.id > 0) it else null
                            }
                        }
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                        /**
                         * this method is not used
                         */
                    }

                }
            binding.symptomCard.otherHypertensionSymptom.tvTitle.text =
                getString(R.string.other_hypertension_symptom)
            binding.symptomCard.otherHypertensionSymptom.tvTitle.markMandatory()
            binding.symptomCard.otherDiabetesSymptom.tvTitle.text =
                getString(R.string.other_diabetes_symptom)
            binding.symptomCard.otherDiabetesSymptom.tvTitle.markMandatory()
            binding.symptomCard.newWorseningHypertensionSymptom.tvTitle.text =
                getString(R.string.new_or_worsening_hypertension)
            binding.symptomCard.newWorseningHypertensionSymptom.tvTitle.markMandatory()
            binding.symptomCard.newWorseningDiabetesSymptom.tvTitle.text =
                getString(R.string.new_or_worsening_diabetes)
            binding.symptomCard.newWorseningDiabetesSymptom.tvTitle.markMandatory()
        } else {
            binding.symptomCard.clParentRoot.isGone()
        }

    }

    private fun attachObserver() {
        viewModel.formLayoutsNcdLiveData.observe(viewLifecycleOwner) { data ->
            val formFieldsType = object : TypeToken<FormResponse>() {}.type
            val formFields: FormResponse = Gson().fromJson(data, formFieldsType)
            formGenerator.populateViews(formFields.formLayout)
            assessmentJSON = formFields.formLayout
            hideProgress()
        }
        if (viewModel.assessmentType.equals(MenuConstants.NCD_MENU_ID)) {
            viewModel.selectedMedication.observe(viewLifecycleOwner) { model ->
                model?.let { selectedModel ->
                    if (selectedModel.childExists) {
                        viewModel.getMedicationChildComplianceList(selectedModel.id)
                    } else hideComplianceOptions()
                    addOrRemoveValuesFromResultMap(selectedModel)
                } ?: kotlin.run {
                    hideComplianceOptions()
                    viewModel.complianceMap?.clear()
                }
                binding.symptomCard.otherComplianceReason.setText("")
                binding.symptomCard.otherComplianceReason.visibility = View.GONE
            }
            viewModel.medicationChildComplianceResponse.observe(viewLifecycleOwner) { list ->
                loadData(list)
            }

            viewModel.selectedSymptoms.observe(viewLifecycleOwner) { selectedSymptoms ->
                loadSymptomsData(selectedSymptoms)
            }
            viewModel.medicationParentComplianceResponse.observe(viewLifecycleOwner) { list ->
                val adapter = ComplianceSpinnerAdapter(
                    requireContext(),
                    true
                )
                val complianceList = ArrayList(list)
                complianceList.add(
                    0,
                    MedicalComplianceEntity(
                        id = (-1).toLong(),
                        name = getString(R.string.please_select)
                    )
                )
                adapter.setData(complianceList)
                binding.symptomCard.complianceSpinner.etUserInput.adapter = adapter
            }
        }
        patientDetailViewModel.patientDetailsLiveData.observe(viewLifecycleOwner) { resourceData ->
            when (resourceData.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    autoPopulateDetails(data = resourceData.data)
                }
            }
        }
        bpViewModel.getRiskEntityListLiveData.observe(viewLifecycleOwner) {

        }
    }

    private fun autoPopulateDetails(data: PatientListRespModel?) {
        showPatientInfoCard(data)
    }

    private fun showPatientInfoCard(screeningDetailsModel: PatientListRespModel?) {
        viewModel.bioDataMap = HashMap()
        //Future backend need to remove bioMetric
        viewModel.bioMetric = HashMap()
        binding.llPatientInfo.visibility = View.VISIBLE

        binding.patientName.tvKey.text = getString(R.string.name)
        screeningDetailsModel?.name?.let { name ->
            binding.patientName.tvValue.text = name
        }
        binding.nationalId.tvKey.text = getString(R.string.national_id)
        viewModel.bioDataMap?.apply {
            this[Screening.First_Name] = screeningDetailsModel?.firstName.toString()
            this[AssessmentDefinedParams.Middle_Name] = screeningDetailsModel?.middleName.toString()
            this[Screening.lastName] = screeningDetailsModel?.lastName.toString()
            this[Screening.phoneNumber] = screeningDetailsModel?.phoneNumber.toString()
            this[AssessmentDefinedParams.phoneNumberCategory] = screeningDetailsModel?.phoneNumberCategory.toString()
            this[AssessmentDefinedParams.landmark] = screeningDetailsModel?.landmark.toString()
            screeningDetailsModel?.identityType?.let { identityType ->
                screeningDetailsModel.identityValue?.let { identityValue ->
                    binding.nationalId.tvValue.text = identityValue
                    this[Screening.identityType] = identityType
                    this[Screening.identityValue] = identityValue
                }
            }
            screeningDetailsModel?.isRegularSmoker?.let {
                this[Screening.is_regular_smoker] = it
            }
            this[Screening.DateOfBirth] =  screeningDetailsModel?.dateOfBirth.toString()
        }

        binding.gender.tvKey.text = getString(R.string.gender)
        screeningDetailsModel?.gender?.let {
            binding.gender.tvValue.text = it.replaceFirstChar(Char::titlecase)
            viewModel.bioMetric?.apply {
                this[DefinedParams.Gender] = it
                this[Screening.Age] = screeningDetailsModel?.age.toString()
                this[Screening.Height] = screeningDetailsModel?.height.toString()
                this[Screening.Weight] = screeningDetailsModel?.weight.toString()

            }
            if (it.equals(Screening.Female, true) && screeningDetailsModel.isPregnant == true) {
                formGenerator.showHideCardFamily(true, AssessmentDefinedParams.pregnancyAnc)
            } else {
                formGenerator.showHideCardFamily(false, AssessmentDefinedParams.pregnancyAnc)
            }
        }

        binding.dobAge.tvKey.text = getString(R.string.age)
        var date: String? = null
        screeningDetailsModel?.age?.let { age ->
            date = CommonUtils.getDecimalFormatted(age)
        }
        binding.dobAge.tvValue.text = date
        showHeightWeight(screeningDetailsModel?.height, screeningDetailsModel?.weight)
    }

    private fun loadSymptomsData(selectedSymptoms: List<SymptomModel>?) {
        if (selectedSymptoms.isNullOrEmpty()) {
            binding.symptomCard.symptomsSpinner.etUserInput.text = ""
            binding.symptomCard.otherHypertensionSymptom.llRoot.visibility = View.GONE
            binding.symptomCard.otherHypertensionSymptom.etUserInput.setText("")
            binding.symptomCard.otherDiabetesSymptom.llRoot.visibility = View.GONE
            binding.symptomCard.otherDiabetesSymptom.etUserInput.setText("")
            binding.symptomCard.newWorseningHypertensionSymptom.llRoot.visibility = View.GONE
            binding.symptomCard.newWorseningHypertensionSymptom.etUserInput.setText("")
            binding.symptomCard.newWorseningDiabetesSymptom.llRoot.visibility = View.GONE
            binding.symptomCard.newWorseningDiabetesSymptom.etUserInput.setText("")
        } else
            binding.symptomCard.symptomsSpinner.etUserInput.text =
                getSelectedSymptomsText(selectedSymptoms)
    }

    private fun getSelectedSymptomsText(selectedSymptoms: List<SymptomModel>): String {
        val otherSelected =
            selectedSymptoms.filter { it.symptom.startsWith(DefinedParams.Other, true) }
        val noSymptomsCount =
            selectedSymptoms.filter { it.symptom.startsWith(AssessmentDefinedParams.NoSymptoms, true) }.size
        val newWorseningSymptoms =
            selectedSymptoms.filter {
                it.symptom.startsWith(
                    getString(R.string.new_or_worsening_symptoms),
                    true
                )
            }
        val stringBuilder: StringBuilder = StringBuilder()
        if (noSymptomsCount == selectedSymptoms.size) {
            stringBuilder.append(getString(R.string.no))
        } else {
            if (otherSelected.isNullOrEmpty()) {
                stringBuilder.append((selectedSymptoms.size - noSymptomsCount))
            } else {
                stringBuilder.append((selectedSymptoms.size - noSymptomsCount) - otherSelected.size)
            }
        }
        if (newWorseningSymptoms.isNotEmpty()) {
            newWorseningSymptomsType(newWorseningSymptoms)
        } else {
            binding.symptomCard.newWorseningHypertensionSymptom.llRoot.visibility = View.GONE
            binding.symptomCard.newWorseningHypertensionSymptom.etUserInput.setText("")
            binding.symptomCard.newWorseningDiabetesSymptom.llRoot.visibility = View.GONE
            binding.symptomCard.newWorseningDiabetesSymptom.etUserInput.setText("")
        }
        stringBuilder.append(getString(R.string.empty_space))

        if (!otherSelected.isNullOrEmpty()) {
            stringBuilder.append(getString(R.string.other_selected))
            if (otherSelected.any { it.type.equals(AssessmentDefinedParams.Compliance_Type_Diabetes) }) {
                binding.symptomCard.otherDiabetesSymptom.llRoot.visibility = View.VISIBLE
            } else {
                binding.symptomCard.otherDiabetesSymptom.llRoot.visibility = View.GONE
                binding.symptomCard.otherDiabetesSymptom.etUserInput.setText("")
            }
            if (otherSelected.any { it.type.equals(AssessmentDefinedParams.Compliance_Type_Hypertension)  }) {
                binding.symptomCard.otherHypertensionSymptom.llRoot.visibility = View.VISIBLE
            } else {
                binding.symptomCard.otherHypertensionSymptom.llRoot.visibility = View.GONE
                binding.symptomCard.otherHypertensionSymptom.etUserInput.setText("")
            }
        } else {
            stringBuilder.append(getString(R.string.symptoms_selected))
            binding.symptomCard.otherDiabetesSymptom.llRoot.visibility = View.GONE
            binding.symptomCard.otherDiabetesSymptom.etUserInput.setText("")
            binding.symptomCard.otherHypertensionSymptom.llRoot.visibility = View.GONE
            binding.symptomCard.otherHypertensionSymptom.etUserInput.setText("")
        }
        return stringBuilder.toString()
    }

    private fun newWorseningSymptomsType(newWorseningSymptoms: List<SymptomModel>) {
        if (newWorseningSymptoms.any { it.type.equals(AssessmentDefinedParams.Compliance_Type_Diabetes)}) {
            binding.symptomCard.newWorseningDiabetesSymptom.llRoot.visibility = View.VISIBLE
        } else {
            binding.symptomCard.newWorseningDiabetesSymptom.llRoot.visibility = View.GONE
            binding.symptomCard.newWorseningDiabetesSymptom.etUserInput.setText("")
        }
        if (newWorseningSymptoms.any { it.type.equals(AssessmentDefinedParams.Compliance_Type_Hypertension) }) {
            binding.symptomCard.newWorseningHypertensionSymptom.llRoot.visibility = View.VISIBLE
        } else {
            binding.symptomCard.newWorseningHypertensionSymptom.llRoot.visibility = View.GONE
            binding.symptomCard.newWorseningHypertensionSymptom.etUserInput.setText("")
        }
    }

    private fun loadData(medicalComplianceList: List<MedicalComplianceEntity>?) {
        medicalComplianceList?.let { list ->
            binding.symptomCard.childCompliance.clearCheck()
            if (list.isNotEmpty()) {
                binding.symptomCard.childCompliance.visibility = View.VISIBLE
                loadRadioButtons(list, binding.symptomCard.childCompliance, 2)
            } else {
                binding.symptomCard.childCompliance.visibility = View.GONE
                binding.symptomCard.tvChildErrorMessage.visibility = View.GONE
            }
        }
    }

    private fun loadRadioButtons(
        list: List<MedicalComplianceEntity>,
        complianceRadioGroup: RadioGroup,
        parent: Int
    ) {
        complianceRadioGroup.removeAllViews()
        list.forEachIndexed { index, model ->
            val radioButton = RadioButton(complianceRadioGroup.context)
            radioButton.id = index
            radioButton.tag = model.id
            radioButton.setPadding(20.dp, 0, 20.dp, 0)
            val colorStateList = ColorStateList(
                arrayOf(
                    intArrayOf(-android.R.attr.state_checked),
                    intArrayOf(android.R.attr.state_checked)
                ), intArrayOf(
                    ContextCompat.getColor(
                        complianceRadioGroup.context,
                        R.color.purple
                    ),  // disabled
                    ContextCompat.getColor(complianceRadioGroup.context, R.color.purple) // enabled
                )
            )

            radioButton.buttonTintList = colorStateList
            radioButton.invalidate()
            radioButton.textSizeSsp = AssessmentDefinedParams.SSP16
            radioButton.text = model.name
            radioButton.layoutParams = RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.WRAP_CONTENT,
                RadioGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
            radioButton.setTextColor(
                ContextCompat.getColor(
                    complianceRadioGroup.context,
                    R.color.navy_blue
                )
            )
            radioButton.typeface =
                ResourcesCompat.getFont(complianceRadioGroup.context, R.font.inter_regular)
            complianceRadioGroup.addView(radioButton)
        }

        complianceRadioGroup.setOnCheckedChangeListener { _, id ->
            if (id > -1) {
                val selectedModel = list[id]
                if (parent == 1) {
                    showOrHideCompliances(selectedModel)
                    binding.symptomCard.otherComplianceReason.setText("")
                    binding.symptomCard.otherComplianceReason.visibility = View.GONE
                } else if (parent == 2) {
                    if (selectedModel.name.startsWith("other", true)) {
                        binding.symptomCard.otherComplianceReason.visibility = View.VISIBLE
                    } else {
                        binding.symptomCard.otherComplianceReason.setText("")
                        binding.symptomCard.otherComplianceReason.visibility = View.GONE
                    }
                }
                addOrRemoveValuesFromResultMap(selectedModel)
            }
        }
    }

    private fun showOrHideCompliances(selectedModel: MedicalComplianceEntity) {
        if (selectedModel.childExists) {
            viewModel.getMedicationChildComplianceList(selectedModel.id)
        } else hideComplianceOptions()
    }

    private fun hideComplianceOptions() {
        viewModel.complianceMap?.clear()
        binding.symptomCard.childCompliance.clearCheck()
        binding.symptomCard.childCompliance.visibility = View.GONE
        binding.symptomCard.tvChildErrorMessage.visibility = View.GONE
    }

    private fun addOrRemoveValuesFromResultMap(
        selectedModel: MedicalComplianceEntity
    ) {
        val map: HashMap<String, Any>
        if (viewModel.complianceMap != null) {
            map = HashMap<String, Any>()
            if (selectedModel.parentComplianceId == null) {
                viewModel.complianceMap?.clear()
            } else {
                val newList = ArrayList<HashMap<String, Any>>()
                viewModel.complianceMap?.forEach { map ->
                    if (map.containsKey(AssessmentDefinedParams.complianceId) && map[AssessmentDefinedParams.complianceId] == selectedModel.parentComplianceId) {
                        newList.add(map)
                    }
                }
                viewModel.complianceMap?.clear()
                viewModel.complianceMap?.addAll(newList)
            }
            if (selectedModel.childExists) {
                map[AssessmentDefinedParams.is_child_exists] = true
            }
            map[AssessmentDefinedParams.complianceId] = selectedModel.id
            map[DefinedParams.NAME] = selectedModel.name
            viewModel.complianceMap?.add(map)
        } else {
            viewModel.complianceMap = ArrayList()
            map = HashMap<String, Any>()
            map[AssessmentDefinedParams.complianceId] = selectedModel.id
            map[DefinedParams.NAME] = selectedModel.name
            if (selectedModel.childExists) {
                map[AssessmentDefinedParams.is_child_exists] = true
            }
            viewModel.complianceMap?.add(map)
        }
    }

    private fun showErrorMessage(message: String, view: TextView, titleView: TextView? = null) {
        titleView?.let { title ->
            formGenerator.scrollToView(binding.scrollView, title)
        }
        view.visibility = View.VISIBLE
        view.text = message
    }

    private fun hideErrorMessage(view: TextView) {
        view.visibility = View.GONE
    }

    private fun checkComplianceDiabetes(other: List<SymptomModel>): Boolean? {
        var isValid: Boolean? = null
        if (other.any { it.type.equals(AssessmentDefinedParams.Compliance_Type_Diabetes) }) {
            hideErrorMessage(binding.symptomCard.symptomsSpinner.tvErrorMessage)
            val otherDiabetesText =
                binding.symptomCard.otherDiabetesSymptom.etUserInput.text
            if (otherDiabetesText.isNullOrBlank()) {
                isValid = false
                showErrorMessage(
                    getString(R.string.default_user_input_error),
                    binding.symptomCard.otherDiabetesSymptom.tvErrorMessage
                )
            } else {
                val filteredOther = other.first { symptomOther ->
                    symptomOther.type.equals(AssessmentDefinedParams.Compliance_Type_Diabetes)
                }
                filteredOther.otherSymptom = otherDiabetesText.toString()
                hideErrorMessage(binding.symptomCard.otherDiabetesSymptom.tvErrorMessage)
            }
        } else {
            hideErrorMessage(binding.symptomCard.otherDiabetesSymptom.tvErrorMessage)
        }
        return isValid
    }

    private fun checkComplianceHypertension(other: List<SymptomModel>): Boolean? {
        var isValid: Boolean? = null
        if (other.any { it.type.equals(AssessmentDefinedParams.Compliance_Type_Hypertension) }) {
            hideErrorMessage(binding.symptomCard.symptomsSpinner.tvErrorMessage)
            val otherHyperTensionText =
                binding.symptomCard.otherHypertensionSymptom.etUserInput.text
            if (otherHyperTensionText.isNullOrBlank()) {
                isValid = false
                showErrorMessage(
                    getString(R.string.default_user_input_error),
                    binding.symptomCard.otherHypertensionSymptom.tvErrorMessage
                )
            } else {
                val filteredOther = other.first { symptomOther ->
                    symptomOther.type.equals(AssessmentDefinedParams.Compliance_Type_Hypertension)
                }
                filteredOther.otherSymptom = otherHyperTensionText.toString()
                hideErrorMessage(binding.symptomCard.otherHypertensionSymptom.tvErrorMessage)
            }
        } else {
            hideErrorMessage(binding.symptomCard.otherHypertensionSymptom.tvErrorMessage)
        }
        return isValid
    }

    private fun checkNewWorseningDiabetes(newWorsening: List<SymptomModel>): Boolean? {
        var isValid: Boolean? = null
        if (newWorsening.any { it.type.equals(AssessmentDefinedParams.Compliance_Type_Diabetes) }) {
            hideErrorMessage(binding.symptomCard.symptomsSpinner.tvErrorMessage)
            val newWorseningDiabetesText =
                binding.symptomCard.newWorseningDiabetesSymptom.etUserInput.text
            if (newWorseningDiabetesText.isNullOrBlank()) {
                isValid = false
                showErrorMessage(
                    getString(R.string.default_user_input_error),
                    binding.symptomCard.newWorseningDiabetesSymptom.tvErrorMessage
                )
            } else {
                val filteredNewWorsening = newWorsening.first { symptomNewWorsening ->
                    symptomNewWorsening.type.equals(AssessmentDefinedParams.Compliance_Type_Diabetes)
                }
                filteredNewWorsening.otherSymptom = newWorseningDiabetesText.toString()
                hideErrorMessage(binding.symptomCard.newWorseningDiabetesSymptom.tvErrorMessage)
            }
        } else {
            hideErrorMessage(binding.symptomCard.newWorseningDiabetesSymptom.tvErrorMessage)
        }
        return isValid
    }

    private fun validateSymptom(): Boolean {
        var isValidData = true
        if (!viewModel.selectedSymptoms.value.isNullOrEmpty()) {
            viewModel.selectedSymptoms.value?.let {
                val selectedSymptom = viewModel.selectedSymptoms.value
                if (!selectedSymptom.isNullOrEmpty()) {
                    val other = selectedSymptom.filter {
                        it.symptom.startsWith(
                            DefinedParams.Other,
                            true
                        )
                    }
                    if (other.isNotEmpty()) {
                        checkComplianceDiabetes(other)?.let {
                            isValidData = it
                        }
                        checkComplianceHypertension(other)?.let {
                            isValidData = it
                        }
                    } else
                        hideErrorMessage(binding.symptomCard.symptomsSpinner.tvErrorMessage)
                    val newWorsening = selectedSymptom.filter {
                        it.symptom.startsWith(
                            getString(R.string.new_or_worsening_symptoms),
                            true
                        )
                    }
                    if (newWorsening.isNotEmpty()) {
                        checkNewWorseningDiabetes(newWorsening)?.let {
                            isValidData = it
                        }
                        checkNewWorseningHypertension(newWorsening)?.let {
                            isValidData = it
                        }
                    } else
                        hideErrorMessage(binding.symptomCard.symptomsSpinner.tvErrorMessage)
                } else
                    hideErrorMessage(binding.symptomCard.symptomsSpinner.tvErrorMessage)
            }
        } else {
            isValidData = false
            showErrorMessage(
                getString(R.string.error_message_spinner),
                binding.symptomCard.symptomsSpinner.tvErrorMessage,
                binding.symptomCard.symptomsSpinner.tvTitle
            )
        }
        return isValidData
    }

    private fun getSymptomsList(list: List<SymptomModel>): ArrayList<HashMap<String, Any>> {
        val resultList = ArrayList<HashMap<String, Any>>()
        list.forEach {
            val map = HashMap<String, Any>()
            map[DefinedParams.NAME] = it.symptom
            map[AssessmentDefinedParams.symptom_id] = it.id
            it.otherSymptom?.let { other ->
                map[AssessmentDefinedParams.other_symptom] = other
            }
            it.type?.let { type ->
                map[Screening.type] = type
            }
            resultList.add(map)
        }

        return resultList
    }

    private fun checkNewWorseningHypertension(newWorsening: List<SymptomModel>): Boolean? {
        var isValid: Boolean? = null
        if (newWorsening.any { it.type.equals(AssessmentDefinedParams.Compliance_Type_Hypertension) }) {
            hideErrorMessage(binding.symptomCard.symptomsSpinner.tvErrorMessage)
            val newWorseningHyperTensionText =
                binding.symptomCard.newWorseningHypertensionSymptom.etUserInput.text
            if (newWorseningHyperTensionText.isNullOrBlank()) {
                isValid = false
                showErrorMessage(
                    getString(R.string.default_user_input_error),
                    binding.symptomCard.newWorseningHypertensionSymptom.tvErrorMessage
                )
            } else {
                val filteredNewWorsening = newWorsening.first { symptomNewWorsening ->
                    symptomNewWorsening.type.equals(AssessmentDefinedParams.Compliance_Type_Hypertension)
                }
                filteredNewWorsening.otherSymptom = newWorseningHyperTensionText.toString()
                hideErrorMessage(binding.symptomCard.newWorseningHypertensionSymptom.tvErrorMessage)
            }
        } else {
            hideErrorMessage(binding.symptomCard.newWorseningHypertensionSymptom.tvErrorMessage)
        }
        return isValid
    }

    private fun validateSymptomsAndCompliance(
        resultHashMap: HashMap<String, Any>,
        serverData: List<FormLayout?>?,
        unitGenericType: String
    ) {
        if (validateCompliance() && validateSymptom()) {
            if (viewModel.complianceMap != null) {
                viewModel.complianceMap?.let {
                    resultHashMap[AssessmentDefinedParams.compliance] = it
                }
            }
            if (!viewModel.selectedSymptoms.value.isNullOrEmpty()) {
                viewModel.selectedSymptoms.value?.let {
                    //  To do backend need to change symptoms name
                    resultHashMap[AssessmentDefinedParams.symptomsDTO] =
                        getSymptomsList(it)
                }
            }
            saveAssessmentBaseValues(resultHashMap, serverData, unitGenericType)
        }
    }

    private fun otherComplianceCheck(complianceMap: ArrayList<HashMap<String, Any>>): Boolean? {
        var isValid: Boolean? = null
        if (binding.symptomCard.otherComplianceReason.visibility == View.VISIBLE) {
            val reason = binding.symptomCard.otherComplianceReason.text
            if (reason.isNullOrBlank()) {
                isValid = false
                showErrorMessage(
                    getString(R.string.default_user_input_error),
                    binding.symptomCard.tvChildErrorMessage
                )
            } else {
                complianceMap[1][AssessmentDefinedParams.other_compliance] =
                    reason.toString()
                hideErrorMessage(binding.symptomCard.tvChildErrorMessage)
            }
        } else
            hideErrorMessage(binding.symptomCard.tvChildErrorMessage)
        return isValid
    }

    private fun saveAssessmentBaseValues(
        resultHashMap: HashMap<String, Any>,
        serverData: List<FormLayout?>?,
        unitGenericType: String
    ) {
        viewModel.bioDataMap?.let {
            resultHashMap[Screening.DateOfBirth] = it[Screening.DateOfBirth] as String
            resultHashMap[Screening.is_regular_smoker] = it[Screening.is_regular_smoker] as Boolean
            resultHashMap[DefinedParams.BioData] = it
        }
        viewModel.bioMetric?.let {
            resultHashMap[DefinedParams.Gender] = it[DefinedParams.Gender] as String
            resultHashMap[Screening.BioMetrics] = it
        }
        processValuesAndProceed(resultHashMap, serverData, unitGenericType)
    }

    private fun validateCompliance(): Boolean {
        var isValidData = true
        if (viewModel.complianceMap != null) {
            viewModel.complianceMap?.let { complianceMap ->
                if (complianceMap.isNotEmpty()) {
                    val selectedModel = complianceMap[0]
                    if (selectedModel.containsKey(AssessmentDefinedParams.is_child_exists) && (selectedModel[AssessmentDefinedParams.is_child_exists] as? Boolean) == true) {
                        if (complianceMap.size < 2) {
                            isValidData = false
                            showErrorMessage(
                                getString(R.string.default_user_input_error),
                                binding.symptomCard.tvChildErrorMessage
                            )
                        } else
                            otherComplianceCheck(complianceMap)?.let {
                                isValidData = it
                            }
                    } else
                        hideErrorMessage(binding.symptomCard.complianceSpinner.tvErrorMessage)
                } else
                    hideErrorMessage(binding.symptomCard.complianceSpinner.tvErrorMessage)
            }
        } else
            hideErrorMessage(binding.symptomCard.complianceSpinner.tvErrorMessage)

        return isValidData
    }

    private fun processValuesAndProceed(
        resultHashMap: HashMap<String, Any>,
        serverData: List<FormLayout?>?,
        unitGenericType: String
    ) {
        val map = HashMap<String, Any>()
        map.putAll(resultHashMap)
        calculateBloodGlucose(map, true) { (fbs, rbs) ->
            if (fbs != null) {
                viewModel.setFbsBloodGlucose(fbs)
            }
            if (rbs != null) {
                viewModel.setRbsBloodGlucose(rbs)
            }
        }
        CommonUtils.calculateBMI(map)
        CommonUtils.calculateAverageBloodPressure(map)
        var isConfirmDiagnosis = false
        patientDetailViewModel.patientDetailsLiveData.value?.data?.let { searchResponse ->
            isConfirmDiagnosis = searchResponse.isConfirmDiagnosis
            //temporary id ,backend in future may change
            map[AssessmentDefinedParams.relatedPersonFhirId] = searchResponse.id.toString()
        }
        map[AssessmentDefinedParams.assessmentType] = DefinedParams.Assessment
        //temperory process id, in future need to change from backend
        map[AssessmentDefinedParams.assessmentProcessType] = AssessmentDefinedParams.ncd_UpperCase
        map[AssessmentDefinedParams.assessmentTakenOn] = DateUtils.getTodayDateDDMMYYYY()
        //backend need to change organization id
        map[AssessmentDefinedParams.assessmentOrganizationId] = SecuredPreference.getOrganizationFhirId()
        calculateFurtherAssessment(map, unitGenericType)
        calculateProvisionalDiagnosis(
            map, isConfirmDiagnosis,
            bpViewModel.getSystolicAverage(), bpViewModel.getDiastolicAverage(),
            viewModel.getFbsBloodGlucose(), viewModel.getRbsBloodGlucose(),
            CommonUtils.getMeasurementTypeValues(map)
        )
        val resultOne = bpViewModel.getRiskEntityListLiveData.value
        val baseType: Type = object : TypeToken<ArrayList<RiskClassificationModel>>() {}.type
        if (resultOne?.isNotEmpty() == true) {
            val resultList =
                Gson().fromJson<ArrayList<RiskClassificationModel>>(
                    resultOne[0].nonLabEntity,
                    baseType
                )
            if (viewModel.assessmentType.equals(AssessmentDefinedParams.ncd)) {
                calculateCVDRiskFactor(
                    map,
                    ArrayList(resultList),
                    bpViewModel.getSystolicAverage()
                )
            }
        }
        map[Screening.UnitMeasurement] = SecuredPreference.getUnitMeasurementType()
        try {
            var result = serverData?.let {
                FormResultComposer().groupValues(
                    context = requireContext(),
                    serverData = it,
                    map,
                    bmiCategoryGroupId = Screening.bp_log
                )
            }
            val organizationId = hashMapOf<String, Any>(
                //backend need to change organization id
                AssessmentDefinedParams.organizationId to SecuredPreference.getOrganizationFhirId(),
                AssessmentDefinedParams.userId to SecuredPreference.getUserFhirId()
            )
            val provenance = hashMapOf<String, Any>(
                DefinedParams.Provenance to organizationId
            )
            val bpLog = result?.second?.get(Screening.bp_log) as HashMap<String, Any>
            if (bpLog.containsKey(AssessmentDefinedParams.Temperature)) {
                result.second[AssessmentDefinedParams.Temperature] = bpLog[AssessmentDefinedParams.Temperature] as Double
            }
            val bioMetric = result?.second?.get(Screening.BioMetrics) as HashMap<String, Any>
            bioMetric[Screening.BMI] = bpLog[Screening.BMI] as Double
            bpLog.apply {
                remove(AssessmentDefinedParams.Temperature)
                remove(Screening.Weight)
                remove(Screening.Height)
                remove(Screening.BMI)
            }
            result?.second?.apply {
                this[AssessmentDefinedParams.encounter] = provenance
                remove(DefinedParams.Gender)
                remove(Screening.is_regular_smoker)
                remove(Screening.DateOfBirth)
                remove(Screening.referredReasons)
            }
            val bioData = result?.second?.get(DefinedParams.BioData) as HashMap<String, Any>
            bioData.apply {
                remove(Screening.is_regular_smoker)
                remove(Screening.DateOfBirth)
            }
            result = Pair(StringConverter.convertGivenMapToString(result.second), result.second)
            if (result != null) {
                result.first?.let {
                    viewModel.saveAssessmentInformation(it)
                }
            }
        } catch (_: Exception) {
            //Exception - Catch block
        }
    }

    private fun calculateFurtherAssessment(map: HashMap<String, Any>, unitGenericType: String) {
        assessmentJSON?.first { it.viewType == ViewType.VIEW_TYPE_FORM_BP }?.let {
            bpViewModel.calculateBPValues(it, map)
        }

        val assessmentConditionResult = CommonUtils.checkAssessmentCondition(
            bpViewModel.getSystolicAverage(),
            bpViewModel.getDiastolicAverage(),
            phQ4Score = null,
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

    private fun showHeightWeight(height: Double?, weight: Double?) {
        formGenerator.getViewByTag(Screening.Height)?.let { editText ->
            if (editText is AppCompatEditText) {
                val formattedText = CommonUtils.getDecimalFormatted(height)
                editText.setText(formattedText)
            }
        }
        formGenerator.getViewByTag(Screening.Weight)?.let { editText ->
            if (editText is AppCompatEditText) {
                val formattedText = CommonUtils.getDecimalFormatted(weight)
                editText.setText(formattedText)
            }
        }
    }

    companion object {
        const val TAG: String = "AssessmentNCDFragment"
        fun newInstance(): AssessmentNCDFragment {
            return AssessmentNCDFragment()
        }
    }

    override fun loadLocalCache(id: String, localDataCache: Any, selectedParent: Long?) {

    }

    override fun onPopulate(targetId: String) {
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
        val unitGenericType = SecuredPreference.getUnitMeasurementType()
        if (binding.symptomCard.root.isVisible()) {
            if (resultMap != null) {
                validateSymptomsAndCompliance(resultMap, serverData, unitGenericType)
            }
        } else {
            if (resultMap != null) {
                saveAssessmentBaseValues(resultMap, serverData, unitGenericType)
            }
        }
    }

    override fun onRenderingComplete() {
    }

    override fun onUpdateInstruction(id: String, selectedId: Any?) {
    }

    override fun onInformationHandling(
        id: String,
        noOfDays: Int,
        enteredDays: Int?,
        resultMap: HashMap<String, Any>?
    ) {
    }

    override fun onAgeCheckForPregnancy() {
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnSubmit.id -> {
                formGenerator.formSubmitAction(v)
            }
        }
    }

    fun getCurrentAnsweredStatus(): Boolean {
        return formGenerator.getResultMap().isNotEmpty()
    }
}