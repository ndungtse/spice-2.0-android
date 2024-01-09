package com.medtroniclabs.spice.ui.medicalreview

import android.os.Bundle
import android.view.View
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.safeClickListener
import com.medtroniclabs.spice.databinding.ActivityMedicalReviewBaseBinding
import com.medtroniclabs.spice.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MedicalReviewBaseActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding : ActivityMedicalReviewBaseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicalReviewBaseBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.patient_medical_review)
        )
        initializeFragments()
        initializeListeners()
    }

    private fun initializeListeners() {
        binding.btnSubmit.safeClickListener(this)
    }

    private fun initializeFragments() {
        binding.patientDiagnosisContainer.visibility = View.VISIBLE
        binding.btnSubmit.isEnabled = true
        supportFragmentManager.beginTransaction()
            .add(R.id.patientDiagnosisContainer, MedicalReviewPatientDiagnosisFragment())
            .commit()
        supportFragmentManager.beginTransaction()
            .add(R.id.patientExaminationsContainer, MedicalReviewPatientExaminationFragment())
            .commit()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnSubmit -> {
                supportFragmentManager.findFragmentById(R.id.patientExaminationsContainer)
                    ?.let {
                        if (it is MedicalReviewPatientExaminationFragment) {
                            it.getSelectedExamsAndComplaints()
                        }
                    }
            }
        }
    }
}