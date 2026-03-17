package org.medtroniclabs.uhis.ui.medicalreview.motherneonate.anc.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.compose.runtime.snapshots.Snapshot.Companion.observe
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.appextensions.gone
import org.medtroniclabs.uhis.appextensions.setExpandableText
import org.medtroniclabs.uhis.appextensions.visible
import org.medtroniclabs.uhis.common.CommonUtils.combineText
import org.medtroniclabs.uhis.common.DateUtils
import org.medtroniclabs.uhis.common.DefinedParams
import org.medtroniclabs.uhis.data.PregnancyDetailsModel
import org.medtroniclabs.uhis.data.model.ChipViewItemModel
import org.medtroniclabs.uhis.databinding.FragmentPregnancySummaryBinding
import org.medtroniclabs.uhis.network.resource.ResourceState
import org.medtroniclabs.uhis.ui.BaseActivity
import org.medtroniclabs.uhis.ui.BaseFragment
import org.medtroniclabs.uhis.ui.assessment.rmnch.RMNCH.gestationalAge
import org.medtroniclabs.uhis.ui.medicalreview.hiv.viewmodel.HivViewModel
import org.medtroniclabs.uhis.ui.medicalreview.motherneonate.anc.MotherNeonateUtil.convertNullableDoubleToString
import org.medtroniclabs.uhis.ui.medicalreview.motherneonate.anc.MotherNeonateUtil.convertNullableIntToString
import org.medtroniclabs.uhis.ui.medicalreview.motherneonate.anc.MotherNeonateUtil.convertNullableStringToString
import org.medtroniclabs.uhis.ui.mypatients.viewmodel.PatientDetailViewModel

class PregnancySummaryFragment() : BaseFragment() {
    private lateinit var binding: FragmentPregnancySummaryBinding
    private var pregnancyDetailsModel: PregnancyDetailsModel? = null
    private var pregnancyHistoryChip: ArrayList<ChipViewItemModel>? = null
    private var pregnancyHistoryNotes: String? = null
    private val patientDetailsViewModel: PatientDetailViewModel by activityViewModels()
    private val hivViewModel: HivViewModel by viewModels()

    fun setData(
        pregnancyHistoryChip: ArrayList<ChipViewItemModel>,
        pregnancyHistoryNotes: String?,
        pregnancyDetailsModel: PregnancyDetailsModel,
    ) {
        this.pregnancyHistoryChip = pregnancyHistoryChip
        this.pregnancyHistoryNotes = pregnancyHistoryNotes
        this.pregnancyDetailsModel = pregnancyDetailsModel
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPregnancySummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        if (hivViewModel.isEMTCTMR) {
            getPatientDetails()
            binding.ancVisitCountGroup.visible()
        } else {
            setDataInfo()
            binding.ancVisitCountGroup.gone()
        }
        attachObserver()
    }

    private fun attachObserver() {
        hivViewModel.getPatientSummaryDetails.observe(viewLifecycleOwner) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                }

                ResourceState.SUCCESS -> {
                    resource.data?.let {
                        pregnancyDetailsModel = it
                        setDataInfo()
                    }
                }

                ResourceState.ERROR -> {
                }
            }
        }
    }

    private fun initializeView() {
        binding.titlePregnancySummaryFragment.text = getString(R.string.title_pregnancy_summary)
        hivViewModel.isEMTCTMR = arguments?.getBoolean(DefinedParams.EMTCTMR, false) == true
        hivViewModel.patientReference = arguments?.getString(DefinedParams.PatientReference)
    }

    private fun setDataInfo() {
        with(binding) {
            pregnancyDetailsModel?.let { pregnancyDetailsModel ->
                tvBmiValue.text =
                    convertNullableDoubleToString(pregnancyDetailsModel.bmi, requireContext())
                tvGravidaValue.text =
                    convertNullableIntToString(pregnancyDetailsModel.gravida, requireContext())
                tvLastMenstrualValue.text =
                    if (pregnancyDetailsModel.lastMenstrualPeriod != null || patientDetailsViewModel.getPatientLmb() != null) {
                        val lmpDate = pregnancyDetailsModel.lastMenstrualPeriod
                            ?: patientDetailsViewModel.getPatientLmb()
                        lmpDate?.let {
                            DateUtils.convertDateFormat(
                                it,
                                DateUtils.DATE_FORMAT_yyyyMMdd,
                                DateUtils.DATE_ddMMyyyy,
                            )
                        }
                    } else {
                        convertNullableStringToString(
                            null,
                            requireContext(),
                        )
                    }
                tvParityValue.text =
                    convertNullableIntToString(pregnancyDetailsModel.parity, requireContext())
                val estimatedDate = pregnancyDetailsModel.estimatedDeliveryDate
                    ?: patientDetailsViewModel.getEstimatedDeliveryDate()
                tvEstimatedDeliveryDateValue.text =
                    estimatedDate
                        ?.let {
                            DateUtils.convertDateFormat(
                                it,
                                DateUtils.DATE_FORMAT_yyyyMMdd,
                                DateUtils.DATE_ddMMyyyy,
                            )
                        }.let {
                            convertNullableStringToString(
                                it,
                                requireContext(),
                            )
                        }
                val gestationalAge = pregnancyDetailsModel.gestationalAge
                    ?: patientDetailsViewModel.getGestationalAge()
                tvGestationalAgeValue.text =
                    gestationalAge?.let {
                        DateUtils.formatGestationalAge(
                            it,
                            requireContext(),
                        )
                    } ?: getString(R.string.hyphen_symbol)
                tvPregnancyHistoryValue.setExpandableText(
                    if (hivViewModel.isEMTCTMR) {
                        combineText(
                            pregnancyDetailsModel.pregnancyHistory?.map { it.toString() },
                            pregnancyHistoryNotes,
                            getString(R.string.hyphen_symbol),
                        )
                    } else {
                        combineText(
                            pregnancyHistoryChip?.map { it.name },
                            pregnancyHistoryNotes,
                            getString(R.string.hyphen_symbol),
                        )
                    },
                    title = tvPregnancyHistoryLabel.text.toString(),
                    maxLength = 20,
                    activity = (requireActivity() as BaseActivity),
                )
                tvBloodGroupValue.text =
                    convertNullableStringToString(
                        pregnancyDetailsModel.patientBloodGroup,
                        requireContext(),
                    )
                tvNoofFetusValue.text =
                    convertNullableIntToString(pregnancyDetailsModel.noOfFetus, requireContext())

                tvAncVisitCountValue.apply {
                    text = patientDetailsViewModel.getANCVisitCount().takeIf { it != 0 }?.toString() ?: getString(R.string.hyphen_symbol)
                }
            }
        }
    }

    private fun getPatientDetails() {
        hivViewModel.getPatientSummaryDetails(hivViewModel.patientReference)
    }

    companion object {
        const val TAG = "PregnancySummaryFragment"

        fun newInstance(): PregnancySummaryFragment = PregnancySummaryFragment()

        fun newInstanceEmtct(
            isEmtct: Boolean,
            patientReference: String?,
        ): PregnancySummaryFragment {
            val fragment = PregnancySummaryFragment()
            fragment.arguments = Bundle().apply {
                putBoolean(DefinedParams.EMTCTMR, isEmtct)
                putString(DefinedParams.PatientReference, patientReference)
            }
            return fragment
        }
    }
}
