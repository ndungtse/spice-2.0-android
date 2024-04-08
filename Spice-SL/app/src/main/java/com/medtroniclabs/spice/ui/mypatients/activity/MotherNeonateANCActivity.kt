package com.medtroniclabs.spice.ui.mypatients.activity

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.isNotTabletAndPortrait
import com.medtroniclabs.spice.appextensions.setPercentWidth
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.databinding.ActivityMedicalReviewAncactivityBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientInfoFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.PregnancyDetailsFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.PregnancyPastObstetricHistoryFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.MotherNeonateANCViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MotherNeonateANCActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMedicalReviewAncactivityBinding
    private val viewModel: MotherNeonateANCViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicalReviewAncactivityBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.patient_medical_review)
        )
        initStaticDataCall()
        attachObservers()
    }

    private fun initStaticDataCall() {
        if (!SecuredPreference.getBoolean(SecuredPreference.EnvironmentKey.IS_MOTHER_NEONATE_LOADEDANC.name)) {
            viewModel.getMotherNeoNateAncStaticData()
        } else {
            initView()
        }
    }

    private fun attachObservers() {
        viewModel.motherNeonateMetaResponse.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    initView()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }
    }

    private fun initView() {
        val fragmentManager = supportFragmentManager

        if (fragmentManager.findFragmentById(R.id.patientDetailFragment) == null) {
            fragmentManager.beginTransaction()
                .add(
                    R.id.patientDetailFragment,
                    PatientInfoFragment.newInstance(intent.getStringExtra(DefinedParams.PatientId))
                )
                .commit()
        }

        if (fragmentManager.findFragmentById(R.id.pregnancyDetailsConatiner) == null) {
            fragmentManager.beginTransaction()
                .add(
                    R.id.pregnancyDetailsConatiner,
                    PregnancyDetailsFragment.newInstance()
                )
                .commit()
        }

        if (fragmentManager.findFragmentById(R.id.pregnancyHistoryConatiner) == null) {
            fragmentManager.beginTransaction()
                .add(
                    R.id.pregnancyHistoryConatiner,
                    PregnancyPastObstetricHistoryFragment.newInstance()
                )
                .commit()
        }
        binding.btnLayout.btnNext.safeClickListener(this)
        if (isNotTabletAndPortrait()) {
            binding.btnLayout.root.setPercentWidth(binding.btnLayout.btnNext.id, 0.4f)
        } else {
            binding.btnLayout.root.setPercentWidth(binding.btnLayout.btnNext.id, 0.2f)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnLayout.btnNext.id -> {
                getPregnantData()
            }
        }
    }

    private fun isAnyEditTextFilled(): Boolean {
        val pregnancyDetailsFragment =
            supportFragmentManager.findFragmentById(R.id.pregnancyDetailsConatiner) as? PregnancyDetailsFragment
        val pregnancyHistoryFragment =
            supportFragmentManager.findFragmentById(R.id.pregnancyHistoryConatiner) as? PregnancyPastObstetricHistoryFragment

        val pregnancyDetailsFilled = pregnancyDetailsFragment?.isAnyEditTextFilled() ?: false
        val pregnancyHistoryFilled = pregnancyHistoryFragment?.isAnyEditTextFilled() ?: false

        return pregnancyDetailsFilled || pregnancyHistoryFilled
    }

    private fun getPregnantData() {
        val pregnancyDetailsFragment =
            supportFragmentManager.findFragmentById(R.id.pregnancyDetailsConatiner) as? PregnancyDetailsFragment
        val pregnancyHistoryFragment =
            supportFragmentManager.findFragmentById(R.id.pregnancyHistoryConatiner) as? PregnancyPastObstetricHistoryFragment
        pregnancyDetailsFragment?.getPregnancyDetailsFromEditText()
        pregnancyHistoryFragment?.getPregnancyHistoryDetails()
    }

    fun updateNextButtonState() {
        binding.btnLayout.btnNext.isEnabled = isAnyEditTextFilled()
    }

}