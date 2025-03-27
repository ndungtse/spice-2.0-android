package com.medtroniclabs.spice.ui.medicalreview.tb.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.setVisible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.data.model.MotherNeonateAncRequest
import com.medtroniclabs.spice.data.model.TbHistory
import com.medtroniclabs.spice.databinding.FragmentPresumptiveTreatmentAndHistoryBinding
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.tb.viewmodel.TbPatientHistoryAndPresumptiveViewModel
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PresumptiveTreatmentAndHistoryFragment : BaseFragment() {

    private val patientDetailsViewModel: PatientDetailViewModel by activityViewModels()
    private lateinit var binding: FragmentPresumptiveTreatmentAndHistoryBinding
    private val viewModel: TbPatientHistoryAndPresumptiveViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPresumptiveTreatmentAndHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "PresumptiveTreatmentAndHistoryFragment"
        fun newInstance() =
            PresumptiveTreatmentAndHistoryFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObserver()
    }

    private fun attachObserver() {
        viewModel.getHistory.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                }

                ResourceState.SUCCESS -> {
                    resourceState.data?.let { history ->
                        if (patientDetailsViewModel.getTbMedicalReviewStatus()) {
                            updateUiWithMedicalReview(history)
                        } else {
                            updateUiWithTbSummary(history)
                        }
                    }
                }

                ResourceState.ERROR -> {
                }
            }
        }
    }

    private fun updateUiWithMedicalReview(history: TbHistory) {
        with(binding) {
            tvPresentingText.text = CommonUtils.combineText(
                history.presentingComplaints.orEmpty(),
                history.presentingComplaintsNotes.orEmpty(),
                getString(R.string.hyphen_symbol)
            )
            tvComorbiditiesText.text = CommonUtils.combineText(
                history.comorbidities.orEmpty(),
                "",
                getString(R.string.hyphen_symbol)
            )
            tvSystemicText.text = CommonUtils.combineText(
                history.systemicExaminations.orEmpty(),
                history.systemicExaminationNotes.orEmpty(),
                getString(R.string.hyphen_symbol)
            )
            tvClinicalNotesText.text = history.clinicalNotes?.takeIf { it.isNotBlank() }
                ?: getString(R.string.hyphen_symbol)
            tvInvestigationsText.text = CommonUtils.combineText(
                history.investigations.orEmpty(),
                "",
                getString(R.string.hyphen_symbol)
            )
            tvPrescriptionsText.text = CommonUtils.combineText(
                history.prescriptions.orEmpty(),
                "",
                getString(R.string.hyphen_symbol)
            )
        }
    }

    private fun updateUiWithTbSummary(history: TbHistory) {
        val presentingText = buildString {
            history.tbSummary?.let { summary ->
                if (summary.hasCough == true) {
                    if (summary.hasCoughLastedLonger == true) append(getString(R.string.cough_2weeks))
                    if (summary.hasNightSweats == true) appendIfNotEmpty(
                        ", ",
                        getString(R.string.drenching_night_sweats)
                    )
                    if (summary.hasFever == true) appendIfNotEmpty(", ", getString(R.string.fever))
                    if (summary.hasWeightLoss == true) appendIfNotEmpty(", ", getString(R.string.weight_loss))
                }
            }
        }.ifEmpty { getString(R.string.hyphen_symbol) }

        binding.tvPresentingText.text = presentingText
    }

    private fun StringBuilder.appendIfNotEmpty(prefix: String, text: String) {
        if (this.isNotEmpty()) append(prefix)
        append(text)
    }

    fun initView() {
        binding.apply {
            val isTbReview = patientDetailsViewModel.getTbMedicalReviewStatus()
            tvPresentingLabel.text =
                getString(if (isTbReview) R.string.presenting_complaints else R.string.tb_sign_symptoms)
            tvTitle.text =
                getString(if (isTbReview) R.string.presumptive_treatment_summary else R.string.patient_history)
            groupCMRTb.setVisible(isTbReview)
            if (isTbReview) {
                viewModel.fetchBmiList(
                    MotherNeonateAncRequest(
                        patientReference = patientDetailsViewModel.getPatientFHIRId(),
                        tbIMRCompleted = true
                    )
                )
            } else {
                viewModel.fetchBmiList(
                    MotherNeonateAncRequest(
                        patientReference = patientDetailsViewModel.getPatientFHIRId(),
                        tbIMRCompleted = false
                    )
                )
            }
        }
    }
}