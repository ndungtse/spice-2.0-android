package com.medtroniclabs.spice.ncd.medicalreview

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.ActivityNcdmedicalReviewCmractivityBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.EncounterReference
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.home.AssessmentToolsActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NCDMedicalReviewCMRActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivityNcdmedicalReviewCmractivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        binding = ActivityNcdmedicalReviewCmractivityBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.patient_medical_review),
            homeAndBackVisibility = Pair(true, true),
        )
        initView()
    }

    private fun initView() {
        binding.btnLayout.clBtn.gone()
        binding.btnMedicalReview.safeClickListener(this)
    }

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressPopStack()
            }
        }

    private fun onBackPressPopStack() {
        this@NCDMedicalReviewCMRActivity.finish()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnMedicalReview.id -> {
                val intent = Intent(this, AssessmentToolsActivity::class.java)
                intent.putExtra(DefinedParams.FhirId, getFhirId())
                intent.putExtra(DefinedParams.PatientId, getPatientId())
                intent.putExtra(DefinedParams.ORIGIN, getOrigin())
                intent.putExtra(DefinedParams.Gender, getGender())
                intent.putExtra(EncounterReference, getEncounterReference())
                startActivity(intent)
            }
        }
    }

    private fun getFhirId(): String? {
        return intent.getStringExtra(DefinedParams.FhirId)
    }

    private fun getPatientId(): String? {
        return intent.getStringExtra(DefinedParams.PatientId)
    }

    private fun getOrigin(): String? {
        return intent.getStringExtra(DefinedParams.ORIGIN)
    }

    private fun getGender(): String? {
        return intent.getStringExtra(DefinedParams.Gender)
    }

    private fun getEncounterReference(): String? {
        return intent.getStringExtra(EncounterReference)
    }
}