package com.medtroniclabs.spice.ui.assessment.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
import com.medtroniclabs.spice.common.DateUtils.DATE_ddMMyyyy
import com.medtroniclabs.spice.common.DateUtils.calculateAge
import com.medtroniclabs.spice.common.DateUtils.calculateGestationalAge
import com.medtroniclabs.spice.common.DateUtils.formatGestationalAge
import com.medtroniclabs.spice.common.DateUtils.getLastMenstrualDate
import com.medtroniclabs.spice.common.DateUtils.getV2YearMonthAndWeek
import com.medtroniclabs.spice.databinding.FragmentPregnancyDetailsRmnchBinding
import com.medtroniclabs.spice.db.entity.PregnancyDetail
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

class PregnancyDetailsRMNCHFragment : BaseFragment() {
    private lateinit var binding: FragmentPregnancyDetailsRmnchBinding
    private val viewModel: AssessmentViewModel by activityViewModels()

    companion object {
        const val TAG = "PregnancyDetailsRMNCHFragment"

        fun newInstance(): PregnancyDetailsRMNCHFragment = PregnancyDetailsRMNCHFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPregnancyDetailsRmnchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObserver()
    }

    private fun initView() {
        // Check if pregnancy detail is already available
        viewModel.pregnancyDetail?.let {
            showPregnancyDetails(it)
        }
    }

