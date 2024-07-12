package com.medtroniclabs.spice.ui.medicalreview.underfiveyears

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.widget.NestedScrollView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.databinding.ActivityUnderFiveYearsBaseBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.model.medicalreview.CreateUnderTwoMonthsResponse
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.common.FloatingDetectorFrameLayout
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
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.AncVisitCallBack
import com.medtroniclabs.spice.ui.medicalreview.prescription.PrescriptionActivity
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.mypatients.fragment.MedicalReviewPatientDiagnosisFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientInfoFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.ReferPatientFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import com.medtroniclabs.spice.ui.mypatients.viewmodel.ReferPatientViewModel
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
    private val clinicalSummaryViewModel: UnderFiveYearsClinicalSummaryViewModel by viewModels()
    private val examinationCardViewModel: ExaminationCardViewModel by viewModels()
    private val patientDetailViewModel: PatientDetailViewModel by viewModels()
    private val referPatientViewModel: ReferPatientViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        binding = ActivityUnderFiveYearsBaseBinding.inflate(layoutInflater)
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
        initializeViews()
        attachObserver()
        initializeListeners()
        viewModel.patientId = intent.getStringExtra(DefinedParams.PatientId)
    }

    private fun backNavigationToHome() {
        showErrorDialogue(
            getString(R.string.alert),
            getString(R.string.exit_reason),
            isNegativeButtonNeed = true
        ) { isPositive ->
            if (isPositive) {
                startActivityWithoutSplashScreen()
            }
        }
    }

    private fun initializeListeners() {
        binding.btnSubmit.safeClickListener(this)
        binding.underFiveSummaryBottomView.btnDone.safeClickListener(this)
        binding.underFiveSummaryBottomView.btnRefer.safeClickListener(this)
        binding.ivPrescription.safeClickListener(this)
        binding.refreshLayout.setOnRefreshListener {
            swipeRefresh()
        }
    }

    private fun swipeRefresh() {
        showLoading()
        viewModel.isSwipeRefresh = true
        supportFragmentManager.findFragmentById(R.id.clinicalSummaryContainer)
            .let { currentFragment ->
                if (currentFragment is UnderFiveYearsTreatmentSummaryFragment) {
                    if (connectivityManager.isNetworkAvailable()) {
                        summaryViewModel.getUnderFiveYearsSummaryDetails(
                            CreateUnderTwoMonthsResponse(
                                encounterId = viewModel.encounterId ?: "",
                                patientReference = viewModel.patientReference ?: ""
                            )
                        )
                    } else {
                        showErrorDialogue(
                            getString(R.string.error), getString(R.string.no_internet_error),
                            isNegativeButtonNeed = false,
                        ) {}
                    }
                } else {
                    patientDetailViewModel.patientDetailsLiveData.value?.data?.let { details ->
                        details.patientId?.let { id ->
                            patientDetailViewModel.getPatients(id)
                        }
                    }
                }
            }
    }

    private fun diagnosisAndExaminationFragment() {
        replaceFragmentInId<MedicalReviewPatientDiagnosisFragment>(
            binding.patientDiagnosisContainer.id,
            bundle = initializeBundle(),
            tag = MedicalReviewPatientDiagnosisFragment.TAG
        )
    }

    private fun systemicExaminationFragment() {
        replaceFragmentInId<SystemicExaminationsFragment>(
            binding.systemicExaminationsContainer.id,
            bundle = initializeBundle(),
            tag = SystemicExaminationsFragment.TAG
        )

    }

    private fun attachObserver() {
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
                    initializePatientDetailFragment()
                }
            }
        }
        patientDetailViewModel.patientDetailsLiveData.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                    showErrorDialogue(
                        title = getString(R.string.alert),
                        message = getString(R.string.something_went_wrong_try_later),
                        positiveButtonName = getString(R.string.ok),
                    ) {
                        if (it) {
                            onBackPressPopStack()
                        }
                    }
                }
            }
            if (binding.refreshLayout.isRefreshing) {
                binding.refreshLayout.isRefreshing = false
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
                        viewModel.encounterId = it.encounterId.toString()
                        viewModel.patientReference = it.patientReference.toString()
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
                        supportFragmentManager, MedicalReviewSuccessDialogFragment.TAG
                    )
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }

        summaryViewModel.checkSubmitBtn.observe(this) {
            summaryValidation()
        }
    }

    private fun summaryValidation() {
        binding.underFiveSummaryBottomView.btnDone.isEnabled =
            summaryViewModel.nextVisitDate?.isNotBlank() == true || summaryViewModel.selectedPatientStatus?.isNotBlank() == true
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
            binding.clinicalSummaryContainer.id,
            bundle,
            tag = UnderFiveYearsTreatmentSummaryFragment.TAG
        )
        binding.bottomNavigationView.gone()
        binding.underFiveSummaryBottomView.root.visibility = View.VISIBLE
    }

    private fun initializeViews() {
        examinationCardViewModel.workFlowType = MedicalReviewTypeEnums.UnderFiveYears.name
        if (!(SecuredPreference.getBoolean(SecuredPreference.EnvironmentKey.IS_UNDER_FIVE_YEARS_LOADED.name))) {
            if (connectivityManager.isNetworkAvailable()) {
                viewModel.getStaticMetaData()
            } else {
                showErrorDialogue(
                    getString(R.string.error), getString(R.string.no_internet_error),
                    isNegativeButtonNeed = false,
                ) {}
            }
        } else {
            initializePatientDetailFragment()
        }
    }

    private fun initializePatientDetailFragment() {
        val patientInfoFragment =
            PatientInfoFragment.newInstance(intent.getStringExtra(DefinedParams.PatientId))
        supportFragmentManager.beginTransaction().add(
                R.id.patientDetailFragment, patientInfoFragment
            ).commit()
        patientInfoFragment.setDataCallback(this)
    }

    private fun initializeBundle(): Bundle {
        return Bundle().apply {
            putString(
                MedicalReviewTypeEnums.PresentingComplaints.name,
                MedicalReviewTypeEnums.UnderFiveYears.name
            )
            putString(
                MedicalReviewTypeEnums.SystemicExaminations.name,
                MedicalReviewTypeEnums.UnderFiveYears.name
            )
            putString(
                MedicalReviewTypeEnums.DiagnosisType.name,
                MedicalReviewTypeEnums.UnderFiveYears.name
            )
            putString(
                DefinedParams.ID, intent.getStringExtra(DefinedParams.ID)
            )
            putString(
                DefinedParams.MemberID, viewModel.memberId
            )
        }
    }

    private fun initializeFragments() {
        binding.patientDiagnosisContainer.visibility = View.VISIBLE
        diagnosisAndExaminationFragment()
        showLoading()
        systemicExaminationFragment()
        replaceFragmentInId<ClinicalSummaryUnderFiveYearsFragment>(
            binding.clinicalSummaryContainer.id, tag = ClinicalSummaryUnderFiveYearsFragment.TAG
        )
        replaceFragmentInId<ExaminationCardFragment>(
            binding.examinationsContainer.id,
            bundle = initializeBundle(),
            tag = ExaminationCardFragment.TAG
        )
        replaceFragmentInId<PresentingComplaintsFragment>(
            binding.presentingComplaintsContainer.id, tag = PresentingComplaintsFragment.TAG
        )
        replaceFragmentInId<ClinicalNotesFragment>(
            binding.clinicalNotesContainer.id, tag = ClinicalNotesFragment.TAG
        )

        supportFragmentManager.setFragmentResultListener(
                MedicalReviewDefinedParams.SUMMARY_ITEM,
                this
            ) { _, _ ->
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
                summaryDone()
            }

            binding.ivPrescription.id -> {
                handlePrescriptionClick()
            }

            R.id.btnRefer -> {
                viewModel.createUnderFiveMedicalReviewLiveData.value?.data?.let {
                    ReferPatientFragment.newInstance(
                        MedicalReviewTypeEnums.ICCM.name,
                        it.patientReference,
                        it.encounterId
                    ).show(
                        supportFragmentManager, ReferPatientFragment.TAG
                    )
                }
            }
        }
    }

    private fun handlePrescriptionClick() {
        patientDetailViewModel.patientDetailsLiveData.value?.data?.let { data ->
            Intent(this, PrescriptionActivity::class.java).apply {
                putExtra(DefinedParams.PatientId, data.patientId)
                putExtra(DefinedParams.EncounterId, patientDetailViewModel.encounterId)
                getResult.launch(this)
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
                    systemicExaminationsNotes = systemicExaminationViewModel.enteredExaminationNotes,
                    patientDetailViewModel.encounterId
                )
            }
        } else {
            showErrorDialogue(
                getString(R.string.error), getString(R.string.no_internet_error),
                isNegativeButtonNeed = false,
            ) {}
        }
    }

    private val getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.getStringExtra(DefinedParams.EncounterId)?.let { value ->
                    patientDetailViewModel.encounterId = value
                }
            }
        }

    private fun summaryDone() {
        if (connectivityManager.isNetworkAvailable()) {
            binding.underFiveSummaryBottomView?.btnDone?.isEnabled = true
            patientDetailViewModel.patientDetailsLiveData.value?.data?.let { details ->
                viewModel.createUnderFiveYearMedicalReview.value?.data?.encounterId?.let { submitEncounterId ->
                    viewModel.createUnderFiveYearMedicalReview.value?.data?.patientReference?.let {patientReferenceId ->
                        viewModel.underFiveYearsSummaryCreate(
                            details,
                            submitEncounterId,
                            summaryViewModel.nextVisitDate,
                            summaryViewModel.selectedPatientStatus,
                            patientReferenceId
                        )
                    }
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
        val clinicalSummaryUnderFiveYearsFragment =
            supportFragmentManager.findFragmentById(R.id.clinicalSummaryContainer) as? ClinicalSummaryUnderFiveYearsFragment
        val presentingComplaintsFragment =
            supportFragmentManager.findFragmentById(R.id.presentingComplaintsContainer) as? PresentingComplaintsFragment
        val clinicalNotesFragment =
            supportFragmentManager.findFragmentById(R.id.clinicalNotesContainer) as? ClinicalNotesFragment
        val isPresentingComplaintsValid = presentingComplaintsFragment?.validate()
        val isClinicalSummaryValid = clinicalSummaryUnderFiveYearsFragment?.validateEditFields()
        val isClinicalNotesValid = clinicalNotesFragment?.validateInput()
        autoScrollError(isClinicalSummaryValid,isClinicalNotesValid)
        return (isClinicalSummaryValid==true && isClinicalNotesValid==true && isPresentingComplaintsValid == true)
    }

    private fun autoScrollError(isClinicalSummaryValid: Boolean?, isClinicalNotesValid: Boolean?) {
        if (isClinicalSummaryValid!=true && isClinicalNotesValid==true){
            val nestedScrollView = findViewById<NestedScrollView>(R.id.nestedScrollViewID)
            val clinicalSummaryContainer = findViewById<FloatingDetectorFrameLayout>(R.id.clinicalSummaryContainer)
            val y = clinicalSummaryContainer.top
            nestedScrollView.smoothScrollTo(0, y)
        }
    }

    private fun removeFragment(clinicalSummaryContainer: Int) {
        supportFragmentManager.findFragmentById(clinicalSummaryContainer)?.let {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    private fun enableReferralDoneBtn() {
        binding.underFiveSummaryBottomView?.btnDone?.isEnabled = getSummaryStatus()
    }

    private fun getSummaryStatus(): Boolean {
        return ((summaryViewModel.selectedPatientStatus == DefinedParams.Recovered && summaryViewModel.nextVisitDate == null) || (summaryViewModel.selectedPatientStatus != DefinedParams.Recovered && summaryViewModel.nextVisitDate != null))
    }

    private fun backNavigation() {
        showErrorDialogue(
            getString(R.string.alert), getString(R.string.exit_reason), isNegativeButtonNeed = true
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
        startActivityWithoutSplashScreen()
    }

    override fun onDataLoaded(details: PatientListRespModel) {
        if (viewModel.isSwipeRefresh) {
            systemicExaminationFragment()
        } else {
            initializeFragments()
        }
        viewModel.memberId = details.memberId
    }

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backNavigation()
            }
        }

}