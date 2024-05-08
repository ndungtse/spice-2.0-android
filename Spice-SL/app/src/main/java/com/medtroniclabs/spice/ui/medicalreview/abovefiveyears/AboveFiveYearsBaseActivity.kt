package com.medtroniclabs.spice.ui.medicalreview.abovefiveyears

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.SpiceLocationManager
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryRequest
import com.medtroniclabs.spice.databinding.ActivityAboveFiveYearsBaseBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.dialog.MedicalReviewSuccessDialogFragment
import com.medtroniclabs.spice.ui.landing.OnDialogDismissListener
import com.medtroniclabs.spice.ui.medicalreview.ClinicalNotesFragment
import com.medtroniclabs.spice.ui.medicalreview.PresentingComplaintsFragment
import com.medtroniclabs.spice.ui.medicalreview.SystemicExaminationsFragment
import com.medtroniclabs.spice.ui.medicalreview.prescription.PrescriptionActivity
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.CLINICAL_NOTES
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.PC_ITEM
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.SE_ITEM
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.SUMMARY_ITEM
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.mypatients.fragment.MedicalReviewPatientDiagnosisFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientInfoFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.ReferPatientFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import com.medtroniclabs.spice.ui.mypatients.viewmodel.ReferPatientViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboveFiveYearsBaseActivity : BaseActivity(), View.OnClickListener, OnDialogDismissListener {

    private lateinit var binding: ActivityAboveFiveYearsBaseBinding
    private val viewModel: AboveFiveYearsViewModel by viewModels()
    private val patientViewModel: PatientDetailViewModel by viewModels()
    private val chipItemViewModel: ClinicalNotesViewModel by viewModels()
    private val presentingComplaintsViewModel: PresentingComplaintsViewModel by viewModels()
    private val systemicExaminationViewModel: SystemicExaminationViewModel by viewModels()
    private val referPatientViewModel: ReferPatientViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboveFiveYearsBaseBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.patient_medical_review),
            callback = {
                backNavigation()
            }
        )
        initializeViews()
        initializeFragments()
        initializeListeners()
        attachObserver()
        getCurrentLocation()
    }

    private fun backNavigation() {
        showErrorDialogue(
            getString(R.string.alert),
            getString(R.string.exit_reason),
            isNegativeButtonNeed = true
        ) { isPositive ->
            if (isPositive) {
                onBackPressPopStack()
            }
        }
    }

    private fun onBackPressPopStack() {
        this@AboveFiveYearsBaseActivity.finish()
    }

    private fun initializeListeners() {
        binding.btnSubmit.safeClickListener(this)
        binding.ivPrescription.safeClickListener(this)
        binding.refreshLayout.setOnRefreshListener {
            swipeRefresh()
        }
        binding.btnDone.safeClickListener(this)
        binding.btnRefer.safeClickListener(this)
    }

    private fun swipeRefresh() {
        supportFragmentManager.findFragmentById(R.id.presentingComplaintsContainer)
            .let { currentFragment ->
                if (currentFragment is AboveFiveYearsTreatmentSummaryFragment) {
                    viewModel.aboveFiveYearsCreateResponse.value?.data?.let {
                        if (connectivityManager.isNetworkAvailable()) {
                            viewModel.getAboveFiveYearsSummaryDetails(
                                AboveFiveYearsSummaryRequest(
                                    id = it.encounterId
                                )
                            )
                        } else {
                            showErrorDialogue(
                                getString(R.string.error), getString(R.string.no_internet_error),
                                isNegativeButtonNeed = false,
                            ) {}
                        }
                    }
                } else {
                    patientViewModel.patientDetailsLiveData.value?.data?.let { details ->
                        details.patientId?.let { id ->
                            patientViewModel.getPatients(id)
                        }
                    }
                }
            }
    }

    private fun initializeViews() {
        if (!(SecuredPreference.getBoolean(SecuredPreference.EnvironmentKey.IS_ABOVE_FIVE_YEARS_LOADED.name))) {
            if (connectivityManager.isNetworkAvailable()) {
                viewModel.getStaticMetaData(MedicalReviewTypeEnums.AboveFiveYears.name)
                addPatientDetails()
            } else {
                showErrorDialogue(
                    getString(R.string.error), getString(R.string.no_internet_error),
                    isNegativeButtonNeed = false,
                ) {}
            }
        } else {
            addPatientDetails()
        }
        supportFragmentManager
            .setFragmentResultListener(PC_ITEM, this) { _, _ ->
                enableSubmitBtn()
            }
        supportFragmentManager
            .setFragmentResultListener(SE_ITEM, this) { _, _ ->
                enableSubmitBtn()
            }
        supportFragmentManager
            .setFragmentResultListener(CLINICAL_NOTES, this) { _, _ ->
                enableSubmitBtn()
            }
        supportFragmentManager
            .setFragmentResultListener(SUMMARY_ITEM, this) { _, _ ->
                enableReferralDoneBtn()
            }
    }

    private fun addPatientDetails() {
        supportFragmentManager.beginTransaction()
            .add(
                R.id.patientDetailFragment,
                PatientInfoFragment.newInstance(intent.getStringExtra(DefinedParams.PatientId))
            ).commit()
    }

    private fun attachObserver() {
        referPatientViewModel.referPatientResultLiveData.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    val fragment =
                        supportFragmentManager.findFragmentByTag(ReferPatientFragment.TAG) as? ReferPatientFragment
                    fragment?.dismiss()
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
        viewModel.aboveFiveYearsMetaLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }

                ResourceState.SUCCESS -> {
                    initializeFragments()
                    hideLoading()
                }
            }
        }
        viewModel.aboveFiveYearsCreateResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }

                ResourceState.SUCCESS -> {
                    binding.nestedScrollViewID.fullScroll(ScrollView.FOCUS_UP)
                    resourceState.data?.let {
                        if (connectivityManager.isNetworkAvailable()) {
                            viewModel.getAboveFiveYearsSummaryDetails(
                                AboveFiveYearsSummaryRequest(
                                    id = it.encounterId
                                )
                            )
                        } else {
                            showErrorDialogue(
                                getString(R.string.error), getString(R.string.no_internet_error),
                                isNegativeButtonNeed = false,
                            ) {}
                        }
                    }
                    initializeSummaryFragments()
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
                }
            }
        }

        viewModel.summaryCreateResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    MedicalReviewSuccessDialogFragment.newInstance().show(
                        supportFragmentManager,
                        MedicalReviewSuccessDialogFragment.TAG
                    )
                }
            }
        }
    }

    private fun initializeSummaryFragments() {
        binding.apply {
            patientDiagnosisContainer.visibility = View.GONE
            bottomNavContainer.visibility = View.INVISIBLE
            referalBottomView.visibility = View.VISIBLE
        }
        replaceFragmentInId<AboveFiveYearsTreatmentSummaryFragment>(
            binding.presentingComplaintsContainer.id,
            tag = AboveFiveYearsTreatmentSummaryFragment::class.simpleName
        )
        val complaintsFragment =
            supportFragmentManager.findFragmentById(R.id.systemicExaminationsContainer)
        val clinicalNotesFragment =
            supportFragmentManager.findFragmentById(R.id.clinicalNotesContainer)
        complaintsFragment?.let {
            supportFragmentManager.beginTransaction().remove(complaintsFragment).commit()
        }
        clinicalNotesFragment?.let {
            supportFragmentManager.beginTransaction().remove(clinicalNotesFragment).commit()
        }
    }

    private fun initializeFragments() {
        binding.patientDiagnosisContainer.visibility = View.VISIBLE
        val bundle = Bundle().apply {
            putString(
                MedicalReviewTypeEnums.PresentingComplaints.name,
                MedicalReviewTypeEnums.AboveFiveYears.name
            )
            putString(
                MedicalReviewTypeEnums.SystemicExaminations.name,
                MedicalReviewTypeEnums.AboveFiveYears.name
            )
            putString(
                MedicalReviewTypeEnums.DiagnosisType.name,
                MedicalReviewTypeEnums.AboveFiveYears.name
            )
            putString(
                DefinedParams.ID,
                intent.getStringExtra(DefinedParams.ID)
            )
        }
        replaceFragmentInId<MedicalReviewPatientDiagnosisFragment>(
            binding.patientDiagnosisContainer.id,
            bundle = bundle,
            tag = MedicalReviewPatientDiagnosisFragment::class.simpleName
        )
        replaceFragmentInId<PresentingComplaintsFragment>(
            binding.presentingComplaintsContainer.id,
            bundle = bundle,
            tag = PresentingComplaintsFragment::class.simpleName
        )
        replaceFragmentInId<SystemicExaminationsFragment>(
            binding.systemicExaminationsContainer.id,
            bundle = bundle,
            tag = SystemicExaminationsFragment::class.simpleName
        )
        replaceFragmentInId<ClinicalNotesFragment>(
            binding.clinicalNotesContainer.id,
            bundle = bundle,
            tag = ClinicalNotesFragment::class.simpleName
        )
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.btnSubmit.id -> {
                postResultInput()
            }

            binding.ivPrescription.id -> {
                patientViewModel.patientDetailsLiveData.value?.data?.let {data ->
                    val intent  = Intent(this, PrescriptionActivity::class.java)
                    intent.putExtra(DefinedParams.PatientId,data.patientId)
                    startActivity(intent)
                }
            }

            binding.btnRefer.id ->
                patientViewModel.patientDetailsLiveData.value?.data?.let { details ->
                    ReferPatientFragment.newInstance(MedicalReviewTypeEnums.AboveFiveYears.name)
                        .show(
                            supportFragmentManager,
                            ReferPatientFragment.TAG
                        )
                }

            binding.btnDone.id -> {
                patientViewModel.patientDetailsLiveData.value?.data?.let { details ->
                    viewModel.aboveFiveYearsCreateResponse.value?.data?.encounterId
                        ?.let { submitCreateId ->
                            if (connectivityManager.isNetworkAvailable()) {
                                viewModel.aboveFiveYearsSummaryCreate(details, submitCreateId)
                            } else {
                                showErrorDialogue(
                                    getString(R.string.error),
                                    getString(R.string.no_internet_error),
                                    isNegativeButtonNeed = false,
                                ) {}
                            }
                        }
                }
            }
        }
    }

    private fun postResultInput() {
        patientViewModel.patientDetailsLiveData.value?.data?.let { details ->
            details.patientId?.let { id ->
                if (connectivityManager.isNetworkAvailable()) {
                    viewModel.createAboveFiveYearsResult(
                        details,
                        Pair(presentingComplaintsViewModel.selectedPresentingComplaints.map { it.value },
                            systemicExaminationViewModel.selectedSystemicExaminations.map { it.value }),
                        Triple(
                            presentingComplaintsViewModel.enteredComplaintNotes,
                            systemicExaminationViewModel.enteredExaminationNotes,
                            chipItemViewModel.enteredClinicalNotes
                        )
                    )
                } else {
                    showErrorDialogue(
                        getString(R.string.error), getString(R.string.no_internet_error),
                        isNegativeButtonNeed = false,
                    ) {}
                }
            }
        }
    }

    private fun enableSubmitBtn() {
        binding.btnSubmit.isEnabled = chipItemViewModel.enteredClinicalNotes.isNotBlank()
    }

    private fun enableReferralDoneBtn() {
        binding.btnDone.isEnabled = getSummaryStatus()
    }

    private fun getSummaryStatus(): Boolean {
        return viewModel.nextFollowupDate != null
    }

    override fun onDialogDismissListener(isFinish: Boolean) {
        finish()
    }

    private fun getCurrentLocation() {
        val locationManager = SpiceLocationManager(this)
        locationManager.getCurrentLocation {
            viewModel.lastLocation = it
        }
    }
}