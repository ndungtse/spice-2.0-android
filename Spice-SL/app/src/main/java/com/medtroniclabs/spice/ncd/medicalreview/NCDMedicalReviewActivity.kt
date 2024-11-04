package com.medtroniclabs.spice.ncd.medicalreview

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.isVisible
import com.medtroniclabs.spice.appextensions.setVisible
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.MemberID
import com.medtroniclabs.spice.common.DefinedParams.ORIGIN
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.databinding.ActivityNcdMrBaseBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.ncd.data.Answer
import com.medtroniclabs.spice.ncd.data.Chip
import com.medtroniclabs.spice.ncd.data.ContinuousMedicalReview
import com.medtroniclabs.spice.ncd.data.CurrentMedications
import com.medtroniclabs.spice.ncd.data.InitialMedicalReview
import com.medtroniclabs.spice.ncd.data.MedicalReviewRequestResponse
import com.medtroniclabs.spice.ncd.data.NCDMRSummaryRequestResponse
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.EncounterReference
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.MATERNAL_HEALTH
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.MENTAL_HEALTH
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.MENU_ID
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.NCD
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.getConfirmDiagnoses
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.getTypeForDiagnoses
import com.medtroniclabs.spice.ncd.medicalreview.dialog.NCDDiagnosisDialogFragment
import com.medtroniclabs.spice.ncd.medicalreview.dialog.NCDMRAlertDialog
import com.medtroniclabs.spice.ncd.medicalreview.dialog.NCDPatientHistoryDialog
import com.medtroniclabs.spice.ncd.medicalreview.dialog.NCDPregnancyDialog
import com.medtroniclabs.spice.ncd.medicalreview.dialog.NCDTreatmentPlanDialog
import com.medtroniclabs.spice.ncd.medicalreview.fragment.NCDChiefComplaintsFragment
import com.medtroniclabs.spice.ncd.medicalreview.fragment.NCDClinicalNotesFragment
import com.medtroniclabs.spice.ncd.medicalreview.fragment.NCDComorbiditiesFragment
import com.medtroniclabs.spice.ncd.medicalreview.fragment.NCDComplicationsFragment
import com.medtroniclabs.spice.ncd.medicalreview.fragment.NCDCurrentMedicationFragment
import com.medtroniclabs.spice.ncd.medicalreview.fragment.NCDLifestyleAssessmentFragment
import com.medtroniclabs.spice.ncd.medicalreview.fragment.NCDMedicalReviewDiagnosisCardFragment
import com.medtroniclabs.spice.ncd.medicalreview.fragment.NCDMedicalReviewSummaryFragment
import com.medtroniclabs.spice.ncd.medicalreview.fragment.NCDObstetricExaminationFragment
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDChiefComplaintsViewModel
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDClinicalNotesViewModel
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDComorbiditiesViewModel
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDComplicationsViewModel
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDCurrentMedicationViewModel
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDLifestyleAssessmentViewModel
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDMedicalReviewDiagnosisCardViewModel
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDMedicalReviewSummaryViewModel
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDObstetricExaminationViewModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.dialog.MedicalReviewSuccessDialogFragment
import com.medtroniclabs.spice.ui.landing.OnDialogDismissListener
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.AncVisitCallBack
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientInfoFragment
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDMedicalReviewViewModel
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.medicalreview.investigation.InvestigationActivity
import com.medtroniclabs.spice.ncd.counseling.activity.NCDCounselingActivity
import com.medtroniclabs.spice.ncd.counseling.activity.NCDLifestyleActivity
import com.medtroniclabs.spice.ncd.medicalreview.prescription.activity.NCDPrescriptionActivity
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import com.medtroniclabs.spice.ui.registration.RegistrationActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NCDMedicalReviewActivity : BaseActivity(), View.OnClickListener, AncVisitCallBack ,
    NCDDialogDismissListener, NCDMRAlertDialog.DialogCallback, OnDialogDismissListener {

    private val viewModel: NCDMedicalReviewViewModel by viewModels()
    private val patientDetailViewModel: PatientDetailViewModel by viewModels()
    private val comorbiditiesViewModel: NCDComorbiditiesViewModel by viewModels()
    private val currentMedicationViewModel: NCDCurrentMedicationViewModel by viewModels()
    private val complicationsViewModel: NCDComplicationsViewModel by viewModels()
    private val lifestyleAssessmentViewModel: NCDLifestyleAssessmentViewModel by viewModels()
    private val obstetricExaminationViewModel: NCDObstetricExaminationViewModel by viewModels()
    private val clinicalNotesViewModel: NCDClinicalNotesViewModel by viewModels()
    private val chiefComplaintsViewModel: NCDChiefComplaintsViewModel by viewModels()
    private val summaryViewModel: NCDMedicalReviewSummaryViewModel by viewModels()
    private val medicalReviewDiagnosisCardViewModel :NCDMedicalReviewDiagnosisCardViewModel by viewModels()
    private lateinit var binding: ActivityNcdMrBaseBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        binding = ActivityNcdMrBaseBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.patient_medical_review),
            homeAndBackVisibility = Pair(true, true),
            callback = {
                backNavigation()
            },
            callbackHome = {
                showErrorDialogHome()
            }
        )
        initializeStaticDataSave()
        setListeners()
        attachObservers()
        binding.refreshLayout.setOnRefreshListener {
            swipeRefresh()
        }

        getPatientDetails()
    }

    fun getPatientDetails(){
        withNetworkCheck(connectivityManager, onNetworkAvailable = {
            supportFragmentManager.findFragmentById(R.id.patientDetailFragment).let {
                    getPatientId()?.let { id ->
                        patientDetailViewModel.getPatients(
                            id,
                            origin = getMenuOrigin()
                        )
                    }
                }
        })
    }

    fun attachObservers() {
        viewModel.ncdMedicalReviewStaticLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    initView()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                    showErrorDialog()
                }
            }
        }

        viewModel.createMedicalReview.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    // navigate to summary
                    resourceState.data?.let {
                        loadSummary()
                    }
                }

                ResourceState.ERROR -> {
                    hideLoading()
                    showError()
                }
            }
        }
        patientDetailViewModel.patientDetailsLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
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
                    if (binding.refreshLayout.isRefreshing) {
                        binding.refreshLayout.isRefreshing = false
                    }
                    showError(true)
                }
            }
        }
        summaryViewModel.createNCDMRSummaryCreate.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    summarySuccessDialog()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                    if (binding.refreshLayout.isRefreshing) {
                        binding.refreshLayout.isRefreshing = false
                    }
                    showError(true)
                }
            }
        }
    }

    private fun summarySuccessDialog() {
        val dialog =
            supportFragmentManager.findFragmentByTag(MedicalReviewSuccessDialogFragment.TAG)
        if (dialog == null) {
            MedicalReviewSuccessDialogFragment.newInstance(patientDetailViewModel.isPatientEnrolled()).show(
                supportFragmentManager,
                MedicalReviewSuccessDialogFragment.TAG
            )
        }
    }

    override fun onDialogDismissListener(isFinish: Boolean) {
        if (isFinish) {
            // enrolled click in success dialog
            navigateToEnrolled()
        } else {
            startActivityWithoutSplashScreen()
        }
    }

    private fun navigateToEnrolled() {
        val intent = Intent(this, RegistrationActivity::class.java).apply {
            putExtra(DefinedParams.FhirId, patientDetailViewModel.getPatientFHIRId())
            putExtra(DefinedParams.PatientId, patientDetailViewModel.getPatientId())
            putExtra(ORIGIN, MenuConstants.REGISTRATION.lowercase())
            putExtra(DefinedParams.Gender, patientDetailViewModel.getGender())
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
    private fun showError(isActivityClosed: Boolean = false) {
        showErrorDialogue(
            title = getString(R.string.alert),
            message = getString(R.string.something_went_wrong_try_later),
            positiveButtonName = getString(R.string.ok),
        ) { isPositiveResult ->
            if (isPositiveResult && isActivityClosed) {
                onBackPressPopStack()
            }
        }
    }

    private fun loadSummary() {
        binding.apply {
            comorbiditiesContainer.gone()
            medicalDiagnosisContainer.gone()
            complicationsContainer.gone()
            lifestyleAssessmentContainer.gone()
            chiefComplaintsContainer.gone()
            clinicalNotesContainer.gone()
            obstetricExaminationContainer.visible()
            btnLayout.btnNext.text = getString(R.string.submit)
            btnLayout.btnNext.isAllCaps = true
        }
        replaceFragment(
            R.id.obstetricExaminationContainer,
            NCDMedicalReviewSummaryFragment.TAG,
            NCDMedicalReviewSummaryFragment.newInstance(getEncounterReference(),getMenuId())
        )
    }

    private fun onBackPressPopStack() {
        this@NCDMedicalReviewActivity.finish()
    }

    private fun initializeStaticDataSave() {
        if (!(SecuredPreference.getBoolean(SecuredPreference.EnvironmentKey.IS_NCD_MEDICAL_REVIEW_LOADED.name))) {
            withNetworkAvailability(online = {
                viewModel.getStaticMetaData()
            }, offline = {
                onBackPressPopStack()
            })
        } else {
            initView()
        }
    }

    fun initView() {
        binding.btnLayout.clPsycMenu.setVisible(CommonUtils.isPsychologicalFlowEnabled())
        initializePatientDetails()
    }

    private fun setListeners() {
        binding.btnLayout.btnNext.safeClickListener(this)
        binding.btnLayout.ivPrescriptionImgView.safeClickListener(this)
        binding.btnLayout.ivTreatmentPlan.safeClickListener(this)
        binding.btnLayout.ivInvestigation.safeClickListener(this)
        binding.btnLayout.ivLifestyle.safeClickListener(this)
        binding.btnLayout.clPsycMenu.safeClickListener(this)
    }

    private fun getPatientId(): String? {
        return intent.getStringExtra(DefinedParams.FhirId)
    }

    private fun getEncounterReference(): String? {
        return intent.getStringExtra(EncounterReference)
    }

    private fun getMenuOrigin(): String? {
        return intent.getStringExtra(ORIGIN)
    }

    private fun getMenuId(): String? {
        return when (intent.getStringExtra(MENU_ID)?.lowercase()) {
            NCD.lowercase() -> {
                return NCD.lowercase()
            }

            MATERNAL_HEALTH.lowercase() -> {
                return DefinedParams.PregnancyANC.lowercase()
            }

            MENTAL_HEALTH.lowercase() -> {
                return MENTAL_HEALTH.lowercase()
            }

            else -> {
                null
            }
        }
    }


    private fun initializePatientDetails() {
        patientDetailViewModel.origin = intent.extras?.getString(ORIGIN)
        val fragment = PatientInfoFragment.newInstanceForNCD(getPatientId(), getMenuOrigin() ?: "")
        fragment.setDataCallback(this)
        addOrReuseFragment(
            R.id.patientDetailFragment,
            PatientInfoFragment.TAG,
            fragment
        )
        hideLoading()
    }

    private fun initializeFragments() {
        replaceFragment(
            R.id.medicalDiagnosisContainer,
            NCDMedicalReviewDiagnosisCardFragment.TAG,
            NCDMedicalReviewDiagnosisCardFragment.newInstance(
                patientDetailViewModel.getNCDInitialMedicalReview(),
                patientDetailViewModel.getGenderIsFemale(),
                getMenuId()
            )
        )
        hideLoading()
    }

    private fun loadFragment(isInitialMR: Boolean) {
        showLoading()
        if (isInitialMR) {
            binding.apply {
                comorbiditiesContainer.visible()
                complicationsContainer.visible()
                lifestyleAssessmentContainer.visible()
                chiefComplaintsContainer.gone()
                clinicalNotesContainer.gone()
                obstetricExaminationContainer.gone()
                btnLayout.btnNext.text = getString(R.string.next)
                btnLayout.btnNext.isAllCaps = true
            }
            showCurrentMedication()
            addOrReuseFragment(
                R.id.comorbiditiesContainer,
                NCDComorbiditiesFragment.TAG,
                NCDComorbiditiesFragment.newInstance(getMenuId())
            )
            addOrReuseFragment(
                R.id.complicationsContainer,
                NCDComplicationsFragment.TAG,
                NCDComplicationsFragment.newInstance()
            )
            addOrReuseFragment(
                R.id.lifestyleAssessmentContainer,
                NCDLifestyleAssessmentFragment.TAG,
                NCDLifestyleAssessmentFragment.newInstance()
            )
        } else {
            binding.apply {
                comorbiditiesContainer.gone()
                complicationsContainer.gone()
                currentMedicationContainer.gone()
                lifestyleAssessmentContainer.gone()
                chiefComplaintsContainer.visible()
                clinicalNotesContainer.visible()
                obstetricExaminationContainer.visible()
                btnLayout.btnNext.text = getString(R.string.submit)
                btnLayout.btnNext.isAllCaps = true
            }
            addOrReuseFragment(
                R.id.chiefComplaintsContainer,
                NCDChiefComplaintsFragment.TAG,
                NCDChiefComplaintsFragment.newInstance(getMenuId())
            )
            addOrReuseFragment(
                R.id.clinicalNotesContainer,
                NCDClinicalNotesFragment.TAG,
                NCDClinicalNotesFragment.newInstance()
            )
            addOrReuseFragment(
                R.id.obstetricExaminationContainer,
                NCDObstetricExaminationFragment.TAG,
                NCDObstetricExaminationFragment.newInstance(getMenuId())
            )
        }
        hideLoading()
    }

    fun showCurrentMedication() {
        val dialogFragment =
            supportFragmentManager.findFragmentByTag(NCDPatientHistoryDialog.TAG) as? NCDPatientHistoryDialog
        dialogFragment?.dismiss()
        val iscomorbiditiesContainer  = supportFragmentManager.findFragmentById(R.id.comorbiditiesContainer) is NCDComorbiditiesFragment
        binding.apply {
            currentMedicationContainer.gone()
            // current only visible on ncd and after patient history submit diabetes known patient(also visible state of NCDComorbiditiesFragment(it show only on initial medical review))
            if ((getMenuId().equals(NCD, true)
                &&( viewModel.statusDiabetesValue != null
                && !patientDetailViewModel.getNCDInitialMedicalReview()))
                && (iscomorbiditiesContainer && binding.comorbiditiesContainer.isVisible())) {
                currentMedicationContainer.visible()
                addOrReuseFragment(
                    R.id.currentMedicationContainer,
                    NCDCurrentMedicationFragment.TAG,
                    NCDCurrentMedicationFragment.newInstance()
                )
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.btnLayout.btnNext.id -> {
                val isComorbiditiesFragmentVisible = binding.comorbiditiesContainer.isVisible
                val isChiefComplaintsFragmentVisible = binding.chiefComplaintsContainer.isVisible
                val isInitialReview = patientDetailViewModel.getNCDInitialMedicalReview()
                val isSummary = supportFragmentManager.findFragmentByTag(NCDMedicalReviewSummaryFragment.TAG) is NCDMedicalReviewSummaryFragment
                when {
                    isSummary -> {
                        submitNextVisitCreate()
                    }
                    !isInitialReview && isComorbiditiesFragmentVisible -> {
                        if (validateInput()) {
                            loadFragment(false)
                        }
                    }

                    !isInitialReview && isChiefComplaintsFragmentVisible -> {
                        if (validateInputCMR() && handleValidation()) {
                            getInitialMedicalReviewData()
                        }
                    }

                    isInitialReview && isChiefComplaintsFragmentVisible -> {
                        // Add your logic here for when chief complaints fragment is visible and initial review
                        if (validateInputCMR()) {
                            getInitialMedicalReviewData()
                        }
                    }
                }
            }

            binding.btnLayout.ivPrescriptionImgView.id -> {
                if (connectivityManager.isNetworkAvailable()) {
                    val intent = Intent(this, NCDPrescriptionActivity::class.java)
                    intent.putExtra(ORIGIN, DefinedParams.MedicalReview)
                    intent.putExtra(DefinedParams.PatientId, patientDetailViewModel.getPatientId()  )
                    intent.putExtra(DefinedParams.id,  patientDetailViewModel.getPatientFHIRId())
                    intent.putExtra(DefinedParams.PatientVisitId, getEncounterReference())
                    startActivity(intent)
                } else {
                   showErrorDialogue(
                        getString(R.string.title_no_network),
                        getString(R.string.message_no_network),
                        isNegativeButtonNeed = false
                    ) { _ -> }
                }
            }


            binding.btnLayout.ivTreatmentPlan.id -> {
                val patientId = patientDetailViewModel.getPatientId()
                val fhirId = patientDetailViewModel.getPatientFHIRId()
                if (patientId.isNullOrBlank() || fhirId.isNullOrBlank())
                    return
                else {
                    val dialog =
                        NCDTreatmentPlanDialog.newInstance(
                            patientId,
                            fhirId
                        ) { isPositiveResult, message ->
                            if (isPositiveResult)
                                showSuccessDialogue(
                                    title = getString(R.string.treatment_plan),
                                    message = message
                                ) {}
                            else
                                showErrorDialogue(
                                    title = getString(R.string.error),
                                    message = message,
                                    positiveButtonName = getString(R.string.ok),
                                ) {}
                        }
                    dialog.show(supportFragmentManager, NCDTreatmentPlanDialog.TAG)
                }
            }
            binding.btnLayout.ivInvestigation.id  -> {
                patientDetailViewModel.patientDetailsLiveData.value?.data?.let { data ->
                    val intent = Intent(this, InvestigationActivity::class.java)
                    intent.putExtra(DefinedParams.PatientId, data.id)
                    // TODO need to get investigation in summary(After confirm with backend)
                    intent.putExtra(EncounterReference, getEncounterReference())
                    intent.putExtra(MemberID, data.id)
                    intent.putExtra(ORIGIN, getMenuOrigin())
                    intent.putExtra(NCD,NCD)
                    getResult.launch(intent)
                }
            }
            binding.btnLayout.ivLifestyle.id -> navigateUser(
                Intent(
                    this,
                    NCDLifestyleActivity::class.java
                )
            )

            binding.btnLayout.clPsycMenu.id -> navigateUser(
                Intent(
                    this,
                    NCDCounselingActivity::class.java
                )
            )
        }
    }

    private fun navigateUser(intent: Intent) {
        val bundle = Bundle()
        bundle.putString(NCDMRUtil.PATIENT_REFERENCE, patientDetailViewModel.getPatientId())
        bundle.putString(NCDMRUtil.MEMBER_REFERENCE, patientDetailViewModel.getPatientFHIRId())
        bundle.putString(NCDMRUtil.VISIT_ID, getEncounterReference())
        intent.putExtras(bundle)
        startActivity(intent)
    }

    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                swipeRefresh()
            }
        }
    private fun submitNextVisitCreate() {
        val fragment =
            supportFragmentManager.findFragmentByTag(NCDMedicalReviewSummaryFragment.TAG) as? NCDMedicalReviewSummaryFragment
        if (fragment?.validateInput() == true && fragment.handleConfirmDiagnoses()) {
            withNetworkAvailability(online = {
                val request = NCDMRSummaryRequestResponse(
                    memberReference = patientDetailViewModel.getPatientFHIRId(),
                    patientReference = patientDetailViewModel.getPatientId(),
                    nextMedicalReviewDate = DateUtils.convertDateTimeToDate(
                        summaryViewModel.nextFollowupDate,
                        DateUtils.DATE_ddMMyyyy,
                        DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                        inUTC = true
                    ),
                    provenance = ProvanceDto()
                )
                summaryViewModel.createNCDMRSummaryCreate(request)
            })
        }
    }
    private fun handleValidation(): Boolean {
        // Function to show the error dialog with customizable options
        fun showErrorDialog(
            message: String,
            showConfirm: Boolean = false,
            showYesNo: Pair<Boolean, Boolean> = Pair(true, true)
        ) {
            val existingDialog = supportFragmentManager.findFragmentByTag(NCDMRAlertDialog.TAG)
            if (existingDialog == null) {
                NCDMRAlertDialog.newInstance(
                    getString(R.string.alert),
                    message = message,
                    showYesNoClose = Triple(showYesNo.first, showYesNo.second, true),
                    showConfirm = showConfirm,
                    callback = this
                ).show(supportFragmentManager, NCDMRAlertDialog.TAG)
            }
        }

        val diagnosisList =
            medicalReviewDiagnosisCardViewModel.getConfirmDiagonsis.value?.data?.diagnosis
        val isDiagnosisListEmpty = diagnosisList.isNullOrEmpty()
        val isKnownDiabetes = viewModel.statusDiabetesValue != null

        // Show error if diagnosis list is empty and patient has no known diabetes status
        if (isDiagnosisListEmpty && !isKnownDiabetes) {
            showErrorDialog(getString(R.string.no_confirm_diagnosis_warning))
            return false
        }

        // If the menu ID is NCD and the patient has a known diabetes status
        if (getMenuId().equals(NCD, ignoreCase = true) && isKnownDiabetes) {
            // Show error if diagnosis list is empty for a known diabetes patient
            if (isDiagnosisListEmpty) {
                showErrorDialog(
                    getString(R.string.no_confirm_diagnosis_mandatory_warning),
                    showConfirm = true,
                    showYesNo = Pair(false, false)
                )
                return false
            }

            /*
             * 1. Check for matching diagnosis in the validation list.
             * 2. After submitting the patient history for NCD, retrieve the data from the ViewModel.
             * 3. Extract all diabetes types from the confirm diagnoses.
             * 4. Compare these types with list (e.g., gestational diabetes, diabetes type 1, type 2, pre-diabetes).
             * 5. For each comparison, obtain the confirmation value for patient history ncd.
             * 6. For example, if compare get confirm diagnoses against the list(contain only diabetes),then  it filter diabetes only then match the diabetes of patient history NCD.
             */
            val mismatchedItems = diagnosisList?.any { diagnosisItem ->
                viewModel.validationForStatus?.any { validationItem ->
                    validationItem.value == diagnosisItem.value && viewModel.statusDiabetesValue == validationItem.value
                } == true
            }

            // Show error if there are any mismatched diagnoses
            if (mismatchedItems == false) {
                showErrorDialog(
                    getString(R.string.edit_confirm_diagnosis_mandatory_warning),
                    showConfirm = true,
                    showYesNo = Pair(false, false)
                )
                return false
            }
        }
        return true
    }

    private fun getInitialMedicalReviewData() {
        val request = MedicalReviewRequestResponse(
            patientReference = patientDetailViewModel.getPatientId(),
            memberReference = patientDetailViewModel.getPatientFHIRId(),
            provenance = ProvanceDto(),
            encounterReference = getEncounterReference()
        ).apply {
            initialMedicalReview = InitialMedicalReview().apply {
                //TODO request to make it generically in backend
                if (getMenuId().equals(NCD, true)
                    && viewModel.statusDiabetesValue != null
                    && !patientDetailViewModel.getNCDInitialMedicalReview()
                ) {
                    currentMedications = CurrentMedications(
                        medications = currentMedicationViewModel.chips.mapNotNull { chip ->
                            Chip(
                                id = chip.id,
                                name = chip.name,
                                value = if (chip.name.equals(
                                        DefinedParams.Other,
                                        true
                                    )
                                ) currentMedicationViewModel.comments.trim()
                                    .takeIf { it.isNotBlank() } else chip.value,
                                other = chip.name.equals(DefinedParams.Other, true)
                            )
                        },
                        drugAllergies = currentMedicationViewModel.drugAllergies,
                        adheringCurrentMed = currentMedicationViewModel.adheringCurrentMed,
                        adheringMedComment = currentMedicationViewModel.adheringMedComment,
                        allergiesComment = currentMedicationViewModel.allergiesComment
                    )
                }
                comorbidities = comorbiditiesViewModel.chips.map { chip ->
                    Chip(
                        id = chip.id,
                        name = chip.name,
                        value = if (chip.name.equals(
                                DefinedParams.Other,
                                true
                            )
                        ) comorbiditiesViewModel.comments.trim().takeIf { it.isNotBlank() } else chip.value,
                        other = chip.name.equals(DefinedParams.Other, true)
                    )
                }

                complications = complicationsViewModel.chips.map { chip ->
                    Chip(
                        id = chip.id,
                        name = chip.name,
                        value = if (chip.name.equals(
                                DefinedParams.Other,
                                true
                            )
                        ) complicationsViewModel.comments.trim().takeIf { it.isNotBlank() } else chip.value,
                        other = chip.name.equals(DefinedParams.Other, true)
                    )
                }

                lifestyle = lifestyleAssessmentViewModel.lifestyle?.map { lifeStyle ->
                    Chip(
                        id = lifeStyle.id,
                        name = lifeStyle.lifestyleQuestion,
                        value = lifeStyle.questionValue,
                        answer = Answer(
                            name = lifeStyle.lifestyleAnswer,
                            value = lifeStyle.answerValue
                        ),
                        comments = lifeStyle.comments?.trim().takeIf { it?.isNotBlank() == true }
                    )
                }
            }
            continuousMedicalReview = ContinuousMedicalReview(
                physicalExams = obstetricExaminationViewModel.chips.map { chip ->
                    Chip(
                        id = chip.id,
                        name = chip.name,
                        value = chip.value,
                        other = chip.name.equals(DefinedParams.Other, true)
                    )
                },
                clinicalNote = clinicalNotesViewModel.comments.trim().takeIf { it.isNotBlank() },
                complaints = chiefComplaintsViewModel.chips.map { chip ->
                    Chip(
                        id = chip.id,
                        name = chip.name,
                        value = chip.value,
                        other = chip.name.equals(DefinedParams.Other, true)
                    )
                },
                physicalExamComments = obstetricExaminationViewModel.comments.trim().takeIf { it.isNotBlank() },
                complaintComments = chiefComplaintsViewModel.comments.trim().takeIf { it.isNotBlank() }
            )
        }
        withNetworkCheck(connectivityManager, onNetworkAvailable = {
            viewModel.createNCDMedicalReview(request)
        })
    }

    override fun onDataLoaded(details: PatientListRespModel) {
        val comorbiditiesFragment =
            supportFragmentManager.findFragmentById(R.id.comorbiditiesContainer) as? NCDComorbiditiesFragment
        val chiefComplaintsFragment =
            supportFragmentManager.findFragmentById(R.id.chiefComplaintsContainer) as? NCDChiefComplaintsFragment
        val summaryFragment =
            supportFragmentManager.findFragmentById(R.id.obstetricExaminationContainer) as? NCDMedicalReviewSummaryFragment
        when {
            details.initialReviewed == false -> {
                when {
                    summaryFragment != null &&(!binding.chiefComplaintsContainer.isVisible && !binding.comorbiditiesContainer.isVisible) -> loadSummary()
                    comorbiditiesFragment != null && !binding.chiefComplaintsContainer.isVisible -> loadFragment(true)
                    chiefComplaintsFragment != null && !binding.comorbiditiesContainer.isVisible -> loadFragment(false)
                    else -> {
                        showNcdPatientStatus()
                        showMaternalStatus()
                        loadFragment(true)
                    }
                }
            }

            details.initialReviewed == true -> {
                if (summaryFragment != null &&(!binding.chiefComplaintsContainer.isVisible && !binding.comorbiditiesContainer.isVisible)) {
                    // Summary logic can go here if needed
                    loadSummary()
                } else {
                    loadFragment(false)
                }
            }
        }
        if (summaryFragment == null) {
            initializeFragments()
        }
    }

    private fun showNcdPatientStatus() {
        if (getMenuId().equals(NCD.lowercase(), true)) {
            patientDetailViewModel.getPatientId()?.let {
                NCDPatientHistoryDialog.newInstance(
                    it,
                    patientDetailViewModel.getPatientFHIRId(),
                    true,
                    isFemale = patientDetailViewModel.getGenderIsFemale()
                ).apply {
                    listener = this@NCDMedicalReviewActivity
                }.show(supportFragmentManager, NCDPatientHistoryDialog.TAG)
            }
        }
    }

    private fun showMaternalStatus() {
        if (getMenuId().equals(DefinedParams.PregnancyANC.lowercase(), true)) {
            withNetworkAvailability(online = {
                patientDetailViewModel.getPatientFHIRId()?.let { id ->
                    val dialog = supportFragmentManager.findFragmentByTag(NCDPregnancyDialog.TAG)
                    if (dialog == null) {
                        val ncdPregnancyDialog =
                            NCDPregnancyDialog.newInstance(patientId = id) { isPositiveResult, message ->
                                if (isPositiveResult) showErrorDialogue(
                                    title = getString(R.string.pregnancy_details),
                                    message = message,
                                    isNegativeButtonNeed = false
                                ) {
                                    swipeRefresh()
                                }
                                else showErrorDialogue(
                                    title = getString(R.string.error),
                                    message = message,
                                    isNegativeButtonNeed = false
                                ) {

                                }
                            }
                        ncdPregnancyDialog.show(supportFragmentManager, NCDPregnancyDialog.TAG)
                    }
                }
            })
        }
    }

    fun validateInput(): Boolean {
        val fragmentOne =
            supportFragmentManager.findFragmentById(R.id.comorbiditiesContainer) as? NCDComorbiditiesFragment
        val fragmentTwo =
            supportFragmentManager.findFragmentById(R.id.complicationsContainer) as? NCDComplicationsFragment
        val fragmentThree =
            supportFragmentManager.findFragmentById(R.id.lifestyleAssessmentContainer) as? NCDLifestyleAssessmentFragment
        val fragmentFour =
            supportFragmentManager.findFragmentById(R.id.currentMedicationContainer) as? NCDCurrentMedicationFragment
        // Execute all validations

        val valueCurrentMedication = fragmentFour?.validateInput(true)
        val valueComorbidities = fragmentOne?.validateInput()
        val valueComplications = fragmentTwo?.validateInput()
        val valueLifestyle = fragmentThree?.validateInput()

        val isCurrentMedication =
            if (getMenuId().equals(NCD, true) && viewModel.statusDiabetesValue != null) {
                valueCurrentMedication?.first == true
            } else {
                true
            }
        val isValidComorbidities = valueComorbidities?.first == true
        val isValidComplications = valueComplications?.first == true
        val isValidLifestyle = valueLifestyle?.first == true

        val firstInvalidView = when {
            !isCurrentMedication -> valueCurrentMedication?.second
            !isValidComorbidities -> valueComorbidities?.second
            !isValidComplications -> valueComplications?.second
            !isValidLifestyle -> valueLifestyle?.second
            else -> null
        }
        // Request focus on the first invalid view, if it exists, and return validation result
        firstInvalidView?.requestFocus()
        // Return true only if all validations are true
        return isValidComorbidities && isValidComplications && isValidLifestyle && isCurrentMedication
    }

    private fun validateInputCMR(): Boolean {
        val fragmentOne =
            supportFragmentManager.findFragmentById(R.id.chiefComplaintsContainer) as? NCDChiefComplaintsFragment
        val fragmentTwo =
            supportFragmentManager.findFragmentById(R.id.clinicalNotesContainer) as? NCDClinicalNotesFragment
        val fragmentThree =
            supportFragmentManager.findFragmentById(R.id.obstetricExaminationContainer) as? NCDObstetricExaminationFragment
        val isValidChiefComplaints = fragmentOne?.validateInput()?.first == true
        val isValidClinicalNotes = fragmentTwo?.validateInput()?.first == true
        val isValidObstetricExamination = fragmentThree?.validateInput()?.first == true

        val firstInvalidView = when {
            !isValidChiefComplaints -> fragmentOne?.validateInput()?.second
            !isValidClinicalNotes -> fragmentTwo?.validateInput()?.second
            !isValidObstetricExamination -> fragmentThree?.validateInput()?.second
            else -> null
        }
        // Request focus on the first invalid view, if it exists, and return validation result
        firstInvalidView?.requestFocus()
        return isValidChiefComplaints && isValidClinicalNotes && isValidObstetricExamination
    }

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backNavigation()
            }
        }

    fun backNavigation() {
        showLoading()
        val isComorbiditiesFragmentVisible = binding.comorbiditiesContainer.isVisible
        val isChiefComplaintsFragmentVisible = binding.chiefComplaintsContainer.isVisible
        val isSummaryVisible = binding.obstetricExaminationContainer.isVisible
        val isInitialReview = patientDetailViewModel.getNCDInitialMedicalReview()
        when {
            !isInitialReview && isComorbiditiesFragmentVisible -> showErrorDialog()
            !isInitialReview && isChiefComplaintsFragmentVisible -> {
                loadFragment(true)
                hideLoading()
            }

            isInitialReview && isChiefComplaintsFragmentVisible -> showErrorDialog()
            isInitialReview && isSummaryVisible -> showErrorDialog()
            !isInitialReview && (isSummaryVisible && (!isComorbiditiesFragmentVisible && !isChiefComplaintsFragmentVisible)) -> showErrorDialog()
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
        hideLoading()
    }

    private fun showErrorDialogHome() {
        showErrorDialogue(
            getString(R.string.alert),
            getString(R.string.exit_reason),
            isNegativeButtonNeed = true
        ) { isPositive ->
            if (isPositive) {
               startActivityWithoutSplashScreen()
            }
        }
        hideLoading()
    }

    fun swipeRefresh() {
        withNetworkCheck(connectivityManager, onNetworkAvailable = {
            supportFragmentManager.findFragmentById(R.id.patientDetailFragment)
                .let {
                    getPatientId()?.let { id ->
                        patientDetailViewModel.getPatients(
                            id,
                            origin = getMenuOrigin()
                        )
                    }
                }
        }) {
            if (binding.refreshLayout.isRefreshing) {
                binding.refreshLayout.isRefreshing = false
            }
        }
    }

    override fun onDialogDismissed(isConfirmed: Boolean) {
        if (isConfirmed) {
            // to refresh the card
            initializeFragments()
            showCurrentMedication()
            swipeRefresh()
        }
    }

    override fun onYesClicked() {
        if (validateInputCMR()) {
            getInitialMedicalReviewData()
        }
    }

    override fun onConfirmDiagnosisClicked() {
        withNetworkAvailability(online = {
            showConfirmDiagnoses()
        })
    }

    fun showConfirmDiagnoses() {
        val dialog = supportFragmentManager.findFragmentByTag(NCDDiagnosisDialogFragment.TAG)
        if (dialog == null) {
            patientDetailViewModel.getPatientId()?.let {
                NCDDiagnosisDialogFragment.newInstance(
                    it,
                    getTypeForDiagnoses(getMenuId()),
                    patientDetailViewModel.getGenderIsFemale(),
                    getConfirmDiagnoses(getMenuId())
                ).apply {
                    listener = this@NCDMedicalReviewActivity
                }.show(supportFragmentManager, NCDDiagnosisDialogFragment.TAG)
            }
        }
    }
}