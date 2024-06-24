package com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.setExpandableText
import com.medtroniclabs.spice.common.CommonUtils.createPrescription
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams.FhirId
import com.medtroniclabs.spice.common.DefinedParams.PatientId
import com.medtroniclabs.spice.data.MotherNeonateAncSummaryModel
import com.medtroniclabs.spice.databinding.FragmentMotherNeonateAncHistoryBinding
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil.calculateBp
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil.convertBeatsPerMinute
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil.convertCMS
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil.convertNullableDoubleToString
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil.convertNullableStringToString
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.viewmodel.MedicalReviewAncHistoryViewModel

class MotherNeonateAncHistoryFragment : BaseFragment() {
    private lateinit var binding: FragmentMotherNeonateAncHistoryBinding
    private val viewModel: MedicalReviewAncHistoryViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMotherNeonateAncHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObservers()
    }

    private fun attachObservers() {
        viewModel.motherNeonateAncSummary.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let {
                        autoPopulate(it)
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }
    }

    private fun autoPopulate(motherNeonateSummaryModel: MotherNeonateAncSummaryModel) {
        motherNeonateSummaryModel.let {
            with(binding) {
                tvDateOfReviewValue.text = it.dateOfReview?.let {
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
                tvAncVisitLabelValue.text = it.visitNumber ?: getString(R.string.hyphen_symbol)
                tvPrescriptionValue.text =  createPrescription(it.prescriptions) ?: getString(R.string.hyphen_symbol)
                tvInvestigationValue.text =  getString(R.string.hyphen_symbol)
                tvBpValue.text = if (it.systolic == null && it.diastolic == null) {
                    getString(R.string.hyphen_symbol)
                } else {
                    calculateBp(it.systolic, it.diastolic, requireContext())
                }
                tvAncVisitLabelValue.text =
                    convertNullableStringToString(it.visitNumber, requireContext())
                tvBmiValue.text = convertNullableDoubleToString(it.bmi, requireContext())
                tvWeightValue.text = convertNullableDoubleToString(it.weight, requireContext())
                tvFetalHeartRateValue.text =
                    convertBeatsPerMinute(it.fetalHeartRate, requireContext())
                tvFundalHeightValue.text =
                    convertCMS(it.fundalHeight, requireContext())
                tvClinicalNotesValue.setExpandableText(convertNullableStringToString(it.clinicalNotes, requireContext()), title = tvClinicalNotesLabel.text.toString(),maxLength = 70, activity = (requireActivity() as BaseActivity))
                val combinedPresentingComplaints = StringBuilder()

                it.presentingComplaints?.filterNotNull()?.takeIf { it.isNotEmpty() }?.joinToString(separator = ",")
                    ?.let {
                        combinedPresentingComplaints.append(it)
                    }
                if (!it.presentingComplaintsNotes.isNullOrEmpty()) {
                    if (combinedPresentingComplaints.isNotEmpty()) {
                        combinedPresentingComplaints.append(",")
                    }
                    combinedPresentingComplaints.append(it.presentingComplaints)
                }
                val presentingComplaintsValue =
                    if (combinedPresentingComplaints.isNotEmpty()) combinedPresentingComplaints.toString() else getString(
                        R.string.hyphen_symbol
                    )
                tvPresentingComplaintsValue.setExpandableText(
                    presentingComplaintsValue,
                    title = tvPresentingComplaintsLabel.text.toString(),
                    maxLength = 70,
                    activity = (requireActivity() as BaseActivity)
                )
                val combinedObstetricsExamination = StringBuilder()

                it.obstetricExaminations?.filterNotNull()?.takeIf { it.isEmpty() }?.joinToString(separator = ",")
                    ?.let {
                        combinedObstetricsExamination.append(it)
                    }
                if (!it.obstetricExaminationNotes.isNullOrEmpty()) {
                    if (combinedObstetricsExamination.isNotEmpty()) {
                        combinedObstetricsExamination.append(",")
                    }
                    combinedObstetricsExamination.append(it.obstetricExaminationNotes)
                }
                val obstetricsExaminationValue =
                    if (combinedObstetricsExamination.isNotEmpty()) combinedObstetricsExamination.toString() else getString(
                        R.string.hyphen_symbol
                    )
                tvObstetricsExaminationValue.setExpandableText(
                    obstetricsExaminationValue,
                    title = tvObstetricsExaminationLabel.text.toString(),
                    maxLength = 70,
                    activity = (requireActivity() as BaseActivity)
                )
            }
        }
    }

    private fun initView() {
        viewModel.getMedicalReviewAncHistory(arguments?.getString(PatientId),arguments?.getString(FhirId))
    }

    companion object {
        const val TAG = "MedicalReviewAncHistoryFragment"
        fun newInstance(): MotherNeonateAncHistoryFragment {
            return MotherNeonateAncHistoryFragment()
        }

        fun newInstance(patientId: String?,fhirId: String?): MotherNeonateAncHistoryFragment {
            val fragment = MotherNeonateAncHistoryFragment()
            fragment.arguments = Bundle().apply {
                putString(PatientId, patientId)
                putString(FhirId, fhirId)
            }
            return fragment
        }
    }
}
