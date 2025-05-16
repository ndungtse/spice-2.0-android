package com.medtroniclabs.spice.ui.medicalreview.motherneonate.emtct.activity

import android.app.Activity
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.databinding.ActivityMotherNeonateEmtctctivityBinding
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.medicalreview.PresentingComplaintsFragment
import com.medtroniclabs.spice.ui.medicalreview.SystemicExaminationsFragment
import com.medtroniclabs.spice.ui.medicalreview.hiv.fragment.*
import com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel.HivViewModel
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.AncVisitCallBack
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.fragment.PregnancySummaryFragment
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientInfoFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MotherNeonateEMTCTActivity : BaseActivity(), AncVisitCallBack {

    private lateinit var binding: ActivityMotherNeonateEmtctctivityBinding
    private val hivViewModel: HivViewModel by viewModels()
    private val patientViewModel: PatientDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMotherNeonateEmtctctivityBinding.inflate(layoutInflater)
        setupView()
        initStaticDataCall()
        attachObserver()
        initEmtctFragments()
        setupSwipeRefresh()
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
        if (SecuredPreference.getBoolean(SecuredPreference.EnvironmentKey.IS_HIV_DATA_LOADED.name)) {
            initializePatientDetailFragment()
        } else if (connectivityManager.isNetworkAvailable()) {
            hivViewModel.getHivMetaData()
        } else {
            showErrorDialogue(
                getString(R.string.error),
                getString(R.string.no_internet_error),
                isNegativeButtonNeed = false
            ) {}
        }
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
    }

    override fun onDataLoaded(details: PatientListRespModel) {
        hivViewModel.memberId = details.memberId
        initializeReviewFragments()
    }

    private fun initializeReviewFragments() {
        setupResultListeners()
        binding.patientDetailFragment.visible()
        binding.patientBMIContainer.visible()

        addOrReuseFragment(
            R.id.patientBMIContainer,
            HivMedicalReviewDiagnosesFragment.TAG,
            HivMedicalReviewDiagnosesFragment.newInstance(),
            Bundle().apply {
                putString(DefinedParams.PatientId, intent.getStringExtra(DefinedParams.PatientId))
                putString(DefinedParams.ID, intent.getStringExtra(DefinedParams.ID))
                putString(DefinedParams.MemberID, hivViewModel.memberId)
                putBoolean(MedicalReviewTypeEnums.HIV.name, true)
            }
        )
    }

    private fun setupSwipeRefresh() {
        binding.refreshLayout.setOnRefreshListener {
            if (!connectivityManager.isNetworkAvailable()) {
                showErrorDialogue(
                    getString(R.string.error),
                    getString(R.string.no_internet_error),
                    isNegativeButtonNeed = false
                ) {
                    binding.refreshLayout.isRefreshing = false
                    startActivityWithoutSplashScreen()
                }
            } else {
                if (hivViewModel.isHivSummary) {
                    showReviewSummary(hivViewModel.encounterId, hivViewModel.patientReference)
                } else {
                    patientViewModel.getPatientId()?.let {
                        patientViewModel.getPatients(it)
                    }
                }
                binding.refreshLayout.isRefreshing = false
            }
        }
    }

    private fun enableSubmitBtn() {
        binding.btnSubmit.isEnabled = with(hivViewModel) {
            selectedHistoryListItem.isNotEmpty() ||
                    selectedPopulationType.isNotEmpty() ||
                    selectedEntryPoint != null ||
                    resultHashMap.isNotEmpty() ||
                    resultHashMap[MedicalReviewDefinedParams.HaveYouTakenHivTestBefore] != null
        }
    }

    private fun setupResultListeners() {
        val listener: (String, Bundle) -> Unit = { _, _ -> enableSubmitBtn() }
        supportFragmentManager.setFragmentResultListener(MedicalReviewDefinedParams.HIV_TEST_ITEM, this, listener)
        supportFragmentManager.setFragmentResultListener(MedicalReviewDefinedParams.HIV_ELIGIBILITY_ITEM, this, listener)
    }

    private fun initEmtctFragments() {
        addOrReuseFragment(R.id.pregnancySummaryContainer,PregnancySummaryFragment.TAG,PregnancySummaryFragment.newInstance())

        val hivBundle = Bundle().apply {
            putString(MedicalReviewTypeEnums.PresentingComplaints.name, MedicalReviewTypeEnums.HIV.name)
            putString(MedicalReviewTypeEnums.SystemicExaminations.name, MedicalReviewTypeEnums.HIV.name)
        }

        replaceFragmentOrCreateNewFragment<PresentingComplaintsFragment>(
            R.id.presentingComplaintsContainer, bundle = hivBundle, tag = PresentingComplaintsFragment.TAG
        )

        replaceFragmentOrCreateNewFragment<SystemicExaminationsFragment>(
            R.id.obstetricExaminationContainer, bundle = hivBundle, tag = SystemicExaminationsFragment.TAG
        )

        replaceFragmentOrCreateNewFragment<SystemicExaminationsFragment>(
            R.id.systemicExaminationsContainer, bundle = hivBundle, tag = SystemicExaminationsFragment.TAG
        )

        addOrReuseFragment(R.id.emtctStatusContainer, HIVStatusFragment.TAG, HIVStatusFragment.newInstance())

        replaceFragmentOrCreateNewFragment<ViralLoadFragment>(
            R.id.viralLoadResultContainer,
            bundle = Bundle().apply { putBoolean(DefinedParams.VIRAL_LOAD, true) },
            tag = ViralLoadFragment.TAG
        )

        replaceFragmentOrCreateNewFragment<ViralLoadFragment>(
            R.id.aRTRegimenResultContainer,
            bundle = Bundle().apply { putBoolean(DefinedParams.VIRAL_LOAD, false) },
            tag = ViralLoadFragment.TAG_ART
        )
    }

    private fun showReviewSummary(encounterId: String?, patientReference: String?) {
        listOf(
            R.id.emtctStatusContainer,
            R.id.viralLoadResultContainer,
            R.id.aRTRegimenResultContainer,
            R.id.systemicExaminationsContainer,
            R.id.obstetricExaminationContainer,
            R.id.presentingComplaintsContainer,
            R.id.pregnancySummaryContainer,
            R.id.patientBMIContainer
        ).forEach { removeFragment(it) }

        replaceFragment(
            R.id.hivSummary,
            HivSummaryFragment.TAG,
            HivSummaryFragment.newInstance().apply {
                arguments = Bundle().apply {
                    putString(DefinedParams.EncounterId, encounterId)
                    putString(DefinedParams.PatientReference, patientReference)
                    putBoolean(DefinedParams.EMTCT, hivViewModel.isEMTCT)
                }
            }
        )
    }
    private fun removeFragment(hivCreateScreeningSummary: Int) {
        supportFragmentManager.findFragmentById(hivCreateScreeningSummary)?.let {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }
    }
}
