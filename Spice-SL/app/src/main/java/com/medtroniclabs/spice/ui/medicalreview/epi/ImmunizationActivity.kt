package com.medtroniclabs.spice.ui.medicalreview.epi

import android.os.Bundle
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.ActivityImmunizationBinding
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.dialog.MedicalReviewSuccessDialogFragment
import com.medtroniclabs.spice.ui.landing.OnDialogDismissListener
import com.medtroniclabs.spice.ui.medicalreview.epi.fragment.ImmunisationDetailFragment
import com.medtroniclabs.spice.ui.medicalreview.epi.fragment.ImmunisationSummaryFragment
import com.medtroniclabs.spice.ui.medicalreview.epi.fragment.MissedImmunisationDialogFragment
import com.medtroniclabs.spice.ui.medicalreview.epi.viewmodel.ImmunisationViewModel
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientInfoFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ImmunizationActivity :  BaseActivity(), OnDialogDismissListener {

    private lateinit var binding: ActivityImmunizationBinding
    private val viewModel: ImmunisationViewModel by viewModels()
    private val patientViewModel: PatientDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityImmunizationBinding.inflate(layoutInflater)
        setMainContentView(binding.root,
            true,
            getString(R.string.patient_medical_review),
            homeAndBackVisibility = Pair(true, true),
            callbackHome = {
                backNavigationToHome()
            },
            callback = {
                backNavigation()
            })

        initView()
        attachObserver()
        initializePatientDetailFragment()
        initImmunisationDetailFragment()
    }

    private fun initView() {
        binding.btnDone.isEnabled = false
        binding.btnDone.setOnClickListener {
            if (binding.btnDone.text == getString(R.string.next)) {
                viewModel.computeAnyMissedSummary()
            } else {
                saveSummaryDetails()
            }
        }
    }

    private fun initializePatientDetailFragment() {
        val patientInfoFragment =
            PatientInfoFragment.newInstance(intent.getStringExtra(DefinedParams.PatientId))
        supportFragmentManager.beginTransaction().replace(
            R.id.patientDetailFragment, patientInfoFragment
        ).commit()
    }

    private fun initImmunisationDetailFragment() {
        binding.btnDone.text = getString(R.string.next)
        val immunisationDetailFragment =
            ImmunisationDetailFragment.newInstance(
                intent.getStringExtra(DefinedParams.ID),
                intent.getStringExtra(DefinedParams.PatientId),
                intent.getStringExtra(DefinedParams.MemberID),
                intent.getStringExtra(DefinedParams.DOB))
        supportFragmentManager.beginTransaction().replace(
            R.id.immunisationDetailFragment, immunisationDetailFragment
        ).commit()
    }

    private fun showImmunisationSummaryFragment(encounterId: String) {
        binding.btnDone.text = getString(R.string.done)
        val immunisationSummaryFragment =
            ImmunisationSummaryFragment.newInstance(
                intent.getStringExtra(DefinedParams.PatientId),
                intent.getStringExtra(DefinedParams.DOB),
                encounterId,
                null
            )
        supportFragmentManager.beginTransaction().replace(
            R.id.immunisationDetailFragment, immunisationSummaryFragment
        ).commit()
    }

    private fun attachObserver() {
        // Show missed vaccine or not. Post local changes
        viewModel.showMissedVaccinationDialog.observe(this) {
            if (it.first) {
                val dialog = MissedImmunisationDialogFragment()
                dialog.show(supportFragmentManager, "MissedVaccination")
            } else {
                val id = intent.getStringExtra(DefinedParams.ID)
                val patientId = intent.getStringExtra(DefinedParams.PatientId)
                val memberId = intent.getStringExtra(DefinedParams.MemberID)
                viewModel.postVaccinationChanges(id, memberId, patientId, it.second)
            }
        }

        viewModel.addedVaccinationItemLiveData.observe(this) {
            binding.btnDone.isEnabled = true
        }

        // POST Immunisation Changes List
        viewModel.saveImmunisationListLiveData.observe(this) {
            when(it.state) {
                ResourceState.LOADING -> showLoading()
                ResourceState.SUCCESS -> {
                    it.data?.let { data ->
                        viewModel.encounterId = data.encounterId
                        viewModel.patientReferenceId = data.patientReference
                        showImmunisationSummaryFragment(data.encounterId)
                    }
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }

        // POST Summary Create
        viewModel.saveImmunisationSummaryLiveData.observe(this) {
            when(it.state) {
                ResourceState.LOADING -> showLoading()
                ResourceState.SUCCESS -> {
                    hideLoading()
                    MedicalReviewSuccessDialogFragment.newInstance().show(
                        supportFragmentManager,
                        MedicalReviewSuccessDialogFragment.TAG
                    )
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }
    }

    private fun backNavigationToHome() {
        showErrorDialogue(
            getString(R.string.alert),
            getString(R.string.exit_reason),
            isNegativeButtonNeed = true
        ) { _ ->
            startActivityWithoutSplashScreen()
        }
    }

    private fun backNavigation() {
        showErrorDialogue(
            getString(R.string.alert), getString(R.string.exit_reason), isNegativeButtonNeed = true
        ) { _ ->
           finish()
        }
    }

    private fun saveSummaryDetails() {
        patientViewModel.patientDetailsLiveData.value?.data?.let { patientDetails ->
            viewModel.saveImmunisationSummaryDetails(
                viewModel.encounterId,
                viewModel.patientReferenceId,
                patientDetails.memberId,
                patientDetails.patientId,
                patientDetails.villageId
            )
        }
    }

    override fun onDialogDismissListener(isFinish: Boolean) {
        startActivityWithoutSplashScreen()
    }
}