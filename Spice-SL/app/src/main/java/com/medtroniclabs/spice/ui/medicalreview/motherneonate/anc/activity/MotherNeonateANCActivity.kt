package com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.activity

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.isNotTabletAndPortrait
import com.medtroniclabs.spice.appextensions.setPercentWidth
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.PregnancyANC
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.SpiceLocationManager
import com.medtroniclabs.spice.data.AboveFiveYearsSummarySubmitRequest
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.databinding.ActivityMedicalReviewAncactivityBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.dialog.MedicalReviewSuccessDialogFragment
import com.medtroniclabs.spice.ui.landing.OnDialogDismissListener
import com.medtroniclabs.spice.ui.medicalreview.ClinicalNotesFragment
import com.medtroniclabs.spice.ui.medicalreview.PresentingComplaintsFragment
import com.medtroniclabs.spice.ui.medicalreview.SystemicExaminationsFragment
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.ClinicalNotesViewModel
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.PresentingComplaintsViewModel
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.SystemicExaminationViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.AncVisitCallBack
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.fragment.MotherNeonateAncSummary
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientInfoFragment
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.fragment.PregnancyDetailsFragment
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.fragment.PregnancyPastObstetricHistoryFragment
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.fragment.PregnancySummaryFragment
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.viewmodel.MotherNeonateANCViewModel
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.viewmodel.MotherNeonateSummaryViewModel
import com.medtroniclabs.spice.ui.mypatients.fragment.MedicalReviewPatientDiagnosisFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PregnancyDetailsViewModel
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PregnancyPastObstetricHistoryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MotherNeonateANCActivity : BaseActivity(), View.OnClickListener, AncVisitCallBack,
    OnDialogDismissListener {

    private lateinit var binding: ActivityMedicalReviewAncactivityBinding
    private val viewModel: MotherNeonateANCViewModel by viewModels()
    private val presentingComplaintsViewModel: PresentingComplaintsViewModel by viewModels()
    private val systemicExaminationViewModel: SystemicExaminationViewModel by viewModels()
    private val patientViewModel: PatientDetailViewModel by viewModels()
    private val clinicalNotesViewModel: ClinicalNotesViewModel by viewModels()
    private val pregnancyPastObstetricHistoryViewModel: PregnancyPastObstetricHistoryViewModel by viewModels()
    private val pregnancyDetailsViewModel: PregnancyDetailsViewModel by viewModels()
    private val motherNeonateSummaryViewModel: MotherNeonateSummaryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicalReviewAncactivityBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.patient_medical_review),
            callback = {
                backNavigation()
            }
        )
        initStaticDataCall()
        attachObservers()
        getCurrentLocation()
        binding.refreshLayout.setOnRefreshListener {
            swipeRefresh()
        }
    }

    private fun getCurrentLocation() {
        val locationManager = SpiceLocationManager(this)
        locationManager.getCurrentLocation {
            viewModel.lastLocation = it
        }
    }

    private fun initStaticDataCall() {
        if (!SecuredPreference.getBoolean(SecuredPreference.EnvironmentKey.IS_MOTHER_NEONATE_LOADEDANC.name)) {
            viewModel.getMotherNeoNateAncStaticData()
        } else {
            initView()
        }
    }

    private fun attachObservers() {
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

        viewModel.motherNeonateMetaResponse.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
//                    hideLoading()
                    initView()
                }

                ResourceState.ERROR -> {
//                    hideLoading()
                }
            }
        }


        viewModel.motherNeonateCreateResponse.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    resource.data?.let {
                        handleSummary(it.encounterId)
                    }
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }
        pregnancyPastObstetricHistoryViewModel.checkSubmitBtn.observe(this) {
            isPregnancyDetailsAndHistoryValidation()
        }
        pregnancyDetailsViewModel.checkSubmitBtn.observe(this) {
            isPregnancyDetailsAndHistoryValidation()
        }
        motherNeonateSummaryViewModel.checkSubmitBtn.observe(this) {
            isMotherNeonateSummaryValidation()
        }
    }
    private fun isMotherNeonateSummaryValidation() {
        binding.btnDone.isEnabled =
            motherNeonateSummaryViewModel.nextFollowupDate?.isNotBlank() == true || motherNeonateSummaryViewModel.patientStatus?.isNotBlank() == true
    }
    private fun isPregnancyDetailsAndHistoryValidation() {
        val model = pregnancyDetailsViewModel.pregnancyDetailsModel
        binding.btnLayout.btnNext.isEnabled =
            pregnancyPastObstetricHistoryViewModel.pregnancyHistoryNotes?.isNotBlank() == true || pregnancyPastObstetricHistoryViewModel.pregnancyHistoryChip.isNotEmpty() || pregnancyPastObstetricHistoryViewModel.resultFlowHashMap[PregnancyPastObstetricHistoryFragment.TAG] != null || model.height != null ||
                    model.weight != null ||
                    model.pulse != null ||
                    model.lastMenstrualPeriod != null ||
                    model.estimatedDeliveryDate != null ||
                    model.noOfFetus != null ||
                    model.gravida != null ||
                    model.parity != null ||
                    model.patientBloodGroup != null ||
                    model.systolic != null ||
                    model.diastolic != null
    }

    private fun swipeRefresh() {
        supportFragmentManager.findFragmentById(R.id.presentingComplaintsContainer)
            .let {
                patientViewModel.patientDetailsLiveData.value?.data?.let { details ->
                    details.patientId?.let { id ->
                        patientViewModel.getPatients(id)
                    }
                }
            }
        val patientDetails =
            supportFragmentManager.findFragmentById(R.id.pregnancyDetailsConatiner)
        when (patientDetails) {
            patientDetails as? MedicalReviewPatientDiagnosisFragment -> {
                replaceWithDiagnosisFragment()
            }
        }
    }

    private fun initView() {
        showLoading()
        initializePatientDetailFragment()
        initializePregnancyDetailsFragment()
        initializePregnancyHistoryFragment()
        hideContainers()
        setButtonClickListener()
        setButtonWidth()
    }

    private fun initializePatientDetailFragment() {
        val fragmentManager = supportFragmentManager
        val existingFragment = fragmentManager.findFragmentById(R.id.patientDetailFragment)
        viewModel.id = intent.getStringExtra(DefinedParams.PatientId)
        if (existingFragment == null) {
            val fragment =
                PatientInfoFragment.newInstance(
                    intent.getStringExtra(DefinedParams.PatientId),
                    isAnc = true
                )
            fragment.setDataCallback(this)
            fragmentManager.beginTransaction()
                .add(R.id.patientDetailFragment, fragment)
                .commit()
        } else if (existingFragment !is PatientInfoFragment) {
            val fragment =
                PatientInfoFragment.newInstance(intent.getStringExtra(DefinedParams.PatientId))
            fragment.setDataCallback(this)
            fragmentManager.beginTransaction()
                .replace(R.id.patientDetailFragment, fragment)
                .commit()
        }
    }

    private fun initializePregnancyDetailsFragment() {
        val fragmentManager = supportFragmentManager
        val existingFragment = fragmentManager.findFragmentById(R.id.pregnancyDetailsConatiner)

        if (existingFragment == null) {
            fragmentManager.beginTransaction()
                .add(R.id.pregnancyDetailsConatiner, PregnancyDetailsFragment.newInstance())
                .commit()
        } else if (existingFragment !is PregnancyDetailsFragment) {
            fragmentManager.beginTransaction()
                .replace(R.id.pregnancyDetailsConatiner, PregnancyDetailsFragment.newInstance())
                .commit()
        }
    }

    private fun initializePregnancyHistoryFragment() {
        val fragmentManager = supportFragmentManager
        val existingFragment = fragmentManager.findFragmentById(R.id.pregnancyHistoryConatiner)

        if (existingFragment == null) {
            fragmentManager.beginTransaction()
                .add(
                    R.id.pregnancyHistoryConatiner,
                    PregnancyPastObstetricHistoryFragment.newInstance()
                )
                .commit()
        } else if (existingFragment !is PregnancyPastObstetricHistoryFragment) {
            fragmentManager.beginTransaction()
                .replace(
                    R.id.pregnancyHistoryConatiner,
                    PregnancyPastObstetricHistoryFragment.newInstance()
                )
                .commit()
        }
    }


    private fun hideContainers() {
        with(binding) {
            presentingComplaintsContainer.gone()
            systemicExaminationsContainer.gone()
            clinicalNotesContainer.gone()
            bottomNavigationView.gone()
            btnLayout.btnNext.visible()
        }
    }

    private fun setButtonClickListener() {
        binding.btnLayout.btnNext.safeClickListener(this)
        binding.btnSubmit.safeClickListener(this)
        binding.btnDone.safeClickListener(this)
    }

    private fun setButtonWidth() {
        val width = if (isNotTabletAndPortrait()) 0.4f else 0.2f
        binding.btnLayout.root.setPercentWidth(binding.btnLayout.btnNext.id, width)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnLayout.btnNext.id -> {
                validatePregnantDetails()
            }

            binding.btnSubmit.id -> {
                val fragment =
                    supportFragmentManager.findFragmentById(R.id.systemicExaminationsContainer) as? SystemicExaminationsFragment
                val clFragment =
                    supportFragmentManager.findFragmentById(R.id.clinicalNotesContainer) as? ClinicalNotesFragment
                if (fragment != null && fragment.validateInput() && clFragment != null && clFragment.validateInput()) {
                    submitRequest()
                }
            }

            binding.btnDone.id -> {
                submitSummary()
            }
        }
    }

    private fun submitSummary() {
        val motherNeonateSummary =
            supportFragmentManager.findFragmentById(R.id.pregnancyDetailsConatiner) as? MotherNeonateAncSummary
        val isValidSummary = motherNeonateSummary?.validateInput() ?: false

        if (isValidSummary) {
            val submitCreateId = viewModel.getSubmitCreateId()
            val nextVisitDate = DateUtils.convertDateTimeToDate(
                motherNeonateSummaryViewModel.nextFollowupDate,
                DateUtils.DATE_ddMMyyyy,
                DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
            )
            val request = AboveFiveYearsSummarySubmitRequest(
                referralTicketType = AssessmentDefinedParams.RMNCH,
                memberId = patientViewModel.getPatientMemberId(),
                id = submitCreateId,
                provenance = ProvanceDto(
                    createdDateTime = DateUtils.getCurrentDateAndTime(
                        DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                    )
                ),
                patientReference = viewModel.getPatientReference(),
                nextVisitDate = nextVisitDate
            )
            if (connectivityManager.isNetworkAvailable()) {
                viewModel.motherNeonateSummaryCreate(request)
            } else {
                showErrorDialogue(
                    getString(R.string.error), getString(R.string.no_internet_error),
                    isNegativeButtonNeed = false,
                ) {}
            }
        }
    }


    private fun submitRequest() {
        viewModel.motherNeonateAncRequest.apply {
            assessmentType = PregnancyANC
            presentingComplaints =
                presentingComplaintsViewModel.selectedPresentingComplaints.map { it.name }
            presentingComplaintsNotes = presentingComplaintsViewModel.enteredComplaintNotes
            obstetricExaminations =
                systemicExaminationViewModel.selectedSystemicExaminations.map { it.name }
            obstetricExaminationNotes = systemicExaminationViewModel.enteredExaminationNotes
            fundalHeight = systemicExaminationViewModel.fundalHeight
            fetalHeartRate = systemicExaminationViewModel.fetalHeartRate
            clinicalNotes = clinicalNotesViewModel.enteredClinicalNotes
            pregnancyDetails = pregnancyDetailsViewModel.pregnancyDetailsModel
            deliveryKit = pregnancyPastObstetricHistoryViewModel.deliveryKit
            pregnancyHistory = pregnancyPastObstetricHistoryViewModel.pregnancyHistoryChip
                .filter { it.name != DefinedParams.Other }
                .map { it.name }
            pregnancyHistoryNotes = pregnancyPastObstetricHistoryViewModel.pregnancyHistoryNotes
        }
        if (connectivityManager.isNetworkAvailable()) {
            viewModel.createMotherNeonate()
        } else {
            showErrorDialogue(
                getString(R.string.error), getString(R.string.no_internet_error),
                isNegativeButtonNeed = false,
            ) {}
        }
    }

    private fun validatePregnantDetails() {
        val pregnancyDetailsFragment =
            supportFragmentManager.findFragmentById(R.id.pregnancyDetailsConatiner) as? PregnancyDetailsFragment
        if (pregnancyDetailsFragment?.validateInput() == true) {
            handleSubmit()
        }
    }

    private fun backNavigation() {
        val fragmentManager = supportFragmentManager
        val pregnancyDetailsFragment =
            fragmentManager.findFragmentById(R.id.pregnancyDetailsConatiner)
        if (viewModel.ancVisit == 1L && pregnancyDetailsFragment is PregnancyDetailsFragment) {
            // Show the dialog here
            showErrorDialog()
        } else if (viewModel.ancVisit == 1L && pregnancyDetailsFragment is MedicalReviewPatientDiagnosisFragment) {
            showLoading()
            initView()
            hideLoading()
        } else if (pregnancyDetailsFragment is MotherNeonateAncSummary) {
            showErrorDialog()
        }
    }


    private fun showErrorDialog() {
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
        this@MotherNeonateANCActivity.finish()
    }

    private fun initializeFragments() {
        with(binding) {
            presentingComplaintsContainer.visible()
            systemicExaminationsContainer.visible()
            clinicalNotesContainer.visible()
        }
        val bundle = Bundle().apply {
            putString(
                MedicalReviewTypeEnums.PresentingComplaints.name,
                MedicalReviewTypeEnums.ANC.name
            )
            putString(
                MedicalReviewTypeEnums.SystemicExaminations.name,
                MedicalReviewTypeEnums.ANC.name
            )
        }
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


    private fun handleSubmit() {
        replaceWithDiagnosisFragment()
        replaceWithPregnancySummaryFragment()
        scrollToTop()
        hideNextButton()
        showBottomNavigation()
        initializeFragments()
        attachObserversListenerForChip()
    }

    private fun attachObserversListenerForChip() {
        supportFragmentManager
            .setFragmentResultListener(MedicalReviewDefinedParams.PC_ITEM, this) { _, _ ->
                enableSubmitBtn()
            }
        supportFragmentManager
            .setFragmentResultListener(MedicalReviewDefinedParams.SE_ITEM, this) { _, _ ->
                enableSubmitBtn()
            }
        supportFragmentManager
            .setFragmentResultListener(MedicalReviewDefinedParams.CLINICAL_NOTES, this) { _, _ ->
                enableSubmitBtn()
            }
    }

    private fun enableSubmitBtn() {
        binding.btnSubmit.isEnabled = clinicalNotesViewModel.enteredClinicalNotes.isNotBlank() ||
                (presentingComplaintsViewModel.selectedPresentingComplaints.isNotEmpty() || systemicExaminationViewModel.selectedSystemicExaminations.isNotEmpty() || presentingComplaintsViewModel.enteredComplaintNotes.isNotBlank() || systemicExaminationViewModel.enteredExaminationNotes.isNotBlank() || systemicExaminationViewModel.fundalHeight != null || systemicExaminationViewModel.fetalHeartRate != null)
    }

    private fun replaceWithDiagnosisFragment() {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.pregnancyDetailsConatiner,
                MedicalReviewPatientDiagnosisFragment.newInstance(
                    true,
                    intent.getStringExtra(DefinedParams.PatientId),
                    viewModel.memberId
                )
            )
            .commit()
    }

    private fun replaceWithPregnancySummaryFragment() {
        val fragment = PregnancySummaryFragment.newInstance()
        fragment.setData(
            pregnancyPastObstetricHistoryViewModel.pregnancyHistoryChip,
            pregnancyPastObstetricHistoryViewModel.pregnancyHistoryNotes,
            pregnancyDetailsViewModel.pregnancyDetailsModel
        )
        supportFragmentManager.beginTransaction()
            .replace(R.id.pregnancyHistoryConatiner, fragment)
            .commit()
    }

    private fun scrollToTop() {
        binding.nestedScrollViewID.smoothScrollTo(0, 0)
    }

    private fun hideNextButton() {
        binding.btnLayout.btnNext.gone()
    }

    private fun showBottomNavigation() {
        binding.bottomNavigationView.visible()
    }

    override fun onDataLoaded(data: PatientListRespModel) {
        viewModel.ancVisit = data?.pregnancyDetails?.ancVisitAssessment?.takeIf { true } ?: 1
        viewModel.memberId = data.memberId
        if (viewModel.ancVisit == 1L) {
            val patientDetails =
                supportFragmentManager.findFragmentById(R.id.pregnancyDetailsConatiner)

            when (patientDetails) {
                patientDetails as? PregnancyDetailsFragment -> {

                }

                patientDetails as? MedicalReviewPatientDiagnosisFragment -> {

                }

                patientDetails as? MotherNeonateAncSummary -> {

                }

                else -> {
                    initView()
                }
            }
        } else {

        }
    }

    private fun handleSummary(encounterId: String?) {
        with(binding) {
            bottomNavigationView.gone()
            referalBottomView.visible()
            btnLayout.btnNext.gone()
        }
        replaceMotherNeonateSummary(encounterId)
        scrollToTop()
    }

    private fun replaceMotherNeonateSummary(encounterId: String?) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.pregnancyDetailsConatiner, MotherNeonateAncSummary.newInstance(encounterId))
            .commit()
        with(supportFragmentManager) {
            findFragmentById(R.id.pregnancyHistoryConatiner)?.let {
                beginTransaction().remove(it).commit()
            }
            findFragmentById(R.id.presentingComplaintsContainer)?.let {
                beginTransaction().remove(it).commit()
            }
            findFragmentById(R.id.systemicExaminationsContainer)?.let {
                beginTransaction().remove(it).commit()
            }
            findFragmentById(R.id.clinicalNotesContainer)?.let {
                beginTransaction().remove(it).commit()
            }
        }
    }

    override fun onDialogDismissListener(isFinish: Boolean) {
        finish()
    }

}