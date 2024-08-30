package com.medtroniclabs.spice.ui.mypatients

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.databinding.ActivityNcdMrBaseBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.mypatients.fragment.MedicalReviewPatientExaminationFragment
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.AboveFiveYearsTreatmentSummaryFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.MedicalReviewPatientDiagnosisFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientInfoFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.NCDMedicalReviewViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MedicalReviewBaseActivity : BaseActivity(), View.OnClickListener {

    private val viewModel: NCDMedicalReviewViewModel by viewModels()

    private lateinit var binding: ActivityNcdMrBaseBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNcdMrBaseBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.patient_medical_review)
        )
        initializeFragments(true)
        initializeListeners()
    }

    private fun initializeListeners() {
        binding.btnSubmit.safeClickListener(this)
    }

    private fun initializeFragments(isInitView: Boolean) {
        if (!(SecuredPreference.getBoolean(SecuredPreference.EnvironmentKey.IS_NCD_MEDICAL_REVIEW_LOADED.name)))
            viewModel.getStaticMetaData()
        binding.nestedScrollViewID.smoothScrollTo(0, 0)
        val fragment = if (isInitView) {
            MedicalReviewPatientExaminationFragment()
        } else {
            AboveFiveYearsTreatmentSummaryFragment.newInstance()
        }
        if (isInitView) {
            binding.bottomNavigationView.visible()
            binding.btnLayout.btnLayout.gone()
            binding.patientExaminationsContainer.gone()
            binding.patientDiagnosisContainer.visible()
        } else {
            binding.patientDiagnosisContainer.gone()
            binding.bottomNavigationView.gone()
            binding.patientExaminationsContainer.visible()
            binding.btnLayout.btnLayout.visible()
        }

        binding.btnSubmit.isEnabled = true
        supportFragmentManager.beginTransaction()
            .add(
                R.id.patientDetailFragment,
                PatientInfoFragment.newInstance(intent.getStringExtra(DefinedParams.PatientId))
            )
            .commit()
        supportFragmentManager.beginTransaction()
            .add(R.id.patientDiagnosisContainer, MedicalReviewPatientDiagnosisFragment())
            .commit()
        supportFragmentManager.beginTransaction()
            .replace(R.id.patientExaminationsContainer, fragment)
            .commit()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnSubmit -> {
                initializeFragments(false)
            }
        }
    }

}