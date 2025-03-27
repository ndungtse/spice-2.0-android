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
        when (viewModel.referralStatus) {
            ReferralStatus.Referred.name -> {
                viewModel.nearestFacilityLiveData.value?.data?.let { siteList ->
                    loadPhuSitesList(siteList)
                }
                binding.labelPhuReferred.visible()
                binding.etPhuChange.visible()
                binding.riskResultLayout.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.attention_color)
                binding.riskResultLayout.text = getString(R.string.referred_for_further_assessment)
            }

            else -> {
                binding.labelPhuReferred.gone()
                binding.etPhuChange.gone()
                binding.riskResultLayout.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.green_attention_color)
                binding.riskResultLayout.text = getString(R.string.no_refferral_treatment_required)
            }
        }
    }

    private fun loadPhuSitesList(healthFacilityList: ArrayList<Map<String, Any>>) {
        val adapter = CustomSpinnerAdapter(requireContext())
        adapter.setData(healthFacilityList)
        binding.etPhuChange.adapter = adapter
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
                        val selectedId = it[DefinedParams.id] as String?
                        if (selectedId != DefinedParams.DefaultID) {
                            viewModel.otherAssessmentDetails[AssessmentDefinedParams.ReferredPHUSiteID] = selectedId.toString()
                        } else {
                            if (viewModel.otherAssessmentDetails.containsKey(AssessmentDefinedParams.ReferredPHUSiteID))
                                viewModel.otherAssessmentDetails.remove(AssessmentDefinedParams.ReferredPHUSiteID)
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /**
                     * this method is not used
                     */
                }
            }
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
        getStatus(viewModel.referralStatus)?.let {
            bindSummaryView(
                getString(R.string.patient_status),
                it
            )
        }

        listSummaryData.forEach { item ->
            item.value?.let {
                when (item.id) {
                    AssessmentDefinedParams.FamilyPlanningMethods -> renderDangerSigns(item.title, listSummaryData)
                    AssessmentDefinedParams.SpecifySideEffects, AssessmentDefinedParams.CondomsStatus, AssessmentDefinedParams.Contraceptive -> bindSummaryView(item.title, it)
                }
            }
        }
    }

    private fun renderDangerSigns(title: String?, summaryData: MutableList<AssessmentSummaryModel>) {
        val otherConcernSymptoms = viewModel.assessmentStringLiveData.value?.let {
            val jsonObject = JSONObject(it)
            jsonObject.optJSONObject(familyPlanning.lowercase())
                ?.optJSONObject(AssessmentDefinedParams.FamilyPlanningDetails)
                ?.optString(AssessmentDefinedParams.OtherFamilyPlanningMethod)
        }
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
            val result = if (!otherConcernSymptoms.isNullOrBlank()) {
                requireContext().getString(R.string.other_value, formattedResult, otherConcernSymptoms)
            } else formattedResult
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