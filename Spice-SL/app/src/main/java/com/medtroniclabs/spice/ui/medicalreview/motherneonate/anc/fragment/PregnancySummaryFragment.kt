package com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.setExpandableText
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.data.PregnancyDetailsModel
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.databinding.FragmentPregnancySummaryBinding
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil.convertNullableDoubleToString
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil.convertNullableIntToString
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil.convertNullableStringToString

class PregnancySummaryFragment() : BaseFragment() {

    private lateinit var binding: FragmentPregnancySummaryBinding
    private var pregnancyDetailsModel: PregnancyDetailsModel? = null
    private var pregnancyHistoryChip: ArrayList<ChipViewItemModel>? = null
    private var pregnancyHistoryNotes: String? = null

    fun setData(
        pregnancyHistoryChip: ArrayList<ChipViewItemModel>,
        pregnancyHistoryNotes: String?,
        pregnancyDetailsModel: PregnancyDetailsModel
    ) {
        this.pregnancyHistoryChip = pregnancyHistoryChip
        this.pregnancyHistoryNotes = pregnancyHistoryNotes
        this.pregnancyDetailsModel = pregnancyDetailsModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPregnancySummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        setDataInfo()
    }

    private fun initializeView() {
        binding.titlePregnancySummaryFragment.text = getString(R.string.title_pregnancy_summary)
    }

    private fun setDataInfo() {
        with(binding) {
            pregnancyDetailsModel?.let { pregnancyDetailsModel ->
                tvBmiValue.text =
                    convertNullableDoubleToString(pregnancyDetailsModel.bmi, requireContext())
                tvGravidaValue.text =
                    convertNullableIntToString(pregnancyDetailsModel.gravida, requireContext())
                tvLastMenstrualValue.text =  pregnancyDetailsModel.lastMenstrualPeriod?.let {
                    DateUtils.convertDateFormat(
                        it,
                        DateUtils.DATE_FORMAT_yyyyMMdd,
                        DateUtils.DATE_ddMMyyyy,
                    )
                }.let {
                    convertNullableStringToString(
                        it,
                        requireContext()
                    )
                }
                tvParityValue.text =
                    convertNullableIntToString(pregnancyDetailsModel.parity, requireContext())
                tvEstimatedDeliveryDateValue.text =
                    pregnancyDetailsModel.estimatedDeliveryDate?.let {
                        DateUtils.convertDateFormat(
                            it,
                            DateUtils.DATE_FORMAT_yyyyMMdd,
                            DateUtils.DATE_ddMMyyyy,
                        )
                    }.let {
                        convertNullableStringToString(
                            it,
                            requireContext()
                        )
                    }
                tvGestationalAgeValue.text =
                    pregnancyDetailsModel.gestationalAge?.let {
                        DateUtils.formatGestationalAge(
                            it,
                            requireContext()
                        )
                    } ?: getString(R.string.hyphen_symbol)
                val pregnancyHistory = pregnancyHistoryChip?.map { it.name }
                val pregnancyHistoryNotes = pregnancyHistoryNotes

                val combinedHistory = StringBuilder()

                pregnancyHistory?.takeIf { it.isNotEmpty() }?.joinToString(separator = ",")?.let {
                    combinedHistory.append(it)
                }
                if (!pregnancyHistoryNotes.isNullOrEmpty()) {
                    if (combinedHistory.isNotEmpty()) {
                        combinedHistory.append(",")
                    }
                    combinedHistory.append(pregnancyHistoryNotes)
                }
                val pregnancyHistoryValue =
                    if (combinedHistory.isNotEmpty()) combinedHistory.toString() else "-"
                tvPregnancyHistoryValue.setExpandableText(
                    pregnancyHistoryValue,
                    title = tvPregnancyHistoryLabel.text.toString(),
                    maxLength = 60
                )
                tvBloodGroupValue.text =
                    convertNullableStringToString(
                        pregnancyDetailsModel.patientBloodGroup,
                        requireContext()
                    )
                tvNoofFetusValue.text =
                    convertNullableIntToString(pregnancyDetailsModel.noOfFetus, requireContext())
            }
        }
    }

    companion object {
        const val TAG = "PregnancySummaryFragment"

        fun newInstance(): PregnancySummaryFragment {
            return PregnancySummaryFragment()
        }
    }

}