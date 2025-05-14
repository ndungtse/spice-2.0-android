package com.medtroniclabs.spice.ui.medicalreview.hiv.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.databinding.ActivityTbMedicalReviewBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.medicalreview.PresentingComplaintsFragment
import com.medtroniclabs.spice.ui.medicalreview.hiv.fragment.HIVStatusFragment
import com.medtroniclabs.spice.ui.medicalreview.hiv.fragment.HivGeneralAndSystemicExaminationFragment
import com.medtroniclabs.spice.ui.medicalreview.hiv.fragment.OpportunisticInfectionsTreatmentFragment
import com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel.HivImrAndCmrViewModel
import com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel.HivViewModel
import com.medtroniclabs.spice.ui.medicalreview.investigation.InvestigationActivity
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.AncVisitCallBack
import com.medtroniclabs.spice.ui.medicalreview.prescription.PrescriptionActivity
import com.medtroniclabs.spice.ui.medicalreview.tb.fragment.ComorbiditiesFragment
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.mypatients.fragment.MedicalReviewPatientDiagnosisFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientInfoFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HivImrAndCmrActivity : BaseActivity(), View.OnClickListener, AncVisitCallBack {
    private val patientViewModel: PatientDetailViewModel by viewModels()
    private val viewModel: HivImrAndCmrViewModel by viewModels()
    private lateinit var binding: ActivityTbMedicalReviewBinding
    private val hivViewModel: HivViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        binding = ActivityTbMedicalReviewBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.patient_medical_review),
            homeAndBackVisibility = Pair(true, true),
            callback = {
                showErrorDialog()
            },
            callbackHome = {
                showErrorDialog(true)
            }
        )
        binding.refreshLayout.setOnRefreshListener {
            swipeRefresh()
        }
        viewModel.patientId = intent.getStringExtra(DefinedParams.PatientId)
        initStaticDataCall()
        setButtonClickListener()
        attachObserver()
        patientViewModel.setUserJourney(AnalyticsDefinedParams.TBMEDICALREVIEW)
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
            initView()
        }
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
                            onBackPressPopStack()
                        }
                    }
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    initView()
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

    private fun setButtonClickListener() {
        binding.btnSubmit.safeClickListener(this)
        binding.btnDone.safeClickListener(this)
        binding.btnRefer.safeClickListener(this)
        binding.ivPrescription.safeClickListener(this)
        binding.ivInvestigation.safeClickListener(this)
    }

    private fun initView() {
        initializePatientDetailFragment()
    }

    private fun initializePatientDetailFragment() {
        replaceFragment(
            R.id.patientDetailFragment,
            PatientInfoFragment.TAG,
            PatientInfoFragment.newInstance(
                intent.getStringExtra(DefinedParams.PatientId),
                isHivImrCmr = true
            ).apply {
                setDataCallback(this@HivImrAndCmrActivity)
            }
        )
    }

    private fun swipeRefresh() {
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

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showErrorDialog()
            }
        }

    private fun showErrorDialog(isHome: Boolean = false) {
        showErrorDialogue(
            getString(R.string.alert),
            getString(R.string.exit_reason),
            isNegativeButtonNeed = true
        ) { isPositive ->
            if (isPositive) {
                if (isHome) {
                    startActivityWithoutSplashScreen()
                } else {
                    onBackPressPopStack()
                }
            }
        }
    }

    private fun onBackPressPopStack() {
        this@HivImrAndCmrActivity.finish()
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
        }
    }

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

    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                val value = it.data?.getStringExtra(DefinedParams.EncounterId)
                value?.let { valueString ->
                    patientViewModel.encounterId = valueString
                }
            }
        }

    override fun onDataLoaded(details: PatientListRespModel) {
        viewModel.memberId = details.memberId
        replaceWithDiagnosisFragment()
    }

    private fun replaceWithDiagnosisFragment() {
        replaceFragment(
            R.id.patientSummaryContainer,
            MedicalReviewPatientDiagnosisFragment.TAG,
            MedicalReviewPatientDiagnosisFragment.newInstance(
                isAnc = false,
                isPnc = false,
                isTB = false,
                patientId = intent.getStringExtra(DefinedParams.PatientId),
                memberID = viewModel.memberId,
                id = intent.getStringExtra(DefinedParams.ID),
                isHivImrCmr = true
            )
        )
        addOrReuseFragment(
            R.id.patientMedicalReviewDiagnosis,
            HIVStatusFragment.TAG,
            HIVStatusFragment.newInstance()
        )
        val bundle = Bundle().apply {
            putString(
                MedicalReviewTypeEnums.PresentingComplaints.name,
                MedicalReviewTypeEnums.HIV.name
            )
            putString(
                MedicalReviewTypeEnums.SystemicExaminations.name,
                MedicalReviewTypeEnums.HIV.name
            )
        }
        replaceFragmentInId<PresentingComplaintsFragment>(
            binding.presentingComplaintsContainer.id,
            bundle = bundle,
            tag = PresentingComplaintsFragment::class.simpleName
        )
        addOrReuseFragment(
            R.id.comorbiditiesContainer,
            ComorbiditiesFragment.TAG,
            ComorbiditiesFragment.newInstance(MedicalReviewTypeEnums.HIV.name)
        )
        binding.systemicExaminationsContainer.visible()
        binding.clinicalNotesContainer.visible()
        addOrReuseFragment(
            R.id.systemicExaminationsContainer,
            OpportunisticInfectionsTreatmentFragment.TAG,
            OpportunisticInfectionsTreatmentFragment.newInstance()
        )
        replaceFragmentOrCreateNewFragment<HivGeneralAndSystemicExaminationFragment>(
            binding.clinicalNotesContainer.id,
            bundle = bundle,
            tag = HivGeneralAndSystemicExaminationFragment.TAG
        )
    }
}