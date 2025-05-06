package com.medtroniclabs.spice.ui.medicalreview.hiv.activity

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.SpiceLocationManager
import com.medtroniclabs.spice.databinding.ActivityHivMedicalReviewBaseBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.medicalreview.hiv.fragment.EligibilityFragment
import com.medtroniclabs.spice.ui.medicalreview.hiv.fragment.HivSummaryFragment
import com.medtroniclabs.spice.ui.medicalreview.hiv.fragment.HivTestFragment
import com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel.HivViewModel
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.AncVisitCallBack
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.A1_TEST_RESULT
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.A2_TEST_RESULT
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.A3_TEST_RESULT
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.HIV_TEST_ITEM
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.HaveYouTakenHivTestBefore
import com.medtroniclabs.spice.ui.mypatients.fragment.MedicalReviewPatientDiagnosisFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientInfoFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HivMedicalReviewBaseActivity : BaseActivity(), AncVisitCallBack, View.OnClickListener {
    private lateinit var binding: ActivityHivMedicalReviewBaseBinding
    private val patientViewModel: PatientDetailViewModel by viewModels()
    private val hivViewModel: HivViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        binding = ActivityHivMedicalReviewBaseBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            homeAndBackVisibility = Pair(true, true),
            callback = {
                backNavigation()
            },
            callbackHome = {
                backNavigationToHome()
            }
        )

        binding.refreshLayout.setOnRefreshListener {
            swipeRefresh()
        }
        setButtonClickListener()
        hivViewModel.patientId = intent.getStringExtra(DefinedParams.PatientId)
        initStaticDataCall()
        attachObserver()
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
                    hideLoading()
                }
            }
        }

        hivViewModel.createHivScreeningLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState.data.let { hivScreeningDetails ->
                        if (hivScreeningDetails != null) {
                            hivViewModel.getHivScreeningDetails(hivScreeningDetails)
                        }
                    }
                }

                ResourceState.ERROR -> {
                    hideLoading()
                    showErrorDialogue(
                        title = getString(R.string.alert),
                        message = getString(R.string.something_went_wrong_try_later),
                        positiveButtonName = getString(R.string.ok),
                    ) {}
                }
            }
        }

        hivViewModel.hivScreeningDetailsLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    replaceFragmentOrCreateNewFragment<HivSummaryFragment>(
                        binding.hivSummary.id,
                        bundle = null,
                        tag = HivSummaryFragment.TAG
                    )
                }

                ResourceState.ERROR -> {
                    hideLoading()
                    showErrorDialogue(
                        title = getString(R.string.alert),
                        message = getString(R.string.something_went_wrong_try_later),
                        positiveButtonName = getString(R.string.ok),
                    ) {}
                }
            }
        }
    }

    private fun setButtonClickListener() {
        binding.btnSubmit.safeClickListener(this)
        binding.btnDone.safeClickListener(this)
        binding.btnRefer.safeClickListener(this)
        binding.ivPrescription.safeClickListener(this)
        binding.ivInvestigation.safeClickListener(this)
    }

    private fun initStaticDataCall() {
        if (!(SecuredPreference.getBoolean(SecuredPreference.EnvironmentKey.IS_HIV_DATA_LOADED.name))) {
            if (connectivityManager.isNetworkAvailable()) {
                hivViewModel.getHivMetaData()
                initializePatientDetailFragment()
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
        replaceFragment(
            R.id.patientDetailFragment,
            PatientInfoFragment.TAG,
            PatientInfoFragment.newInstanceForHIV(
                intent.getStringExtra(DefinedParams.PatientId),
                isHiv = true,
                isHivSummary = hivViewModel.isHivSummary
            ).apply {
                setDataCallback(this@HivMedicalReviewBaseActivity)
            }
        )
    }

    private fun swipeRefresh() {
        if (connectivityManager.isNetworkAvailable()) {
            supportFragmentManager.findFragmentById(R.id.patientDetailFragment)
                .let {
                    patientViewModel.getPatientId()?.let { id ->
                        patientViewModel.getPatients(id)
                    }
                }
        } else {
            showErrorDialogue(
                getString(R.string.error), getString(R.string.no_internet_error),
                isNegativeButtonNeed = false,
            ) {
                if (binding.refreshLayout.isRefreshing) {
                    binding.refreshLayout.isRefreshing = false
                    startActivityWithoutSplashScreen()
                }
            }
        }
    }

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backNavigation()
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

    private fun backNavigation() {
        showErrorDialogue(
            getString(R.string.alert),
            getString(R.string.exit_reason),
            isNegativeButtonNeed = true
        ) { isPositive ->
            if (isPositive) {
                hivViewModel.isHivSummary = false
                onBackPressPopStack()
            }
        }
    }

    private fun onBackPressPopStack() {
        this@HivMedicalReviewBaseActivity.finish()
    }

    private fun enableSubmitBtn() {
        binding.btnSubmit.isEnabled = (
                hivViewModel.selectedHistoryListItem.size > 0 ||
                        hivViewModel.selectedPopulationType.size > 0 ||
                        hivViewModel.selectedEntryPoint.isNullOrEmpty() ||
                        hivViewModel.resultHashMap.size > 0 ||
                        (hivViewModel.resultHashMap[HaveYouTakenHivTestBefore] != null &&
                                !hivViewModel.selectedLastTestForHIV.isNullOrEmpty()))
    }

    override fun onDataLoaded(details: PatientListRespModel) {
        hivViewModel.memberId = details.memberId
        initializeFragments()
    }

    private fun initializeFragments() {
        supportFragmentManager
            .setFragmentResultListener(HIV_TEST_ITEM, this) { _, _ ->
                enableSubmitBtn()
            }
        with(binding) {
            ivPrescription.invisible()
            ivInvestigation.invisible()
            patientDetailFragment.visible()
            patientBMIContainer.visible()
            patientEligibility.visible()
        }
        addOrReuseFragment(
            R.id.patientBMIContainer,
            MedicalReviewPatientDiagnosisFragment.TAG,
            MedicalReviewPatientDiagnosisFragment.newInstance(
                isAnc = false,
                isPnc = false,
                isTB = true,
                patientId = intent.getStringExtra(DefinedParams.PatientId),
                memberID = hivViewModel.memberId,
                id = intent.getStringExtra(DefinedParams.ID)
            )
        )
        replaceFragmentOrCreateNewFragment<EligibilityFragment>(
            binding.patientEligibility.id,
            bundle = null,
            tag = EligibilityFragment.TAG
        )
        replaceFragmentOrCreateNewFragment<HivTestFragment>(
            binding.patientHIVTest.id,
            bundle = null,
            tag = HivTestFragment.TAG
        )
    }

    override fun onResume() {
        super.onResume()
        getCurrentLocation()
    }

    private fun getCurrentLocation() {
        val locationManager = SpiceLocationManager(this)
        locationManager.getCurrentLocation {
            hivViewModel.lastLocation = it
        }
    }

    private fun postResultInput() {
        patientViewModel.patientDetailsLiveData.value?.data?.let { details ->
            details.patientId?.let { id ->
                if (connectivityManager.isNetworkAvailable()) {
                    val haveYouTakenTestBefore =
                        hivViewModel.resultHashMap[HaveYouTakenHivTestBefore] as? String
                    if (haveYouTakenTestBefore != null) {
                        hivViewModel.createHivRequestModel(
                            patientListRespModel = details,
                            selectedEligibilityPair = Pair(
                                hivViewModel.selectedHistoryListItem.map { it.value },
                                hivViewModel.selectedPopulationType.map { it.value }
                            ),
                            haveHivTestTestedBeforePair = Pair(
                                haveYouTakenTestBefore,
                                hivViewModel.selectedLastTestForHIV
                            ),
                            hivTestResult = Triple(
                                hivViewModel.resultHashMap[A1_TEST_RESULT] as? String ?: "",
                                hivViewModel.resultHashMap[A2_TEST_RESULT] as? String ?: "",
                                hivViewModel.resultHashMap[A3_TEST_RESULT] as? String ?: "",
                            ),
                            entryPoint = hivViewModel.selectedEntryPoint
                        )
                    }
                } else {
                    showErrorDialogue(
                        getString(R.string.error), getString(R.string.no_internet_error),
                        isNegativeButtonNeed = false,
                    ) {}
                }
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnSubmit -> {
                if (validation()) {
                    postResultInput()
                }
            }
        }
    }

    private fun validation(): Boolean {
        val eligibilityFragment =
            supportFragmentManager.findFragmentById(R.id.patientEligibility) as? EligibilityFragment
        val isValidEligibilityFragment = eligibilityFragment?.validation()
        val hivTestFragment =
            supportFragmentManager.findFragmentById(R.id.patientHIVTest) as? HivTestFragment
        val isHivTestFragment = hivTestFragment?.validation()
        return (isValidEligibilityFragment == true && isHivTestFragment == true)
    }
}



