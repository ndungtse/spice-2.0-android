package com.medtroniclabs.spice.ui.medicalreview.undertwomonths

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.databinding.ActivityUnderTwoMonthsBaseBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.dialog.MedicalReviewSuccessDialogFragment
import com.medtroniclabs.spice.ui.landing.OnDialogDismissListener
import com.medtroniclabs.spice.ui.medicalreview.ClinicalNotesFragment
import com.medtroniclabs.spice.ui.medicalreview.PresentingComplaintsFragment
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.ClinicalNotesViewModel
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.PresentingComplaintsViewModel
import com.medtroniclabs.spice.ui.medicalreview.examinations.ExaminationCardFragment
import com.medtroniclabs.spice.ui.medicalreview.examinations.ExaminationCardViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.mypatients.fragment.MedicalReviewPatientDiagnosisFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientInfoFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.UnderTwoMonthsTreatmentSummaryFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UnderTwoMonthsBaseActivity : BaseActivity(), View.OnClickListener, OnDialogDismissListener {

    private lateinit var binding: ActivityUnderTwoMonthsBaseBinding
    private val viewModel: UnderTwoMonthViewModel by viewModels()
    private val summaryViewModel: UnderTwoMonthsTreatmentSummaryViewModel by viewModels()
    private val clinicalNotesViewModel: ClinicalNotesViewModel by viewModels()
    private val presentingComplaintsViewModel: PresentingComplaintsViewModel by viewModels()
    private val clinicalSummaryViewModel: ClinicalSummaryViewModel by viewModels()
    private val examinationCardViewModel: ExaminationCardViewModel by viewModels()
    private val patientDetailViewModel: PatientDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnderTwoMonthsBaseBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.patient_medical_review),
            callback = {
                backNavigation()
            }
        )
        val swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.refreshLayout)
        summaryViewModel.isRefreshing.observe(this) { isRefreshing ->
            swipeRefreshLayout.isRefreshing = isRefreshing
        }
        initializeViews()
        attachObserver()
        initializeListeners()
        viewModel.patientId = intent.getStringExtra(DefinedParams.PatientId)
    }

    private fun attachObserver() {
        clinicalNotesViewModel.submitButtonStateLiveData.observe(this) {
            val clinicalNotes = clinicalNotesViewModel.enteredClinicalNotes
            binding.btnSubmit.isEnabled = clinicalNotes.isNotEmpty() && clinicalNotes.isNotBlank()
        }

        viewModel.createUnderTwoMonthsMedicalReview.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState.data?.let {
                        showUnderTwoMonthsReviewSummary(it.encounterId, it.patientReference)
                    }
                }
            }
        }

        viewModel.underTwoMonthsMetaLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    initializeFragments()
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

        summaryViewModel.checkSubmitBtn.observe(this) {
            summaryValidation()
        }
    }

    private fun summaryValidation() {
        binding.underTwoSummaryBottomView.btnDone.isEnabled =
            summaryViewModel.nextVisitDate?.isNotBlank() == true || summaryViewModel.selectedPatientStatus?.isNotBlank() == true
    }

    private fun showUnderTwoMonthsReviewSummary(encounterId: String?, patientReference: String?) {
        val bundle = Bundle().apply {
            putString(DefinedParams.EncounterId, encounterId)
            putString(DefinedParams.PatientReference, patientReference)
        }
        removeFragment(R.id.clinicalSummaryContainer)
        removeFragment(R.id.examinationsContainer)
        removeFragment(R.id.presentingComplaintsContainer)
        removeFragment(R.id.clinicalNotesContainer)
        removeFragment(R.id.patientDiagnosisContainer)
        removeFragment(R.id.birthHistoryContainer)
        replaceFragmentInId<UnderTwoMonthsTreatmentSummaryFragment>(
            binding.underTwoSummaryContainer.id,
            bundle,
            tag = UnderTwoMonthsTreatmentSummaryFragment.TAG
        )
        binding.bottomNavigationView.gone()
        binding.underTwoSummaryBottomView.root.visibility = View.VISIBLE
    }

    private fun initializeViews() {
        examinationCardViewModel.workFlowType = MedicalReviewTypeEnums.UnderTwoMonths.name
        if (!(SecuredPreference.getBoolean(SecuredPreference.EnvironmentKey.IS_UNDER_TWO_MONTHS_LOADED.name))) {
            viewModel.getStaticMetaData()
        } else {
            initializeFragments()
        }
    }

    private fun initializeFragments() {
        supportFragmentManager.beginTransaction()
            .add(
                R.id.patientDetailFragment,
                PatientInfoFragment.newInstance(intent.getStringExtra(DefinedParams.PatientId))
            ).commit()
        binding.patientDiagnosisContainer.visibility = View.VISIBLE
        val bundle = Bundle().apply {
            putString(
                MedicalReviewTypeEnums.PresentingComplaints.name,
                MedicalReviewDefinedParams.PC_5YEARS
            )
            putString(
                MedicalReviewTypeEnums.SystemicExaminations.name,
                MedicalReviewDefinedParams.PC_5YEARS
            )
        }
        replaceFragmentInId<MedicalReviewPatientDiagnosisFragment>(
            binding.patientDiagnosisContainer.id,
            tag = MedicalReviewPatientDiagnosisFragment::class.simpleName
        )
        replaceFragmentInId<BirthHistoryFragment>(
            binding.birthHistoryContainer.id,
            tag = BirthHistoryFragment::class.simpleName
        )
        replaceFragmentInId<ClinicalSummaryFragment>(
            binding.clinicalSummaryContainer.id,
            tag = ClinicalSummaryFragment::class.simpleName
        )
        replaceFragmentInId<ExaminationCardFragment>(
            binding.examinationsContainer.id,
            tag = ExaminationCardFragment::class.simpleName
        )
        replaceFragmentInId<PresentingComplaintsFragment>(
            binding.presentingComplaintsContainer.id,
            bundle = bundle,
            tag = PresentingComplaintsFragment::class.simpleName
        )
        replaceFragmentInId<ClinicalNotesFragment>(
            binding.clinicalNotesContainer.id,
            bundle = bundle,
            tag = ClinicalNotesFragment::class.simpleName
        )

        supportFragmentManager
            .setFragmentResultListener(MedicalReviewDefinedParams.SUMMARY_ITEM, this) { _, _ ->
                enableReferralDoneBtn()
            }
    }

    private fun initializeListeners() {
        binding.btnSubmit.safeClickListener(this)
        binding.underTwoSummaryBottomView.btnDone.isEnabled = false
        binding.underTwoSummaryBottomView.btnDone.safeClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnSubmit -> {
                if (validateInputs()) {
                    if (connectivityManager.isNetworkAvailable()) {
                        patientDetailViewModel.patientDetailsLiveData.value?.data?.let { details ->
                            details.patientId?.let { id ->
                                viewModel.createMedicalReviewForUnderTwoMonths(
                                    details,
                                    clinicalSummaryAndSigns = clinicalSummaryViewModel.clinicalSummaryAndSigns,
                                    examinationResultHashMap = examinationCardViewModel.examinationResultHashMap,
                                    clinicalNotes = clinicalNotesViewModel.enteredClinicalNotes,
                                    presentingComplaints = presentingComplaintsViewModel.enteredComplaintNotes,
                                )
                            }
                        }
                    } else {
                        showErrorSnackBar(text = getString(R.string.no_internet_error))
                    }
                }
            }

            R.id.btnDone -> {
                val fragment =
                    supportFragmentManager.findFragmentById(R.id.underTwoSummaryContainer) as? UnderTwoMonthsTreatmentSummaryFragment
                val nextVisitDateValidation = fragment?.validateInput() ?: false
                if (nextVisitDateValidation) {
                    if (connectivityManager.isNetworkAvailable()) {
                        binding.underTwoSummaryBottomView.btnDone.isEnabled = true
                        patientDetailViewModel.patientDetailsLiveData.value?.data?.let { details ->
                            viewModel.createUnderTwoMonthsMedicalReview.value?.data?.encounterId
                                ?.let { submitCreateId ->
                                    viewModel.underTwoMonthsSummaryCreate(
                                        details,
                                        submitCreateId,
                                        summaryViewModel.nextVisitDate
                                    )
                                }
                        }
                    } else {
                        showErrorSnackBar(text = getString(R.string.no_internet_error))
                    }
                } else {
                    binding.underTwoSummaryBottomView.btnDone.isEnabled = false
                }
            }
        }
    }

    private fun removeFragment(clinicalSummaryContainer: Int) {
        supportFragmentManager.findFragmentById(clinicalSummaryContainer)?.let {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    private fun validateInputs(): Boolean {
        val clinicalSummaryFragment =
            supportFragmentManager.findFragmentById(R.id.clinicalSummaryContainer) as? ClinicalSummaryFragment
        val presentingComplaintsFragment =
            supportFragmentManager.findFragmentById(R.id.presentingComplaintsContainer) as? PresentingComplaintsFragment
        val isPresentingComplaintsValid = presentingComplaintsFragment?.validate()
        val isClinicalSummaryValid = clinicalSummaryFragment?.validateEditFields()
        return (isClinicalSummaryValid == true && isPresentingComplaintsValid == true)
    }

    override fun onDialogDismissListener(isFinish: Boolean) {
        finish()
    }

    private fun enableReferralDoneBtn() {
        binding.underTwoSummaryBottomView.btnDone.isEnabled = getSummaryStatus()
    }

    private fun getSummaryStatus(): Boolean {
        return summaryViewModel.nextVisitDate != null
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
        this@UnderTwoMonthsBaseActivity.finish()
    }

}