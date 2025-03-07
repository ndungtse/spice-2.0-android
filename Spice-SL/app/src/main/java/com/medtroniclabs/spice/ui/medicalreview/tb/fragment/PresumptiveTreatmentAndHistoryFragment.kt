package com.medtroniclabs.spice.ui.medicalreview.tb.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.setVisible
import com.medtroniclabs.spice.databinding.FragmentPresumptiveTreatmentAndHistoryBinding
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PresumptiveTreatmentAndHistoryFragment : BaseFragment() {

    private val patientDetailsViewModel: PatientDetailViewModel by activityViewModels()
    private lateinit var binding: FragmentPresumptiveTreatmentAndHistoryBinding

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
    }

    fun initView() {
        binding.apply {
            val isTbReview = patientDetailsViewModel.getTbMedicalReviewStatus()
            tvPresentingLabel.text =
                getString(if (isTbReview) R.string.presenting_complaints else R.string.tb_sign_symptoms)
            tvTitle.text =
                getString(if (isTbReview) R.string.presumptive_treatment_summary else R.string.patient_history)
            groupCMRTb.setVisible(isTbReview)
        }
    }
}