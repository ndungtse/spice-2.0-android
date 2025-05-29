package com.medtroniclabs.spice.ui.medicalreview.motherneonate.emtct.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.takeIfNotNull
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.SpiceLocationManager
import com.medtroniclabs.spice.data.model.HivRequestData
import com.medtroniclabs.spice.data.model.MedicalReviewEncounter
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.databinding.ActivityMotherNeonateEmtctctivityBinding
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
import com.medtroniclabs.spice.ui.medicalreview.hiv.fragment.*
import com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel.HivGeneralAndSystemicExaminationViewModel
import com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel.HivImrAndCmrViewModel
import com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel.HivImrCmrSummaryViewModel
import com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel.HivViewModel
import com.medtroniclabs.spice.ui.medicalreview.investigation.InvestigationActivity
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.AncVisitCallBack
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.fragment.PregnancySummaryFragment
import com.medtroniclabs.spice.ui.medicalreview.prescription.PrescriptionActivity
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.CLINICAL_NOTES
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.isViralLoadTestRecommended
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientInfoFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.ReferPatientFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.MotherNeonateBpWeightViewModel
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import com.medtroniclabs.spice.ui.mypatients.viewmodel.ReferPatientViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import kotlin.math.log
import kotlin.math.log2

@AndroidEntryPoint
class MotherNeonateEMTCTActivity : BaseActivity(), AncVisitCallBack, View.OnClickListener,
    OnDialogDismissListener {

    companion object {
        private const val TAG = "MotherNeonateEMTCTActivity"
    }

    private val binding: ActivityMotherNeonateEmtctctivityBinding by lazy {
        ActivityMotherNeonateEmtctctivityBinding.inflate(layoutInflater)
    }

    // ViewModels
    private val hivViewModel: HivViewModel by viewModels()
    private val patientViewModel: PatientDetailViewModel by viewModels()
    private val presentingComplaintsViewModel: PresentingComplaintsViewModel by viewModels()
    private val weightViewModel: MotherNeonateBpWeightViewModel by viewModels()
    private val hivGeneralAndSystemicExaminationViewModel: HivGeneralAndSystemicExaminationViewModel by viewModels()
    private val clinicalNotesViewModel: ClinicalNotesViewModel by viewModels()
    private val viewModel: HivImrAndCmrViewModel by viewModels()
    private val summaryViewModel: HivImrCmrSummaryViewModel by viewModels()
    private val referPatientViewModel: ReferPatientViewModel by viewModels()
    private val systemicExaminationViewModel: SystemicExaminationViewModel by viewModels()

    private val getResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            handleActivityResult(result.data)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupView()
        initStaticDataCall()
        attachObserver()
        setupSwipeRefresh()
        setButtonClickListener()
    }

    private fun handleActivityResult(data: Intent?) {
        data?.getStringExtra(DefinedParams.EncounterId)?.let { valueString ->
            patientViewModel.encounterId = valueString
        }
        
        when {
            data?.getBooleanExtra(DefinedParams.Investigation, false) == true -> {
                refreshViralLoadFragment()
            }
            data?.getBooleanExtra(DefinedParams.PRESCRIPTION, false) == true -> {
                refreshARTRegimenFragment()
            }
        }
    }

    private fun refreshViralLoadFragment() {
        (supportFragmentManager.findFragmentById(R.id.viralLoadResultContainer) as? ViralLoadFragment)?.apply {
            refreshFragment(
                intent.getStringExtra(DefinedParams.ID),
                intent.getStringExtra(DefinedParams.MemberID)
            )
        }
    }

    private fun refreshARTRegimenFragment() {
        (supportFragmentManager.findFragmentById(R.id.aRTRegimenResultContainer) as? ARTRegimenFragment)?.apply {
            refreshFragment(
                intent.getStringExtra(DefinedParams.PatientId),
                intent.getStringExtra(DefinedParams.ID)
            )
        }
    }

    private fun setupView() {
        setMainContentView(
            binding.root,
            true,
            getString(R.string.patient_medical_review),
            homeAndBackVisibility = Pair(true, true),
            callback = { backNavigation() },
            callbackHome = { backNavigationToHome() }
        )

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backNavigation()
            }
        })
    }

    private fun backNavigation() {
        showErrorDialogue(
            getString(R.string.alert),
            getString(R.string.exit_reason),
            isNegativeButtonNeed = true
        ) { isPositive ->
            if (isPositive) {
                hivViewModel.isHivSummary = false
                finishWithResultOk()
            }
        }
    }

    private fun backNavigationToHome() {
        showErrorDialogue(
            getString(R.string.alert),
            getString(R.string.exit_reason),
            isNegativeButtonNeed = true
        ) { isPositive ->
            if (isPositive) {
                hivViewModel.isHivSummary = false
                startActivityWithoutSplashScreen()
            }
        }
    }

    private fun finishWithResultOk() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun initStaticDataCall() {
        if (!(SecuredPreference.getBoolean(SecuredPreference.EnvironmentKey.IS_HIV_DATA_LOADED.name))) {
            if (connectivityManager.isNetworkAvailable()) {
                hivViewModel.getHivMetaData()
            } else {
                showErrorDialogue(
                    getString(R.string.error), getString(R.string.no_internet_error),
                    isNegativeButtonNeed = false,
                ) {}
            }
        } else {
            initializePatientDetailFragment()
        }
        hivViewModel.patientId = intent.getStringExtra(DefinedParams.PatientId)

    }

    private fun initializePatientDetailFragment() {
        replaceFragment(
            R.id.patientDetailFragment,
            PatientInfoFragment.TAG,
            PatientInfoFragment.newInstanceForEMTCT(
                intent.getStringExtra(DefinedParams.PatientId),
                isEMTCTMR = true,
                isEMTCTSummary = hivViewModel.isHivSummary
            ).apply {
                setDataCallback(this@MotherNeonateEMTCTActivity)
            }
        )

    }

    private fun attachObserver() {
        viewModel.checkRecommendationRInvestigations.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    resource.data?.let {
                        if (it[isViralLoadTestRecommended] == true) {
                            showDialogIfNotPresent(
                                RecommendedInvestigationsDialog.TAG
                            ) {
                                RecommendedInvestigationsDialog.newInstance().apply {
                                    onOkayClickListener = {
                                        openInvestigationActivity()
                                    }
                                    onCancelClickListener = {
                                        submitEmtctRequest()
                                    }
                                }
                            }
                        }else{
                            submitEmtctRequest()
                        }
                    }
                }

                ResourceState.ERROR -> {
                    hideLoading()
                    showErrorDialogue(
                        title = getString(R.string.alert),
                        message = getString(R.string.something_went_wrong_try_later),
                        positiveButtonName = getString(R.string.ok),
                    ) {

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
                        positiveButtonName = getString(R.string.ok),
                    ) {

                    }
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    showDialogIfNotPresent(
                        MedicalReviewSuccessDialogFragment.TAG
                    ) {
                        MedicalReviewSuccessDialogFragment.newInstance()
                    }
                }
            }
        }
        hivViewModel.hivMetaResponseLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                    showErrorDialogue(
                        title = getString(R.string.alert),
                        message = getString(R.string.something_went_wrong_try_later),
                        positiveButtonName = getString(R.string.ok),
                    ) {
                        if (it) {
                            //onBackPressPopStack()
                        }
                    }
                }

                ResourceState.SUCCESS -> {
                    initializePatientDetailFragment()
                    hideLoading()
                }
            }
        }
        viewModel.hivCreateResponse.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    resource.data?.let {
                        showSummary {
                            setupSwipeRefresh()
                        }
                    }
                }

                ResourceState.ERROR -> {
                    hideLoading()
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
                    showDialogIfNotPresent(
                        MedicalReviewSuccessDialogFragment.TAG
                    ) {
                        MedicalReviewSuccessDialogFragment.newInstance()
                    }
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
                            onBackPressPopStack()
                        }
                    }
                }
            }
        }


    }

    override fun onDataLoaded(details: PatientListRespModel) {
        hivViewModel.memberId = details.memberId

        if(hivViewModel.isHivSummary){

        }else {
            hivViewModel.ancVisit =
                details.pregnancyDetails?.ancVisitMedicalReview?.takeIf { true }?.plus(1) ?: 1
            initializeReviewFragments()
        }
    }

    private fun initializeReviewFragments() {
        setupResultListeners()
        binding.patientDetailFragment.visible()
        binding.patientBMIContainer.visible()

        replaceFragment(
            R.id.patientBMIContainer,
            HivMedicalReviewDiagnosesFragment.TAG,
            HivMedicalReviewDiagnosesFragment.newInstance(true,true)
        )

        initEmtctFragments()
    }

    private fun setupSwipeRefresh() {
        binding.refreshLayout.setOnRefreshListener {
            withNetworkAvailability(online = {
                supportFragmentManager.findFragmentById(R.id.patientDetailFragment)
                    .let {
                        patientViewModel.getPatientId()?.let { id ->
                            patientViewModel.getPatients(id)
                        }
                    }
            }, offline = {
                if (binding.refreshLayout.isRefreshing) {
                    binding.refreshLayout.isRefreshing = false
                }
            })
        }
    }

    private fun enableSubmitButton() {
        val clinicalNotesFragment =
            supportFragmentManager.findFragmentById(R.id.clinicalNotesContainer) as? ClinicalNotesFragment
        val isClinicalNotesValid = clinicalNotesFragment?.validateInput()

        binding.btnSubmit.isEnabled = isClinicalNotesValid ?: false
    }

    private fun setupResultListeners() {
        val listener: (String, Bundle) -> Unit = { _, _ -> enableSubmitButton() }
        supportFragmentManager.setFragmentResultListener(CLINICAL_NOTES, this, listener)
    }

    private fun initEmtctFragments() {
        addOrReuseFragment(
            R.id.pregnancySummaryContainer,
            PregnancySummaryFragment.TAG,
            PregnancySummaryFragment.newInstanceEmtct(isEmtct =  true,patientReference = intent.getStringExtra(DefinedParams.ID))
        )

        val hivBundle = Bundle().apply {
            putString(
                MedicalReviewTypeEnums.PresentingComplaints.name,
                MedicalReviewTypeEnums.HIV.name
            )
            putString(
                MedicalReviewTypeEnums.SystemicExaminations.name,
                MedicalReviewTypeEnums.HIV.name
            )
        }

        replaceFragmentOrCreateNewFragment<PresentingComplaintsFragment>(
            R.id.presentingComplaintsContainer,
            bundle = hivBundle,
            tag = PresentingComplaintsFragment.TAG
        )

        replaceFragmentOrCreateNewFragment<SystemicExaminationsFragment>(
            R.id.obstetricExaminationContainer,
            bundle = hivBundle,
            tag = SystemicExaminationsFragment.TAG
        )

        replaceFragmentOrCreateNewFragment<HivGeneralAndSystemicExaminationFragment>(
            R.id.systemicExaminationsContainer,
            bundle = hivBundle,
            tag = HivGeneralAndSystemicExaminationFragment.TAG
        )

        var bundle = Bundle().apply {
            putBoolean(DefinedParams.EMTCTMR, true)
        }
        addOrReuseFragment(
            R.id.emtctStatusContainer,
            HIVStatusFragment.TAG,
            HIVStatusFragment.newInstance(),
            bundle
        )


        replaceFragmentOrCreateNewFragment<ARTRegimenFragment>(
            R.id.aRTRegimenResultContainer,
            bundle = Bundle().apply {
                putString(DefinedParams.PatientId, intent.getStringExtra(DefinedParams.PatientId))
                putString(DefinedParams.ID, intent.getStringExtra(DefinedParams.ID))
            },
            tag = ARTRegimenFragment.TAG
        )
        replaceFragmentOrCreateNewFragment<ViralLoadFragment>(
            R.id.viralLoadResultContainer,
            bundle = Bundle().apply {
                putBoolean(DefinedParams.VIRAL_LOAD, true)
                putString(DefinedParams.PatientReference, intent.getStringExtra(DefinedParams.ID))
                putString(
                    DefinedParams.MemberReference,
                    intent.getStringExtra(DefinedParams.MemberID)
                )
            },
            tag = ViralLoadFragment.TAG
        )
        replaceFragmentOrCreateNewFragment<ClinicalNotesFragment>(
            binding.clinicalNotesContainer.id,
            bundle = null,
            tag = ClinicalNotesFragment.TAG
        )
    }
    private fun setButtonClickListener() {
        binding.btnSubmit.safeClickListener(this)
        binding.btnDone.safeClickListener(this)
        binding.btnRefer.safeClickListener(this)
        binding.ivPrescription.safeClickListener(this)
        binding.ivInvestigation.safeClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.ivPrescription.id -> withNetworkAvailability(online = {
                openPrescriptionActivity()
            })

            binding.ivInvestigation.id -> withNetworkAvailability(online = {
                openInvestigationActivity()
            })

            binding.loadingProgress.id -> {}
            binding.btnSubmit.id -> withLocationCheck(::clickSubmit)
            binding.btnDone.id -> withLocationCheck(::createSummary)
            binding.btnRefer.id -> showReferPatientDialog()
        }
    }
    private fun showReferPatientDialog() {
        withNetworkAvailability(online = {
            viewModel.hivCreateResponse.value?.data?.let {
                showDialogIfNotPresent(
                    ReferPatientFragment.TAG
                ) {
                    ReferPatientFragment.newInstance(
                        MedicalReviewTypeEnums.HIV.name,
                        it.patientReference,
                        it.encounterId
                    )
                }
            }
        })
    }
    private fun createSummary() {
        val fragment = supportFragmentManager.findFragmentById(R.id.hivSummary)
        if (fragment is HivImrCmrSummaryFragment) {
            val isValid = (fragment as? HivImrCmrSummaryFragment)?.validateInput()
            if (isValid == true) {
                val submitCreateId = viewModel.getSubmitCreateId()
                val nextVisitDate = DateUtils.convertDateTimeToDate(
                    summaryViewModel.nextFollowupDate,
                    DateUtils.DATE_ddMMyyyy,
                    DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                    inUTC = true
                )
                if (connectivityManager.isNetworkAvailable()) {
                    viewModel.createHivSummary(
                        MedicalReviewTypeEnums.HIV.name,
                        patientViewModel.getPatientMemberId(),
                        submitCreateId,
                        patientViewModel.getPatientHouseholdId(),
                        viewModel.getPatientReference(),
                        nextVisitDate,
                        summaryViewModel.patientStatus,
                        patientViewModel.getVillageId(),
                        patientViewModel.getPatientId(),
                        DefinedParams.EMTCT_HIV_MEDICAL_REVIEW,
                        summaryViewModel.eMTCTStatus,
                        summaryViewModel.maternalOutcome
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

    private fun clickSubmit() {
        if (patientViewModel.getHivMedicalReviewStatus()) {
            callViralLoadTestRecommendation()
        } else {
            submitEmtctRequest()
        }
    }

    private fun callViralLoadTestRecommendation() {
        viewModel.checkRecommendationRInvestigations(patientReference = patientViewModel.getPatientId())
    }

    private fun submitEmtctRequest() {
        val request = createMedicalReviewRequest()
        withNetworkAvailability(online = {
            viewModel.hivCreate(request = request)
        })
    }

    private fun createMedicalReviewRequest(): HivRequestData = HivRequestData(
        clinicalStage = hivViewModel.whovalue,
        cd4 = hivViewModel.cd4Value,
        artCode = patientViewModel.artCode,
        weight = weightViewModel.getWeight(),
        hivStatus = (supportFragmentManager.findFragmentByTag(HIVStatusFragment.TAG) as? HIVStatusFragment)?.getRequest(),
        presentingComplaints = presentingComplaintsViewModel.selectedPresentingComplaints.map { it.value },
        presentingComplaintsNotes = presentingComplaintsViewModel.enteredComplaintNotes.takeIf { it.isNotBlank() },
        systemicExaminations = hivGeneralAndSystemicExaminationViewModel.resultHashMap,
        encounter = createMedicalReviewEncounter(
            encounterId = patientViewModel.encounterId,
            patientHouseholdId = patientViewModel.getPatientHouseholdId(),
            memberId = patientViewModel.getPatientMemberId()
        ),
        medicalReviewType = DefinedParams.EMTCT_HIV_MEDICAL_REVIEW,
        clinicalNotes = clinicalNotesViewModel.enteredClinicalNotes,
        id = patientViewModel.encounterId,
        emtctVisitStatus = hivViewModel.emtctVisitStatus,
        obstetricExaminations = systemicExaminationViewModel.selectedSystemicExaminations.map { it.value } ,
        obstetricExaminationNotes = systemicExaminationViewModel.enteredExaminationNotes,
        fundalHeight = systemicExaminationViewModel.fundalHeight.takeIfNotNull(),
        fetalHeartRate = systemicExaminationViewModel.fetalHeartRate.takeIfNotNull()
    )

    private fun createMedicalReviewEncounter(
        encounterId: String?,
        patientHouseholdId: String?,
        memberId: String?
    ): MedicalReviewEncounter = MedicalReviewEncounter(
        id = encounterId,
        patientId = hivViewModel.patientId,
        provenance = ProvanceDto(),
        memberId = memberId,
        latitude = hivViewModel.lastLocation?.latitude ?: 0.0,
        longitude = hivViewModel.lastLocation?.longitude ?: 0.0,
        startTime = DateUtils.getCurrentDateAndTime(DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ),
        endTime = DateUtils.getCurrentDateAndTime(DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ),
        householdId = patientHouseholdId,
        visitNumber = hivViewModel.ancVisit,
        referred = true
    )

    private fun openPrescriptionActivity() {
        patientViewModel.patientDetailsLiveData.value?.data?.let { data ->
            val intent = Intent(this, PrescriptionActivity::class.java)
            intent.putExtra(DefinedParams.PatientId, data.patientId)
            intent.putExtra(DefinedParams.EncounterId, patientViewModel.encounterId)
            getResult.launch(intent)
        }
    }

    private fun openInvestigationActivity() {
        patientViewModel.patientDetailsLiveData.value?.data?.let { data ->
            val intent = Intent(this, InvestigationActivity::class.java)
            intent.putExtra(DefinedParams.PatientId, data.patientId)
            intent.putExtra(DefinedParams.EncounterId, patientViewModel.encounterId)
            getResult.launch(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        getCurrentLocation()
        setupSwipeRefresh()
    }

    private fun getCurrentLocation() {
        val locationManager = SpiceLocationManager(this)
        locationManager.getCurrentLocation {
            hivViewModel.lastLocation = it
        }
    }


    private fun showSummary(callBack: () -> Unit) {
        scrollUp()
        with(binding) {
            patientBMIContainer.gone()
            pregnancySummaryContainer.gone()
            presentingComplaintsContainer.gone()
            obstetricExaminationContainer.gone()
            systemicExaminationsContainer.gone()
            aRTRegimenResultContainer.gone()
            viralLoadResultContainer.gone()
            clinicalNotesContainer.gone()
            emtctStatusContainer.gone()
            binding.bottomNavigationView.gone()
            binding.referralBottomView.visible()
            binding.btnDone.isEnabled = true
            binding.btnRefer.isEnabled = true
            patientViewModel.isSummary = true
            hivViewModel.isHivSummary = true

            initializePatientDetailFragment()
            replaceFragment(
                R.id.hivSummary,
                HivImrCmrSummaryFragment.TAG,
                HivImrCmrSummaryFragment.newInstance(
                    encounterId = viewModel.hivCreateResponse.value?.data?.encounterId,
                    fhirId = patientViewModel.getPatientFHIRId(),
                    isEMTCTMR = true

                )
            )
        }
        callBack.invoke()
    }
    fun enableRefer(isEnable: Boolean) {
//        binding.btnRefer.isEnabled = isEnable
    }
    override fun onDialogDismissListener(isFinish: Boolean) {
        startActivityWithoutSplashScreen()
    }
    private fun onBackPressPopStack() {
        this.finish()
    }
    private fun scrollUp() {
        binding.nestedScrollViewID.post {
            binding.nestedScrollViewID.fullScroll(View.FOCUS_UP)
        }
    }
}
