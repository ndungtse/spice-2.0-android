package com.medtroniclabs.spice.ui.medicalreview

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.ActivityMedicalReviewLabourDeliveryactivityBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.common.LabourOrDeliveryFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.MotherFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.MotherSummaryFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.NeonateFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.NeonateSummaryFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientInfoFragment


class LabourDeliveryBaseActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMedicalReviewLabourDeliveryactivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicalReviewLabourDeliveryactivityBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.labour_delivery_medical_review)
        )
        initializeFragment()
        initializeListener()
    }

    private fun initializeListener() {
        binding.btnSubmit.safeClickListener(this)
    }

    private fun initializeFragment() {
        binding.btnSubmit.isEnabled = true
        supportFragmentManager.beginTransaction()
            .add(
                R.id.patientDetailFragment,
                PatientInfoFragment.newInstance(intent.getStringExtra(DefinedParams.PatientId))
            )
            .commit()
        supportFragmentManager.beginTransaction()
            .add(R.id.labourDeliveryContainer, LabourOrDeliveryFragment())
            .commit()
        supportFragmentManager.beginTransaction()
            .add(R.id.motherContainer, MotherFragment())
            .commit()
        supportFragmentManager.beginTransaction()
            .add(R.id.neonateContainer, NeonateFragment())
            .commit()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnSubmit -> {
               // if (validateMother() && validateNeonate()) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.labourDeliveryContainer, MotherSummaryFragment()).commit()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.motherContainer, NeonateSummaryFragment()).commit()
                    binding.neonateContainer.isVisible = false
               // }
            }
        }
    }

    private fun validateMother(): Boolean {
        var isValid = true
        supportFragmentManager.findFragmentById(R.id.motherContainer)
            ?.let { sessionFragment ->
                if (sessionFragment is MotherFragment) {
                    isValid = sessionFragment.validateInput()
                    return isValid
                }
            }
        return isValid
    }

    private fun validateNeonate(): Boolean {
        var isValid = true
        supportFragmentManager.findFragmentById(R.id.neonateContainer)
            ?.let { sessionFragment ->
                if (sessionFragment is NeonateFragment) {
                    isValid = sessionFragment.validateInput()
                    return isValid
                }
            }
        return isValid
    }
}