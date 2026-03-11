package com.medtroniclabs.spice.ui.assessment.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.databinding.FragmentAssessmentFamilyPlanningSummaryBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel

class AssessmentFamilyPlanningSummaryFragment : BaseFragment(), View.OnClickListener {
    private val viewModel: AssessmentViewModel by activityViewModels()
    lateinit var binding: FragmentAssessmentFamilyPlanningSummaryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAssessmentFamilyPlanningSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObservers()
    }

    private fun attachObservers() {
        viewModel.assessmentStringLiveData.value?.let {
            renderSummaryData(it)
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
                viewModel.updateFamilyPlanningAssessmentDetails()
            }
        }
    }

    /**
     * Renders summary data
     */
    private fun renderSummaryData(data: String) {
        val convertedMap = StringConverter.stringToMap(data)
        if (convertedMap.isEmpty()) {
            showErrorInSummary()
        } else {
            binding.emptyErrorMessage.gone()
            val fpMap = convertedMap[DefinedParams.familyPlanning.lowercase()] as Map<*, *>
            val assessmentMap = fpMap[AssessmentDefinedParams.FamilyPlanningDetails] as Map<*, *>
            val noOfChildren = CommonUtils.getInteger(assessmentMap[AssessmentDefinedParams.NumberOfLivingChildren])
            val desireForChildren = assessmentMap[AssessmentDefinedParams.DesireForChildrenInFuture] as? String
            val familyPlanningMethod = assessmentMap[AssessmentDefinedParams.FamilyPlanningMethods] as? List<*>
            if (AssessmentDefinedParams.FP_METHOD_STERILIZATION_MALE.equals(familyPlanningMethod?.firstOrNull()?.toString(), true) ||
                AssessmentDefinedParams.FP_METHOD_STERILIZATION_FEMALE.equals(familyPlanningMethod?.firstOrNull()?.toString(), true)
            ) {
                bindSummaryView(getString(R.string.counselling_message), getString(R.string.separator_double_hyphen))
                return
            }
            when {
                AssessmentDefinedParams.DesireYesWithin2Yrs.equals(desireForChildren, true) ||
                    (
                        AssessmentDefinedParams.DesireUnsure.equals(desireForChildren, true) &&
                            noOfChildren == 0
                    ) -> {
                    bindSummaryView(getString(R.string.counselling_message), getString(R.string.short_acting_message))
                }

                AssessmentDefinedParams.DesireNoMore.equals(desireForChildren, true) ||
                    (
                        AssessmentDefinedParams.DesireUnsure.equals(desireForChildren, true) &&
                            noOfChildren >= 2
                    ) -> {
                    bindSummaryView(getString(R.string.counselling_message), getString(R.string.permanent_acting_message))
                }

                AssessmentDefinedParams.DesireYesAfter2Yrs.equals(desireForChildren, true) ||
                    (
                        AssessmentDefinedParams.DesireUnsure.equals(desireForChildren, true) &&
                            noOfChildren >= 1
                    ) -> {
                    bindSummaryView(getString(R.string.counselling_message), getString(R.string.long_acting_message))
                }
            }
        }
    }

    private fun showErrorInSummary() {
        binding.emptyErrorMessage.visibility = View.VISIBLE
        binding.parentLayout.visibility = View.GONE
    }

    private fun bindSummaryView(
        title: String?,
        value: String?,
        valueTextColor: Int? = null,
    ) {
        binding.parentLayout.addView(
            AssessmentCommonUtils.addViewSummaryLayout(
                title,
                value,
                valueTextColor,
                requireContext(),
            ),
        )
    }

    companion object {
        const val TAG = "AssessmentFamilyPlanningSummaryFragment"

        fun newInstance(): AssessmentFamilyPlanningSummaryFragment = AssessmentFamilyPlanningSummaryFragment()
    }

    fun getCurrentAnsweredStatus(): Boolean = viewModel.otherAssessmentDetails.isNotEmpty()
}
