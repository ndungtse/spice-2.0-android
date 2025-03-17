package com.medtroniclabs.spice.ui.medicalreview.familyplan.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.app.analytics.model.UserDetail
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.ActivityFamilyPlanMedicalReviewBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.medicalreview.ClinicalNotesFragment
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.AboveFiveYearsTreatmentSummaryFragment
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.ClinicalNotesViewModel
import com.medtroniclabs.spice.ui.medicalreview.familyplan.fragment.ContraceptivesFragment
import com.medtroniclabs.spice.ui.medicalreview.familyplan.fragment.FamilyPlanTreatmentFragment
import com.medtroniclabs.spice.ui.medicalreview.familyplan.viewmodel.ContraceptivesViewModel
import com.medtroniclabs.spice.ui.medicalreview.familyplan.viewmodel.FamilyPlanViewModel
import com.medtroniclabs.spice.ui.medicalreview.investigation.InvestigationActivity
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.AncVisitCallBack
import com.medtroniclabs.spice.ui.medicalreview.prescription.PrescriptionActivity
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.CLINICAL_NOTES
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.CONTRACEPTIVES_ITEMS
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.ClientType
import com.medtroniclabs.spice.ui.mypatients.fragment.MedicalReviewPatientDiagnosisFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientInfoFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FamilyPlanMedicalReviewActivity : BaseActivity(), AncVisitCallBack, View.OnClickListener {
    private lateinit var binding: ActivityFamilyPlanMedicalReviewBinding
    private val patientViewModel: PatientDetailViewModel by viewModels()
    private val chipItemViewModel: ClinicalNotesViewModel by viewModels()
    private val contraceptivesViewModel: ContraceptivesViewModel by viewModels()
    private val viewModel: FamilyPlanViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        binding = ActivityFamilyPlanMedicalReviewBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.patient_medical_review),
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
        viewModel.patientId = intent.getStringExtra(DefinedParams.PatientId)
        initStaticDataCall()
        setButtonClickListener()
    }

    private fun setButtonClickListener() {
        binding.btnSubmit.safeClickListener(this)
        binding.btnDone.safeClickListener(this)
        binding.btnRefer.safeClickListener(this)
        binding.ivPrescription.safeClickListener(this)
        binding.ivInvestigation.safeClickListener(this)
    }

    private fun initStaticDataCall() {
        if (false) {
            viewModel.getFamilyPlanStaticData()
        } else {
            initView()
        }
    }

    private fun initView() {
        initializePatientDetailFragment()
        supportFragmentManager
            .setFragmentResultListener(CLINICAL_NOTES, this) { _, _ ->
                enableSubmitBtn()
            }

        supportFragmentManager
            .setFragmentResultListener(CONTRACEPTIVES_ITEMS, this) { _, _ ->
                enableSubmitBtn()
            }
    }

    private fun initializePatientDetailFragment() {
        replaceFragment(
            R.id.patientDetailFragment,
            PatientInfoFragment.TAG,
            PatientInfoFragment.newInstance(
                intent.getStringExtra(DefinedParams.PatientId),
                isFamilyPlan = true,
                isFPSummary = viewModel.isFamilyPlanSummary
            ).apply {
                setDataCallback(this@FamilyPlanMedicalReviewActivity)
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

    override fun onDataLoaded(details: PatientListRespModel) {
        viewModel.memberId = details.memberId
        initializeFragments()
    }

    private fun initializeFragments() {
        with(binding) {
            patientDetailFragment.visible()
            patientBMIContainer.visible()
            contraceptivesContainer.visible()
            familyPlanClinicalNotesContainer.visible()
        }

        addOrReuseFragment(
            R.id.patientBMIContainer,
            MedicalReviewPatientDiagnosisFragment.TAG,
            MedicalReviewPatientDiagnosisFragment.newInstance(
                isAnc = false,
                isPnc = false,
                isTB = true,
                patientId = intent.getStringExtra(DefinedParams.PatientId),
                memberID = viewModel.memberId,
                id = intent.getStringExtra(DefinedParams.ID)
            )
        )

        replaceFragmentOrCreateNewFragment<ContraceptivesFragment>(
            binding.contraceptivesContainer.id,
            bundle = null,
            tag = ContraceptivesFragment.TAG
        )


        replaceFragmentOrCreateNewFragment<ClinicalNotesFragment>(
            binding.familyPlanClinicalNotesContainer.id,
            bundle = null,
            tag = ClinicalNotesFragment.TAG
        )

    }

    override fun onClick(view: View?) {
        when(view?.id){
           binding.ivPrescription.id -> openPrescriptionActivity()
           binding.ivInvestigation.id-> openInvestigationActivity()
            binding.btnSubmit.id -> {
                viewModel.isFamilyPlanSummary = true
                validateInputRequest()
            }
        }
    }

    private fun validateInputRequest() {
        val conFragment = getFragmentById(
            supportFragmentManager,
            (R.id.contraceptivesContainer)
        ) as? ContraceptivesFragment

        conFragment?.let {
            if(it.validInputs()){
                initializeSummaryFragment()
            }
        }
    }

    private fun initializeSummaryFragment() {
        binding.apply {
            binding.patientBMIContainer.gone()
            binding.contraceptivesContainer.gone()
            familyPlanClinicalNotesContainer.gone()
            binding.bottomNavigationView.gone()
            binding.referralBottomView.visible()
            binding.familyPlanSummaryContainer.visible()
        }
        replaceFragment(
            R.id.patientDetailFragment,
            PatientInfoFragment.TAG,
            PatientInfoFragment.newInstance(
                intent.getStringExtra(DefinedParams.PatientId),
                isFamilyPlan = true,
                isFPSummary = viewModel.isFamilyPlanSummary
            )
        )

        replaceFragmentInId<FamilyPlanTreatmentFragment>(
           binding.familyPlanSummaryContainer.id,
            tag = FamilyPlanTreatmentFragment::class.simpleName
        )
        val bmiFragment = supportFragmentManager.findFragmentById(R.id.patientBMIContainer)
        val contraceptivesFragment = supportFragmentManager.findFragmentById(R.id.contraceptivesContainer)
        val clinicalNotesFragment = supportFragmentManager.findFragmentById(R.id.familyPlanClinicalNotesContainer)
        bmiFragment?.let {
            supportFragmentManager.beginTransaction().remove(bmiFragment).commit()
        }
        contraceptivesFragment?.let {
            supportFragmentManager.beginTransaction().remove(contraceptivesFragment).commit()
        }
        clinicalNotesFragment?.let {
            supportFragmentManager.beginTransaction().remove(clinicalNotesFragment).commit()
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

    private fun enableSubmitBtn() {
        binding.btnSubmit.isEnabled = (chipItemViewModel.enteredClinicalNotes.isNotBlank() ||
                contraceptivesViewModel.resultHashMap.size > 0)
    }

    private fun backNavigationToHome() {
        showErrorDialogue(
            getString(R.string.alert),
            getString(R.string.exit_reason),
            isNegativeButtonNeed = true
        ) { isPositive ->
            if (isPositive) {
                viewModel.isFamilyPlanSummary = false
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
                viewModel.isFamilyPlanSummary = false
                onBackPressPopStack()
            }
        }
    }

    private fun onBackPressPopStack() {
        this@FamilyPlanMedicalReviewActivity.finish()
    }

}