package com.medtroniclabs.spice.ui.medicalreview.underfiveyears

import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import androidx.activity.viewModels
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.databinding.ActivityUnderFiveYearsBaseBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.dialog.MedicalReviewSuccessDialogFragment
import com.medtroniclabs.spice.ui.landing.OnDialogDismissListener
import com.medtroniclabs.spice.ui.medicalreview.ClinicalNotesFragment
import com.medtroniclabs.spice.ui.medicalreview.PresentingComplaintsFragment
import com.medtroniclabs.spice.ui.medicalreview.SystemicExaminationsFragment
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.ClinicalNotesViewModel
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.PresentingComplaintsViewModel
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.SystemicExaminationViewModel
import com.medtroniclabs.spice.ui.medicalreview.examinations.ExaminationCardFragment
import com.medtroniclabs.spice.ui.medicalreview.examinations.ExaminationCardViewModel
import com.medtroniclabs.spice.ui.medicalreview.undertwomonths.fragment.ClinicalSummaryFragment
import com.medtroniclabs.spice.ui.medicalreview.undertwomonths.viewmodel.ClinicalSummaryViewModel
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.AncVisitCallBack
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.mypatients.fragment.MedicalReviewPatientDiagnosisFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientInfoFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UnderFiveYearsBaseActivity : BaseActivity(), View.OnClickListener, OnDialogDismissListener,
    AncVisitCallBack {

    private lateinit var binding: ActivityUnderFiveYearsBaseBinding
    private val viewModel: UnderFiveYearsViewModel by viewModels()
    private val summaryViewModel: UnderFiveYearTreatmentSummaryViewModel by viewModels()
    private val systemicExaminationViewModel: SystemicExaminationViewModel by viewModels()
    private val clinicalNotesViewModel: ClinicalNotesViewModel by viewModels()
    private val presentingComplaintsViewModel: PresentingComplaintsViewModel by viewModels()
    private val clinicalSummaryViewModel: ClinicalSummaryViewModel by viewModels()
    private val examinationCardViewModel: ExaminationCardViewModel by viewModels()
    private val patientDetailViewModel: PatientDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnderFiveYearsBaseBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root, true, getString(R.string.patient_medical_review),
            callback = {
                backNavigation()
            }
        )
        initializeViews()
        attachObserver()
        initializeListeners()
        viewModel.patientId = intent.getStringExtra(DefinedParams.PatientId)
    }

    private fun initializeListeners() {
        binding.btnSubmit.safeClickListener(this)
        binding.underTwoSummaryBottomView.btnDone.isEnabled = false
        binding.underTwoSummaryBottomView.btnDone.safeClickListener(this)
    }

    private fun attachObserver() {

        clinicalNotesViewModel.submitButtonStateLiveData.observe(this) {
            val clinicalNotes = clinicalNotesViewModel.enteredClinicalNotes
            binding.btnSubmit.isEnabled = clinicalNotes.isNotEmpty() && clinicalNotes.isNotBlank()
        }

        viewModel.underFiveYearsMetaLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                    showErrorDialogue(
                        title = getString(R.string.alert),
                        message = getString(R.string.something_went_wrong_try_later),
                        positiveButtonName = getString(R.string.ok)
                    ) {
                        if (it) {
                            onBackPressPopStack()
                        }
                    }
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    initializePatientFragment()
                }
            }
        }

        viewModel.createUnderFiveYearMedicalReview.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                    showErrorDialogue(
                        title = getString(R.string.alert),
                        message = getString(R.string.something_went_wrong_try_later),
                        positiveButtonName = getString(R.string.ok)
                    ) {
                        if (it) {
                            onBackPressPopStack()
                        }
                    }
                }

                ResourceState.SUCCESS -> {
                    binding.nestedScrollViewID.fullScroll(ScrollView.FOCUS_UP)
                    hideLoading()
                    resourceState.data?.let {
                        showReviewSummary(it.encounterId, it.patientReference)
                    }
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
                    showErrorDialogue(
                        title = getString(R.string.alert),
                        message = getString(R.string.something_went_wrong_try_later),
                        positiveButtonName = getString(R.string.ok)
                    ) {
                        if (it) {
                            onBackPressPopStack()
                        }
                    }
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    MedicalReviewSuccessDialogFragment.newInstance().show(
                        supportFragmentManager, MedicalReviewSuccessDialogFragment.TAG
                    )
                }
            }
        }

    }

    private fun showReviewSummary(encounterId: String?, patientReference: String?) {
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
        removeFragment(R.id.systemicExaminationsContainer)
        replaceFragmentInId<UnderFiveYearsTreatmentSummaryFragment>(
            binding.underTwoSummaryContainer.id,
            bundle,
            tag = UnderFiveYearsTreatmentSummaryFragment.TAG
        )
        binding.bottomNavigationView.gone()
        binding.underTwoSummaryBottomView.root.visibility = View.VISIBLE
    }

    private fun initializeViews() {
        examinationCardViewModel.workFlowType = MedicalReviewTypeEnums.UnderFiveYears.name
        if (!(SecuredPreference.getBoolean(SecuredPreference.EnvironmentKey.IS_UNDER_FIVE_YEARS_LOADED.name))) {
            viewModel.getStaticMetaData()
        } else {
            initializePatientFragment()
        }
    }

    private fun initializePatientFragment() {
        val fragmentManager = supportFragmentManager
        val fragment =
            PatientInfoFragment.newInstance(
                intent.getStringExtra(DefinedParams.PatientId)
            )
        fragment.setDataCallback(this)
        fragmentManager.beginTransaction()
            .add(R.id.patientDetailFragment, fragment)
            .commit()

    }


    private fun initializeFragments() {

        val bundle = Bundle().apply {
            putString(
                MedicalReviewTypeEnums.PresentingComplaints.name,
                MedicalReviewTypeEnums.UnderFiveYears.name
            )
            putString(
                MedicalReviewTypeEnums.SystemicExaminations.name,
                MedicalReviewTypeEnums.UnderFiveYears.name
            )
        }
        replaceFragmentInId<MedicalReviewPatientDiagnosisFragment>(
            binding.patientDiagnosisContainer.id,
            tag = MedicalReviewPatientDiagnosisFragment::class.simpleName
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
            tag = PresentingComplaintsFragment::class.simpleName
        )
        replaceFragmentInId<SystemicExaminationsFragment>(
            binding.systemicExaminationsContainer.id,
            bundle = bundle,
            tag = SystemicExaminationsFragment::class.simpleName
        )
        replaceFragmentInId<ClinicalNotesFragment>(
            binding.clinicalNotesContainer.id, tag = ClinicalNotesFragment::class.simpleName
        )

        supportFragmentManager
            .setFragmentResultListener(MedicalReviewDefinedParams.SUMMARY_ITEM, this) { _, _ ->
                enableReferralDoneBtn()
            }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnSubmit -> {
                if (validateInputs()) {
                    summaryCreate()
                }
            }

            R.id.btnDone -> {
                val fragment =
                    supportFragmentManager.findFragmentById(R.id.underTwoSummaryContainer) as? UnderFiveYearsTreatmentSummaryFragment
                val nextVisitDateValidation = fragment?.validateInput() ?: false
                if (nextVisitDateValidation) {
                    summaryDone()
                } else {
                    binding.underTwoSummaryBottomView.btnDone.isEnabled = false
                }
            }
        }
    }

    private fun summaryCreate() {
        if (connectivityManager.isNetworkAvailable()) {
            patientDetailViewModel.patientDetailsLiveData.value?.data?.let { patientDetail ->
                    viewModel.createMedicalReviewForUnderFiveYears(
                        patientDetail,
                        clinicalSummaryAndSigns = clinicalSummaryViewModel.clinicalSummaryAndSigns,
                        examinationResultHashMap = examinationCardViewModel.examinationResultHashMap,
                        clinicalNotes = clinicalNotesViewModel.enteredClinicalNotes,
                        presentingComplaints = presentingComplaintsViewModel.enteredComplaintNotes,
                        systemicExaminations = systemicExaminationViewModel.selectedSystemicExaminations.map { it.value },
                        systemicExaminationsNotes = systemicExaminationViewModel.enteredExaminationNotes
                    )
            }
        } else {
            showErrorDialogue(
                getString(R.string.error), getString(R.string.no_internet_error),
                isNegativeButtonNeed = false,
            ) {}
        }
    }


    private fun summaryDone() {
        if (connectivityManager.isNetworkAvailable()) {
            binding.underTwoSummaryBottomView.btnDone.isEnabled = true
            patientDetailViewModel.patientDetailsLiveData.value?.data?.let { details ->
                viewModel.createUnderFiveYearMedicalReview.value?.data?.encounterId?.let { submitCreateId ->
                    viewModel.underFiveYearsSummaryCreate(
                        details, submitCreateId, summaryViewModel.nextVisitDate
                    )
                }
            }
        } else {
            showErrorDialogue(
                getString(R.string.error), getString(R.string.no_internet_error),
                isNegativeButtonNeed = false,
            ) {}
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

    private fun removeFragment(clinicalSummaryContainer: Int) {
        supportFragmentManager.findFragmentById(clinicalSummaryContainer)?.let {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }
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
        this@UnderFiveYearsBaseActivity.finish()
    }

    override fun onDialogDismissListener(isFinish: Boolean) {
        finish()
    }

    override fun onDataLoaded(details: PatientListRespModel) {
        initializeFragments()
    }

}