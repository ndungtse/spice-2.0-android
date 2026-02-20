package com.medtroniclabs.spice.ui.assessment.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.familyPlanning
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.databinding.FragmentAssessmentFamilyPlanningSummaryBinding
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.OtherMethodSpecify
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.model.AssessmentSummaryModel
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.assessment.referrallogic.utils.ReferralStatus
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import org.json.JSONObject


class AssessmentFamilyPlanningSummaryFragment : BaseFragment(), View.OnClickListener {
    private val viewModel: AssessmentViewModel by activityViewModels()
    lateinit var binding: FragmentAssessmentFamilyPlanningSummaryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAssessmentFamilyPlanningSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObservers()
    }

    private fun attachObservers() {
        viewModel.assessmentStringLiveData.value?.let {
            updateStatusBar()
            createSummaryView(createListSummaryData(it))
        }
    }

    private fun initView() {
        viewModel.setUserJourney(AnalyticsDefinedParams.FAMILYPLANNINGSUMMARY)
        binding.btnDone.safeClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnDone -> {
                viewModel.setUserJourney(AnalyticsDefinedParams.DONEBUTTONTRIGGERED)
                viewModel.updateOtherAssessmentDetails()
            }
        }
    }

    private fun createListSummaryData(data: String): MutableList<AssessmentSummaryModel>? {
        return viewModel.formLayoutsLiveData.value?.data?.formLayout?.filter { it.isSummary == true }?.map { formLayout ->
            AssessmentSummaryModel(
                title = formLayout.titleSummary ?: formLayout.title,
                id = formLayout.id,
                cultureValue = formLayout.titleCulture,
                value = AssessmentCommonUtils.getValueOfKeyFromMap(
                    StringConverter.stringToMap(data),
                    formLayout.id,
                    menuType = familyPlanning.lowercase()
                )
            )
        }?.toMutableList()
    }

    private fun updateStatusBar() {
        // Always hide PHU fields
        binding.labelPhuReferred.gone()
        binding.etPhuChange.gone()
        
        when (viewModel.referralStatus) {
            ReferralStatus.Referred.name -> {
                // Load referral_facility_type dropdown in place of PHU field
                loadReferralFacilityTypeDropdown()
                binding.riskResultLayout.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.attention_color)
                binding.riskResultLayout.text = getString(R.string.referred_for_further_assessment)
            }

            else -> {
                binding.riskResultLayout.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.green_attention_color)
                binding.riskResultLayout.text = getString(R.string.no_refferral_treatment_required)
            }
        }
    }

    /**
     * Loads referral_facility_type dropdown in place of PHU field.
     * Uses the existing labelPhuReferred and etPhuChange views.
     */
    private fun loadReferralFacilityTypeDropdown() {
        val formLayout = viewModel.formLayoutsLiveData.value?.data?.formLayout?.find { 
            it.id == AssessmentDefinedParams.ReferralFacilityType 
        } ?: return

        val optionsList = formLayout.optionsList ?: return
        
        // Get current value from assessment data
        val currentValue = viewModel.assessmentStringLiveData.value?.let { data ->
            AssessmentCommonUtils.getValueOfKeyFromMap(
                StringConverter.stringToMap(data),
                AssessmentDefinedParams.ReferralFacilityType,
                menuType = familyPlanning.lowercase()
            )
        }

        // Create dropdown list from optionsList
        val dropDownList = ArrayList<Map<String, Any>>()
        dropDownList.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to getString(R.string.select),
                DefinedParams.id to DefinedParams.DefaultID
            )
        )
        optionsList.forEach { option ->
            dropDownList.add(
                hashMapOf<String, Any>(
                    DefinedParams.NAME to (option[DefinedParams.NAME] as? String ?: ""),
                    DefinedParams.id to (option[DefinedParams.id] as? String ?: "")
                )
            )
        }

        // Update label text
        binding.labelPhuReferred.text = formLayout.titleSummary ?: formLayout.title ?: getString(R.string.separator_hyphen)
        binding.labelPhuReferred.visible()

        // Setup adapter
        val adapter = CustomSpinnerAdapter(requireContext())
        adapter.setData(dropDownList)
        binding.etPhuChange.adapter = adapter

        // Set selected value - match by ID from optionsList
        var defaultPosition = 0
        currentValue?.let { value ->
            // Try to match by ID first, then by name
            dropDownList.forEachIndexed { index, item ->
                val optionId = item[DefinedParams.id] as? String
                val optionName = item[DefinedParams.NAME] as? String
                if (optionId == value || optionName == value) {
                    defaultPosition = index
                }
            }
        }
        binding.etPhuChange.post {
            binding.etPhuChange.setSelection(defaultPosition, false)
        }

        // Handle selection
        binding.etPhuChange.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    itemId: Long
                ) {
                    val selectedItem = adapter.getData(position = pos)
                    selectedItem?.let {
                    val selectedId = it[DefinedParams.id] as? String?
                    if (selectedId != null && selectedId != DefinedParams.DefaultID) {
                        viewModel.otherAssessmentDetails[AssessmentDefinedParams.ReferralFacilityType] = selectedId
                    } else {
                        viewModel.otherAssessmentDetails.remove(AssessmentDefinedParams.ReferralFacilityType)
                    }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    // No action needed
                }
            }
        
        binding.etPhuChange.visible()
    }

    private fun createSummaryView(
        listSummaryData: MutableList<AssessmentSummaryModel>?
    ) {
        listSummaryData?.let {summaryData ->
            binding.emptyErrorMessage.visibility = View.GONE
            binding.parentLayout.visibility = View.VISIBLE
            binding.parentLayout.removeAllViews()
            composeSummaryView(summaryData)
        } ?: kotlin.run {
            showErrorInSummary()
        }
    }

    private fun composeSummaryView(listSummaryData: MutableList<AssessmentSummaryModel>) {
        listSummaryData.forEach { item ->
            val fieldId = item.id
            when (fieldId) {
                AssessmentDefinedParams.FamilyPlanningMethods -> {
                    item.value?.let {
                        renderDangerSigns(item.title, listSummaryData)
                    }
                }
                AssessmentDefinedParams.ReferralFacilityType -> {
                    // Skip rendering in summary list - it's shown in place of PHU field
                    // when referral status is "Referred"
                }
                AssessmentDefinedParams.DesireForChildrenInFuture -> {
                    // Convert spinner ID to display name
                    item.value?.let { value ->
                        val displayValue = getSpinnerDisplayValue(fieldId, value)
                        bindSummaryView(item.title, displayValue)
                    }
                }
                AssessmentDefinedParams.SpecifySideEffects, AssessmentDefinedParams.CondomsStatus, AssessmentDefinedParams.Contraceptive, AssessmentDefinedParams.MemberUsingAnyFamilyPlanning, AssessmentDefinedParams.NeedOfOtherFamilyPlanning, AssessmentDefinedParams.IsAnySideEffects -> {
                    item.value?.let {
                        bindSummaryView(item.title, it)
                    }
                }
                else -> {
                    // Handle other fields with values
                    item.value?.let {
                        bindSummaryView(item.title, it)
                    }
                }
            }
        }
    }

    /**
     * Converts spinner field ID to display name using optionsList from formLayout
     */
    private fun getSpinnerDisplayValue(fieldId: String, valueId: String): String {
        val formLayout = viewModel.formLayoutsLiveData.value?.data?.formLayout?.find { 
            it.id == fieldId 
        } ?: return valueId

        val optionsList = formLayout.optionsList ?: return valueId
        optionsList.forEach { option ->
            val optionId = option[DefinedParams.id] as? String
            if (optionId == valueId) {
                return option[DefinedParams.NAME] as? String ?: valueId
            }
        }
        
        return valueId
    }

    private fun renderDangerSigns(title: String?, summaryData: MutableList<AssessmentSummaryModel>) {
//        val otherConcernSymptoms = viewModel.assessmentStringLiveData.value?.let {
//            val jsonObject = JSONObject(it)
//            jsonObject.optJSONObject(familyPlanning.lowercase())
//                ?.optJSONObject(AssessmentDefinedParams.FamilyPlanningDetails)
//                ?.optString(AssessmentDefinedParams.OtherFamilyPlanningMethod)
//        }
        val symptomsMeta = viewModel.symptomTypeListResponse.value ?: return
        val titleEntities = symptomsMeta.filter { it.isTitle }.sortedBy { it.displayOrder ?: Int.MAX_VALUE }
        val methodEntities = symptomsMeta.filter { !it.isTitle }
        val categoryMap = mutableMapOf<String, String>()
        methodEntities.forEach { entity ->
            val entityDisplayOrder = entity.displayOrder ?: Int.MAX_VALUE
            val category = titleEntities.lastOrNull { (it.displayOrder ?: Int.MIN_VALUE) < entityDisplayOrder }?.symptom
            if (category != null) {
                categoryMap[entity.symptom] = category
            }
        }

        summaryData.find { it.id == AssessmentDefinedParams.FamilyPlanningMethods }?.let { item ->
            val methodList = item.value?.split(", ") ?: emptyList()
            val hasOtherMethod = OtherMethodSpecify in methodList
            val groupedMethods = methodList.filter { it != OtherMethodSpecify }
                .groupBy { categoryMap[it] ?: "Unknown" }
            var formattedResult = groupedMethods.entries.joinToString(", ") { (category, methods) ->
                "$category - (${methods.joinToString(", ")})"
            }
            if (hasOtherMethod) {
                formattedResult = if (formattedResult.isNotBlank()) {
                    "$formattedResult, $OtherMethodSpecify"
                } else {
                    OtherMethodSpecify
                }
            }

            // val result = if (!otherConcernSymptoms.isNullOrBlank()) {
            //     requireContext().getString(R.string.other_value, formattedResult, otherConcernSymptoms)
            // } else formattedResult
            val result = formattedResult
            bindSummaryView(title, result.ifBlank { getString(R.string.seperator_hyphen) })
        }
    }


    private fun showErrorInSummary() {
        binding.emptyErrorMessage.visibility = View.VISIBLE
        binding.parentLayout.visibility = View.GONE
    }

    fun getStatus(referralStatus: String?): String? {
        return when (referralStatus) {
            ReferralStatus.Referred.name -> getString(R.string.referred)
            ReferralStatus.OnTreatment.name -> getString(R.string.on_treatment)
            ReferralStatus.Recovered.name -> getString(R.string.recovered)
            else -> {
                null
            }
        }
    }

    private fun bindSummaryView(title: String?, value: String?, valueTextColor: Int? = null) {
        binding.parentLayout.addView(
            AssessmentCommonUtils.addViewSummaryLayout(
                title,
                value,
                valueTextColor,
                requireContext()
            )
        )
    }

    companion object {
        const val TAG = "AssessmentFamilyPlanningSummaryFragment"
        fun newInstance(): AssessmentFamilyPlanningSummaryFragment {
            return AssessmentFamilyPlanningSummaryFragment()
        }
    }

    fun getCurrentAnsweredStatus():Boolean {
        return viewModel.otherAssessmentDetails.isNotEmpty()
    }

}