package org.medtroniclabs.uhis.ui.assessment.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.appextensions.getLongTime
import org.medtroniclabs.uhis.common.DateUtils
import org.medtroniclabs.uhis.common.DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
import org.medtroniclabs.uhis.common.DateUtils.DATE_ddMMyyyy
import org.medtroniclabs.uhis.common.DateUtils.calculateAge
import org.medtroniclabs.uhis.common.DateUtils.calculateGestationalAge
import org.medtroniclabs.uhis.common.DateUtils.formatGestationalAge
import org.medtroniclabs.uhis.common.DateUtils.getLastMenstrualDate
import org.medtroniclabs.uhis.common.DateUtils.getV2YearMonthAndWeek
import org.medtroniclabs.uhis.common.StringConverter
import org.medtroniclabs.uhis.databinding.FragmentPregnancyDetailsRmnchBinding
import org.medtroniclabs.uhis.db.entity.PregnancyDetail
import org.medtroniclabs.uhis.ui.BaseFragment
import org.medtroniclabs.uhis.ui.assessment.AssessmentCommonUtils
import org.medtroniclabs.uhis.ui.assessment.AssessmentDefinedParams
import org.medtroniclabs.uhis.ui.assessment.rmnch.RMNCH
import org.medtroniclabs.uhis.ui.assessment.viewmodel.AssessmentViewModel

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
        attachObserver()
    }

    private fun attachObserver() {
        viewModel.pregnancyDetailLiveData.observe(viewLifecycleOwner) { details ->
            showPregnancyDetails(details)
        }
    }

    private fun showPregnancyDetails(pregnancyDetail: PregnancyDetail?) {
        binding.llPregnancyInfo.removeAllViews()

        val formattedCurrentDate = DateUtils.getTodayDateDDMMYYYY(DATE_ddMMyyyy)

        if (viewModel.workflowName == RMNCH.ANC) {
            // Calculate visit count once for ANC workflow
            val visitCount = getVisitCount(pregnancyDetail?.ancVisitNo)
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

            // Show High risk pregnant woman and Gaps in ANC only if visit count > 1 and values exist
            // Display these BEFORE Date of Visit (only for ANC workflow)
            if (visitCount > 1) {
                // High risk pregnant woman
                val highRiskList = arrayListOf<String>()
                pregnancyDetail?.highRiskPregnantWoman?.let { highRiskJson ->
                    val highRisk = StringConverter.convertStringToMap(highRiskJson)
                    highRisk?.forEach { (_, value) ->
                        (value as? List<String>)?.let {
                            highRiskList.addAll(it)
                        }
                    }
                }
                if (highRiskList.isNotEmpty()) {
                    val highRiskDisplay = highRiskList.joinToString()
                    createSummary(
                        AssessmentDefinedParams.LABEL_HIGH_RISK_PREGNANT_WOMAN,
                        highRiskDisplay,
                    )
                } else {
                    createSummary(
                        AssessmentDefinedParams.LABEL_HIGH_RISK_PREGNANT_WOMAN,
                        getString(R.string.na),
                    )
                }

                // Gaps in ANC
                val gapsList = arrayListOf<String>()
                pregnancyDetail?.gapsInAnc?.let { gapsJson ->
                    gapsList.addAll(StringConverter.convertStringToList(gapsJson))
                }
                if (gapsList.isNotEmpty()) {
                    val gapsDisplay = gapsList.joinToString()
                    createSummary(
                        AssessmentDefinedParams.LABEL_GAPS_IN_ANC,
                        gapsDisplay,
                    )
                } else {
                    createSummary(
                        AssessmentDefinedParams.LABEL_GAPS_IN_ANC,
                        getString(R.string.na),
                    )
                }
            }

            // Date of Visit always shows current date (shown after High risk and Gaps)
            createSummary(
                getString(R.string.date_of_visit),
                formattedCurrentDate,
            )

            // ANC Visit (shown after date of visit, only for ANC workflow)
            createSummary(
                getString(R.string.anc_visit),
                visitCount.toString(),
            )
        } else {
            // Calculate visit count once for PNC workflow
            val visitCount = getVisitCount(pregnancyDetail?.pncVisitNo)
            createSummary(
                getString(R.string.date_of_visit),
                formattedCurrentDate,
            )
            createSummary(
                getString(R.string.pnc_visit),
                visitCount.toString(),
            )
            if (visitCount > 1) {
                val gapsInPnc = pregnancyDetail?.gapsInPnc
                val highRiskMother = pregnancyDetail?.highRiskMother
                val highRiskList = arrayListOf<String>()
                if (!highRiskMother.isNullOrBlank()) {
                    val highRisk = StringConverter.convertStringToMap(highRiskMother)
                    highRisk?.forEach { (_, value) ->
                        (value as? List<String>)?.let {
                            highRiskList.addAll(it)
                        }
                    }
                }
                if (highRiskList.isNotEmpty()) {
                    val risksString = highRiskList.joinToString()
                    createSummary(
                        getString(R.string.high_risk_mother),
                        risksString,
                    )
                } else {
                    createSummary(
                        getString(R.string.high_risk_mother),
                        getString(R.string.na),
                    )
                }
                val gapsList = arrayListOf<String>()
                if (!gapsInPnc.isNullOrBlank()) {
                    gapsList.addAll(StringConverter.convertStringToList(gapsInPnc))
                }
                if (gapsList.isNotEmpty()) {
                    val gapsString = gapsList.joinToString()
                    createSummary(
                        getString(R.string.gaps_in_pnc),
                        gapsString,
                    )
                } else {
                    createSummary(
                        getString(R.string.gaps_in_pnc),
                        getString(R.string.na),
                    )
                }
            }
            pregnancyDetail?.dateOfDelivery?.let { clinicalDate ->
                DateUtils.parseDate(clinicalDate)?.let { date ->
                    val days = DateUtils.getDaysDifference(date.getLongTime())
                    createSummary(
                        getString(R.string.days_since_delivery),
                        days.toString(),
                    )
                }
            }
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
