package com.medtroniclabs.spice.ncd.medicalreview.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.res.ResourcesCompat
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.databinding.FragmentNcdMedicalReviewDiagnosisCardBinding
import com.medtroniclabs.spice.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NCDMedicalReviewDiagnosisCardFragment : BaseFragment() {

    private lateinit var binding: FragmentNcdMedicalReviewDiagnosisCardBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNcdMedicalReviewDiagnosisCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        fun newInstance() =
            NCDMedicalReviewDiagnosisCardFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        // TODO REMOVE THE HARD CODE STRING
        binding.apply {
            diagnosisCard.tvDiagnosisLbl.text = getString(R.string.diagnosis)
            diagnosisCard.tvDiagnosis.text = "HTN, DM (Provisional)"
            diagnosisCard.tvDiagnosisConfirm.text = getString(R.string.confirm_diagnoses)

            weightCard.tvDiagnosisLbl.text = getString(R.string.weight)
            weightCard.tvDiagnosis.text = "75 Kg"
            weightCard.tvDiagnosisConfirm.invisible()

            estimatedDeliveryDateCard.tvDiagnosisLbl.text =
                getString(R.string.estimated_delivery_date)
            estimatedDeliveryDateCard.tvDiagnosis.text = "75 Kg"
            estimatedDeliveryDateCard.tvDiagnosisConfirm.text = "6 weeks pregnant"

            pregnancyDetailsCard.tvDiagnosisLbl.text = getString(R.string.pregnancy_details)
            pregnancyDetailsCard.tvDiagnosis.apply {
                text = getString(R.string.edit_details)
                setTextColor(getColor(requireContext(), R.color.medium_blue))
                typeface = ResourcesCompat.getFont(requireContext(), R.font.inter_regular)
            }
            pregnancyDetailsCard.tvDiagnosisConfirm.invisible()

            bloodGlucoseCard.tvDiagnosisLbl.text = getString(R.string.pregnancy_details)
            bloodGlucoseCard.tvDiagnosis.text = "120/80 mmHg"
            bloodGlucoseCard.tvDiagnosisConfirm.text = getString(R.string.view_details)

            bloodPressureCard.tvDiagnosisLbl.text = getString(R.string.pregnancy_details)
            bloodPressureCard.tvDiagnosis.text = "8 (mmol/L)"
            bloodPressureCard.tvDiagnosisConfirm.text = getString(R.string.view_details)

        }
    }
}