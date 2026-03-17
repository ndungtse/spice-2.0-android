package org.medtroniclabs.uhis.ui.assessment.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsDefinedParams
import org.medtroniclabs.uhis.common.DateUtils
import org.medtroniclabs.uhis.common.DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
import org.medtroniclabs.uhis.common.DateUtils.DATE_ddMMyyyy
import org.medtroniclabs.uhis.common.DateUtils.convertDateFormat
import org.medtroniclabs.uhis.common.DateUtils.formatGestationalAge
import org.medtroniclabs.uhis.common.StringConverter
import org.medtroniclabs.uhis.databinding.FragmentAssessmentPregnantWomenRegistrationSummaryBinding
import org.medtroniclabs.uhis.formgeneration.extension.safeClickListener
import org.medtroniclabs.uhis.mappingkey.MemberRegistration
import org.medtroniclabs.uhis.mappingkey.PregnantWomen
import org.medtroniclabs.uhis.ui.BaseFragment
import org.medtroniclabs.uhis.ui.MenuConstants
import org.medtroniclabs.uhis.ui.assessment.AssessmentCommonUtils
import org.medtroniclabs.uhis.ui.assessment.viewmodel.AssessmentViewModel

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

        // LMP
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

        if (daysDifference >= PregnantWomen.LMP_THRESHOLD_DAYS) {
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
                getString(R.string.gestational_age),
                gestationalAgeWeekString,
            )

            // Risk factors
            val riskFactors = PregnantWomen.computeRiskFactors(mapFlatData)
            if (riskFactors.isNotEmpty()) {
                with(AssessmentCommonUtils.getTextSummaryLabelLayoutBinding(context)) {
                    with(tvTitle) {
                        setText(R.string.risk_factors_identified)
                        setTextColor("#991b1b".toColorInt())
                        setTypeface(null, Typeface.BOLD)
                    }
                    displayData.add(root)
                }

                riskFactors.forEach { riskFactor ->
                    with(AssessmentCommonUtils.getTextSummaryLabelLayoutBinding(context)) {
                        with(tvTitle) {
                            text = "  • $riskFactor" // Use bigger bullet (•)
                            setTextColor("#7f1d1d".toColorInt())
                        }
                        displayData.add(root)
                    }
                }
            }

            displayData.forEach {
                binding.parentLayout.addView(it)
            }
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
