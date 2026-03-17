package org.medtroniclabs.uhis.ui.medicalreview.epi

import android.os.Bundle
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsDefinedParams
import org.medtroniclabs.uhis.appextensions.gone
import org.medtroniclabs.uhis.appextensions.visible
import org.medtroniclabs.uhis.common.DefinedParams
import org.medtroniclabs.uhis.databinding.ActivityImmunizationBinding
import org.medtroniclabs.uhis.formgeneration.extension.safeClickListener
import org.medtroniclabs.uhis.network.resource.ResourceState
import org.medtroniclabs.uhis.ui.BaseActivity
import org.medtroniclabs.uhis.ui.dialog.MedicalReviewSuccessDialogFragment
import org.medtroniclabs.uhis.ui.landing.OnDialogDismissListener
import org.medtroniclabs.uhis.ui.medicalreview.epi.fragment.EpiCatchUpPolicyDialogFragment
import org.medtroniclabs.uhis.ui.medicalreview.epi.fragment.ImmunisationDetailFragment
import org.medtroniclabs.uhis.ui.medicalreview.epi.fragment.ImmunisationSummaryFragment
import org.medtroniclabs.uhis.ui.medicalreview.epi.fragment.MissedImmunisationDialogFragment
import org.medtroniclabs.uhis.ui.medicalreview.epi.viewmodel.ImmunisationViewModel
import org.medtroniclabs.uhis.ui.mypatients.fragment.PatientInfoFragment
import org.medtroniclabs.uhis.ui.mypatients.viewmodel.PatientDetailViewModel

@AndroidEntryPoint
class ImmunizationActivity : BaseActivity(), OnDialogDismissListener {
    private lateinit var binding: ActivityImmunizationBinding
    private val viewModel: ImmunisationViewModel by viewModels()
    private val patientViewModel: PatientDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityImmunizationBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.patient_medical_review),
            homeAndBackVisibility = Pair(true, true),
            callbackHome = {
                backNavigationToHome()
            },
            callback = {
                backNavigation()
            },
        )

        binding.refreshLayout.setOnRefreshListener {
            swipeRefresh()
        }
        initView()
        attachObserver()
        initializePatientDetailFragment()
        initImmunisationDetailFragment()
        patientViewModel.setUserJourney(AnalyticsDefinedParams.IMMUNIZATION)
    }

    private fun swipeRefresh() {
        if (connectivityManager.isNetworkAvailable()) {
            supportFragmentManager
                .findFragmentById(R.id.patientDetailFragment)
                .let {
                    patientViewModel.getPatientId()?.let { id ->
                        patientViewModel.getPatients(id)
                    }
                }
        } else {
            showErrorDialogue(
                getString(R.string.error),
                getString(R.string.no_internet_error),
                isNegativeButtonNeed = false,
            ) {
                if (binding.refreshLayout.isRefreshing) {
                    binding.refreshLayout.isRefreshing = false
                }
            }
        }
    }

    private fun initView() {
        binding.btnDone.isEnabled = false
        binding.btnDone.safeClickListener {
            if (binding.btnDone.text == getString(R.string.next)) {
                viewModel.computeAnyMissedSummary()
            } else {
                saveSummaryDetails()
            }
        }

        binding.btnViewCatchUpPolicy.safeClickListener {
            val missedCount = viewModel.getMissedVaccineCount()
            val dialog = EpiCatchUpPolicyDialogFragment(missedCount)
            dialog.show(supportFragmentManager, "EpiCatchPolicy")
        }
    }

    private fun initializePatientDetailFragment() {
        val patientInfoFragment =
            PatientInfoFragment.newInstance(intent.getStringExtra(DefinedParams.PatientId))
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.patientDetailFragment,
                patientInfoFragment,
            ).commit()
    }

    private fun initImmunisationDetailFragment() {
        binding.btnViewCatchUpPolicy.visible()
        binding.btnDone.text = getString(R.string.next)
        val immunisationDetailFragment =
            ImmunisationDetailFragment.newInstance(
                intent.getStringExtra(DefinedParams.ID),
                intent.getStringExtra(DefinedParams.PatientId),
                intent.getStringExtra(DefinedParams.MEMBER_ID),
                intent.getStringExtra(DefinedParams.DOB),
            )
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.immunisationDetailFragment,
                immunisationDetailFragment,
            ).commit()
    }

    private fun showImmunisationSummaryFragment(encounterId: String) {
        binding.btnViewCatchUpPolicy.gone()
        binding.btnDone.text = getString(R.string.done)
        val immunisationSummaryFragment =
            ImmunisationSummaryFragment.newInstance(
                intent.getStringExtra(DefinedParams.PatientId),
                intent.getStringExtra(DefinedParams.DOB),
                encounterId,
                null,
            )
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.immunisationDetailFragment,
                immunisationSummaryFragment,
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
                val memberId = intent.getStringExtra(DefinedParams.MEMBER_ID)
                val villageId = intent.getStringExtra(DefinedParams.villageId)
                val householdId = intent.getStringExtra(DefinedParams.householdId)
                viewModel.postVaccinationChanges(id, memberId, patientId, it.second, villageId = villageId, householdId = householdId)
            }
        }

        viewModel.addedVaccinationItemLiveData.observe(this) {
            binding.btnDone.isEnabled = true
        }

        // POST Immunisation Changes List
        viewModel.saveImmunisationListLiveData.observe(this) {
            when (it.state) {
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
            when (it.state) {
                ResourceState.LOADING -> showLoading()
                ResourceState.SUCCESS -> {
                    hideLoading()
                    MedicalReviewSuccessDialogFragment.newInstance().show(
                        supportFragmentManager,
                        MedicalReviewSuccessDialogFragment.TAG,
                    )
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }

        patientViewModel.patientDetailsLiveData.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    if (binding.refreshLayout.isRefreshing) {
                        binding.refreshLayout.isRefreshing = false
                    }
                }

                ResourceState.ERROR -> {
                    hideLoading()
                    showErrorDialogue(
                        title = getString(R.string.alert),
                        message = getString(R.string.something_went_wrong_try_later),
                        positiveButtonName = getString(R.string.ok),
                    ) {
                        if (it) {
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun backNavigationToHome() {
        showErrorDialogue(
            getString(R.string.alert),
            getString(R.string.exit_reason),
            isNegativeButtonNeed = true,
        ) { flag ->
            if (flag) {
                startActivityWithoutSplashScreen()
            }
        }
    }

    private fun backNavigation() {
        showErrorDialogue(
            getString(R.string.alert),
            getString(R.string.exit_reason),
            isNegativeButtonNeed = true,
        ) { _ ->
            finish()
        }
    }

    private fun saveSummaryDetails() {
        patientViewModel.patientDetailsLiveData.value?.data?.let { patientDetails ->
            patientViewModel.setUserJourney(AnalyticsDefinedParams.DONEBUTTONTRIGGERED)
            viewModel.saveImmunisationSummaryDetails(
                viewModel.encounterId,
                viewModel.patientReferenceId,
                patientDetails.memberId,
                patientDetails.patientId,
                patientDetails.villageId,
            )
        }
    }

    override fun onDialogDismissListener(isFinish: Boolean) {
        startActivityWithoutSplashScreen()
    }
}
