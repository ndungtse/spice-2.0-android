package com.medtroniclabs.spice.ui.assessment.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
import com.medtroniclabs.spice.common.DateUtils.DATE_ddMMyyyy
import com.medtroniclabs.spice.common.DateUtils.convertDateFormat
import com.medtroniclabs.spice.common.DateUtils.formatGestationalAge
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.databinding.FragmentAssessmentPregnantWomenRegistrationSummaryBinding
import com.medtroniclabs.spice.databinding.TextLabelLayoutBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.extension.textSizeSsp
import com.medtroniclabs.spice.mappingkey.MemberRegistration
import com.medtroniclabs.spice.mappingkey.PregnantWomen
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment responsible for showing pregnant women registration summary details
 *
 * Ticket : https://mdtlabs.atlassian.net/browse/UHIS-118
 * UI : https://claude.ai/public/artifacts/b504e171-ab92-4553-8cb4-2ba36bb3a9ed
 */
@AndroidEntryPoint
class AssessmentPregnantWomenRegistrationSummaryFragment : BaseFragment(), View.OnClickListener {
    lateinit var binding: FragmentAssessmentPregnantWomenRegistrationSummaryBinding
    private val viewModel: AssessmentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAssessmentPregnantWomenRegistrationSummaryBinding.inflate(
            inflater,
            container,
            false,
        )
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
        attachObservers()
        viewModel.setUserJourney(AnalyticsDefinedParams.OtherSymptomsSummary)
    }

    private fun setListeners() {
        binding.btnDone.safeClickListener(this)
    }

    private fun attachObservers() {
        viewModel.assessmentStringLiveData.value?.let {
            renderSummaryData(it)
        }
    }

    /**
     * Renders summary data
     */
    private fun renderSummaryData(data: String) {
        val convertedMap = StringConverter.stringToMap(data)
        if (convertedMap.isEmpty()) {
            showErrorInSummary()
        }
        binding.emptyErrorMessage.visibility = View.GONE
        binding.parentLayout.visibility = View.VISIBLE
        binding.parentLayout.removeAllViews()

        val mapData = convertedMap[MenuConstants.PREGNANT_WOMEN_PROFILE] as Map<*, *>
        val mapFlatData = hashMapOf<String, Any?>()
        mapFlatData[MemberRegistration.dateOfBirth] = viewModel.memberDetailsLiveData.value
            ?.data
            ?.dateOfBirth ?: ""
        (mapData[PregnantWomen.ID_PREGNANCY_DETAILS_AND_HISTORY] as? Map<String, Any?>)?.let {
            mapFlatData.putAll(it)
        }
        (mapData[PregnantWomen.ID_HEALTH_RISK_SCREENING] as? Map<String, Any?>)?.let {
            mapFlatData.putAll(it)
        }

        val lastMenstrualDateString = mapFlatData[PregnantWomen.ID_LMP] as String
        val lastMenstrualDate =
            DateUtils.getLastMenstrualDate(lastMenstrualDateString)
        val lastMenstrualDateDisplayString = convertDateFormat(
            lastMenstrualDateString,
            DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
            DATE_ddMMyyyy,
        )
        bindSummaryView(
            getString(R.string.lmp_last_menstrual_period),
            lastMenstrualDateDisplayString,
        )

        val displayData = mutableListOf<View>()

        val daysDifference =
            DateUtils.getDaysDifference(lastMenstrualDate.timeInMillis) ?: 0

        if (daysDifference < PregnantWomen.LMP_THRESHOLD_DAYS) {
            with(TextLabelLayoutBinding.inflate(LayoutInflater.from(context))) {
                with(tvTitle) {
                    setText(R.string.pregnancy_too_early_access_title)
                    setTextColor("#991b1b".toColorInt())
                    setTypeface(null, Typeface.BOLD)
                    textSizeSsp = PregnantWomen.SSP_18
                }
                displayData.add(root)
            }
            with(TextLabelLayoutBinding.inflate(LayoutInflater.from(context))) {
                with(tvTitle) {
                    setText(R.string.pregnancy_too_early_access_desc_1)
                    setTextColor("#7f1d1d".toColorInt())
                }
                displayData.add(root)
            }
            with(TextLabelLayoutBinding.inflate(LayoutInflater.from(context))) {
                with(tvTitle) {
                    setText(R.string.pregnancy_too_early_access_desc_2)
                    setTextColor("#7f1d1d".toColorInt())
                    setTypeface(null, Typeface.BOLD)
                }
                displayData.add(root)
            }
        } else {
            // EDD
            val estimatedDeliveryDate =
                DateUtils.calculateEstimatedDeliveryDate(lastMenstrualDate)
            val estimatedDeliveryDateDisplay =
                DateUtils.getDateFormat().format(estimatedDeliveryDate.time)
            bindSummaryView(
                getString(R.string.edd_estimated_delivery_date),
                estimatedDeliveryDateDisplay,
            )

            // Gestational week
            val gestationalAgeWeekString = formatGestationalAge(
                DateUtils.calculateGestationalAge(
                    lastMenstrualDate,
                ),
                requireContext(),
            )
            bindSummaryView(
                getString(R.string.gestational_week),
                gestationalAgeWeekString,
            )

            // Risk factors
            val riskFactors = PregnantWomen.computeRiskFactors(mapFlatData)
            with(TextLabelLayoutBinding.inflate(LayoutInflater.from(context))) {
                with(tvTitle) {
                    setText(R.string.the_following_risk_factors_identified)
                    setTextColor("#991b1b".toColorInt())
                    setTypeface(null, Typeface.BOLD)
                }
                displayData.add(root)
            }
            if (riskFactors.isNotEmpty()) {
                riskFactors.forEach { riskFactor ->
                    with(TextLabelLayoutBinding.inflate(LayoutInflater.from(context))) {
                        with(tvTitle) {
                            text = riskFactor
                            setTextColor("#7f1d1d".toColorInt())
                        }
                        displayData.add(root)
                    }
                }
            } else {
                with(TextLabelLayoutBinding.inflate(LayoutInflater.from(context))) {
                    with(tvTitle) {
                        text = getString(R.string.none)
                    }
                    displayData.add(root)
                }
            }
        }

        displayData.forEach {
            binding.parentLayout.addView(it)
        }
    }

    private fun bindSummaryView(
        title: String?,
        value: String?,
        valueTextColor: Int? = null,
    ) {
        value?.let { result ->
            binding.parentLayout.addView(
                AssessmentCommonUtils.addViewSummaryLayout(
                    title,
                    result,
                    valueTextColor,
                    requireContext(),
                ),
            )
        }
    }

    private fun showErrorInSummary() {
        binding.emptyErrorMessage.visibility = View.VISIBLE
        binding.parentLayout.visibility = View.GONE
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.btnDone.id -> {
                withLocationCheck({
                    viewModel.fetchCurrentLocation(requireContext())
                    viewModel.updatePregnantWomanAssessmentDetails()
                    viewModel.setUserJourney(AnalyticsDefinedParams.DONEBUTTONTRIGGERED)
                })
            }
        }
    }

    companion object {
        const val TAG = "AssessmentPregnantWomenRegistrationSummaryFragment"
    }
}