    private fun attachObserver() {
        // Observe member details to fetch pregnancy details when available
        viewModel.memberDetailsLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    // Fetch pregnancy details when member details are loaded
                    viewModel.getPregnancyDetailInformation()
                    // Use postDelayed to allow coroutine to complete
                    // The delay ensures the suspend function has time to execute
                    view?.postDelayed({
                        showPregnancyDetails(viewModel.pregnancyDetail)
                    }, AssessmentDefinedParams.PREGNANCY_DETAILS_DELAY_300_MS)
                }
                else -> {}
            }
        }

        // Observe member clinical data to get visit count for ANC visit
        viewModel.memberClinicalLiveData.observe(viewLifecycleOwner) { _ ->
            if (viewModel.workflowName == RMNCH.ANC) {
                // Update ANC visit count when clinical data is available
                view?.postDelayed({
                    showPregnancyDetails(viewModel.pregnancyDetail)
                }, AssessmentDefinedParams.PREGNANCY_DETAILS_DELAY_100_MS)
            }
        }

        // Also check if pregnancy detail becomes available later
        // This handles cases where data is loaded asynchronously
        view?.postDelayed({
            if (viewModel.pregnancyDetail != null && binding.llPregnancyInfo.childCount == 0) {
                showPregnancyDetails(viewModel.pregnancyDetail)
            }
        }, AssessmentDefinedParams.PREGNANCY_DETAILS_DELAY_500_MS)
    }

    private fun showPregnancyDetails(pregnancyDetail: PregnancyDetail?) {
        binding.llPregnancyInfo.removeAllViews()

        pregnancyDetail?.let { detail ->
            // 1. Last Menstrual Period (LMP)
            detail.lastMenstrualPeriod?.let { lmp ->
                val formattedLMP = DateUtils.convertDateFormat(
                    lmp,
                    DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                    DATE_ddMMyyyy,
                )
                createSummary(
                    getString(R.string.last_menstrual_period),
                    formattedLMP,
                )
            }

            // 2. Estimated Delivery Date (EDD)
            detail.estimatedDeliveryDate?.let { edd ->
                val formattedEDD = DateUtils.convertDateFormat(
                    edd,
                    DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                    DATE_ddMMyyyy,
                )
                createSummary(
                    getString(R.string.estimated_delivery_date),
                    formattedEDD,
                )
            }

            // 3. Gestational Week (calculated, shown after EDD)
            detail.lastMenstrualPeriod?.let { lmp ->
                try {
                    val lmpDate = getLastMenstrualDate(lmp)
                    val gestationalAgeWeekString = formatGestationalAge(
                        calculateGestationalAge(
                            lmpDate,
                        ),
                        requireContext(),
                    )
                    createSummary(
                        getString(R.string.gestational_age),
                        gestationalAgeWeekString,
                    )
                } catch (e: Exception) {
                    // Skip if calculation fails
                }
            }

            // 4. Gravida
            detail.gravida?.let { gravida ->
                createSummary(
                    getString(R.string.gravida),
                    gravida.toString(),
                )
            }

            // 5. Parity - only show if gravida >= 2 and parity value is present
            val gravida = detail.gravida
            if (gravida != null && gravida >= AssessmentDefinedParams.GRAVIDA_THRESHOLD_FOR_PARITY) {
                detail.parity?.let { parity ->
                    val parityColor = if (parity >= AssessmentDefinedParams.PARITY_HIGH_RISK_THRESHOLD) Color.RED else null
                    createSummary(
                        getString(R.string.parity),
                        parity.toString(),
                        parityColor,
                    )
                    // 6. Age of Last Child - only show if numberOfLivingChildren >= 1 and ageOfLastChild is present
                    val numberOfLivingChildren = detail.numberOfLivingChildren
                    if (numberOfLivingChildren != null && numberOfLivingChildren >= AssessmentDefinedParams.NUMBER_OF_LIVING_CHILDREN_THRESHOLD) {
                        detail.ageOfLastChild?.let { dobString ->
                            try {
                                val yearMonthWeek = getV2YearMonthAndWeek(dobString)
                                val years = yearMonthWeek.years
                                val months = yearMonthWeek.months
                                val totalMonths = (years * 12) + months

                                val ageOfLastChildDisplay = when {
                                    totalMonths < AssessmentDefinedParams.MONTHS_FOR_YEARS_DISPLAY -> {
                                        // Show only months if less than 12 months
                                        if (totalMonths == AssessmentDefinedParams.MONTHS_YEARS_SINGULAR_THRESHOLD) {
                                            "$totalMonths ${getString(R.string.month)}"
                                        } else {
                                            "$totalMonths ${getString(R.string.months)}"
                                        }
                                    }
                                    else -> {
                                        // Show years and months if 12+ months
                                        val yearText = if (years == AssessmentDefinedParams.MONTHS_YEARS_SINGULAR_THRESHOLD) {
                                            "$years ${getString(R.string.year)}"
                                        } else {
                                            "$years ${getString(R.string.years)}"
                                        }
                                        val monthText = if (months == 0) {
                                            ""
                                        } else if (months == AssessmentDefinedParams.MONTHS_YEARS_SINGULAR_THRESHOLD) {
                                            " $months ${getString(R.string.month)}"
                                        } else {
                                            " $months ${getString(R.string.months)}"
                                        }
                                        "$yearText$monthText"
                                    }
                                }

                                // Check if age of last child is < 2 years
                                val ageOfLastChildColor = try {
                                    val ageInYears = calculateAge(dobString)
                                    if (ageInYears < AssessmentDefinedParams.AGE_OF_LAST_CHILD_HIGH_RISK_YEARS) Color.RED else null
                                } catch (e: Exception) {
                                    null
                                }

                                createSummary(
                                    getString(R.string.age_of_last_child),
                                    ageOfLastChildDisplay,
                                    ageOfLastChildColor,
                                )
                            } catch (e: Exception) {
                                // Skip if calculation fails
                            }
                        }
                    }
                }
            }


        } ?: run {
            // If no pregnancy detail, don't show any fields except Date of Visit and ANC Visit
        }

        // Calculate visit count once for ANC workflow
        val visitCount = if (viewModel.workflowName == RMNCH.ANC) {
            getVisitCount(viewModel.memberClinicalLiveData.value?.visitCount)
        } else {
            null
        }

        // Show High risk pregnant woman and Gaps in ANC only if visit count > 1 and values exist
        // Display these BEFORE Date of Visit (only for ANC workflow)
        if (viewModel.workflowName == RMNCH.ANC && visitCount != null && visitCount > 1) {
            // High risk pregnant woman
            pregnancyDetail?.highRiskPregnantWoman?.let { highRiskJson ->
                val highRiskList = parseJsonStringToList(highRiskJson)
                if (highRiskList.isNotEmpty()) {
                    val highRiskDisplay = highRiskList.joinToString(", ")
                    createSummary(
                        AssessmentDefinedParams.LABEL_HIGH_RISK_PREGNANT_WOMAN,
                        highRiskDisplay,
                    )
                }
            }

            // Gaps in ANC
            pregnancyDetail?.gapsInAnc?.let { gapsJson ->
                val gapsList = parseJsonStringToList(gapsJson)
                if (gapsList.isNotEmpty()) {
                    val gapsDisplay = gapsList.joinToString(", ")
                    createSummary(
                        AssessmentDefinedParams.LABEL_GAPS_IN_ANC,
                        gapsDisplay,
                    )
                }
            }
        }

        // Date of Visit always shows current date (shown after High risk and Gaps)
        val formattedCurrentDate = DateUtils.getTodayDateDDMMYYYY(DATE_ddMMyyyy)
        createSummary(
            getString(R.string.date_of_visit),
            formattedCurrentDate,
        )

        // ANC Visit (shown after date of visit, only for ANC workflow)
        if (viewModel.workflowName == RMNCH.ANC && visitCount != null) {
            createSummary(
                getString(R.string.anc_visit),
                visitCount.toString(),
            )
        }
    }

    /**
     * Parses a JSON string to a List<String>
     */
    private fun parseJsonStringToList(jsonString: String): List<String> {
        return try {
            val type: Type = object : TypeToken<List<String>>() {}.type
            Gson().fromJson(jsonString, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun getVisitCount(visitCount: Long?): Long =
        if (visitCount == null) {
            1L
        } else {
            (visitCount + 1)
        }

    private fun createSummary(
        title: String,
        value: String,
        valueTextColor: Int? = null,
    ) {
        binding.llPregnancyInfo.addView(
            AssessmentCommonUtils.addViewSummaryLayout(
                title = title,
                value = value,
                valueTextColor = valueTextColor,
                context = binding.llPregnancyInfo.context,
            ),
        )
    }
}
