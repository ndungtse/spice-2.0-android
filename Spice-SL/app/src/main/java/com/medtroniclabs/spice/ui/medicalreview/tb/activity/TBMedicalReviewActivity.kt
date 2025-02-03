package com.medtroniclabs.spice.ui.medicalreview.tb.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.ActivityTbMedicalReviewBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.medicalreview.ClinicalNotesFragment
import com.medtroniclabs.spice.ui.medicalreview.PresentingComplaintsFragment
import com.medtroniclabs.spice.ui.medicalreview.SystemicExaminationsFragment
import com.medtroniclabs.spice.ui.medicalreview.investigation.InvestigationActivity
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.AncVisitCallBack
import com.medtroniclabs.spice.ui.medicalreview.prescription.PrescriptionActivity
import com.medtroniclabs.spice.ui.medicalreview.tb.fragment.ComorbiditiesFragment
import com.medtroniclabs.spice.ui.medicalreview.tb.fragment.PresumptiveTreatmentAndHistoryFragment
import com.medtroniclabs.spice.ui.medicalreview.tb.fragment.TbSummaryFragment
import com.medtroniclabs.spice.ui.medicalreview.tb.viewmodel.TbViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.mypatients.fragment.MedicalReviewPatientDiagnosisFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientInfoFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TBMedicalReviewActivity : BaseActivity(), View.OnClickListener, AncVisitCallBack {

    private lateinit var binding: ActivityTbMedicalReviewBinding
    private val patientViewModel: PatientDetailViewModel by viewModels()
    private val viewModel: TbViewModel by viewModels()
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
                finish()
            },
            callbackHome = {
                startActivityWithoutSplashScreen()
            }
        )
        binding.refreshLayout.setOnRefreshListener {
            swipeRefresh()
        }
        viewModel.patientId = intent.getStringExtra(DefinedParams.PatientId)
        initStaticDataCall()
        setButtonClickListener()
    }

    private fun initStaticDataCall() {
        if (false) {
            viewModel.getTbStaticData()
        } else {
            initView()
        }
    }

    private fun initView() {
        initializePatientDetailFragment()
    }

    private fun initializePatientDetailFragment() {
        val fragmentManager = supportFragmentManager
        val existingFragment = fragmentManager.findFragmentById(R.id.patientDetailFragment)
        if (existingFragment == null) {
            val fragment =
                PatientInfoFragment.newInstance(
                    intent.getStringExtra(DefinedParams.PatientId),
                    isTb = true
                )
            fragment.setDataCallback(this)
            fragmentManager.beginTransaction()
                .add(R.id.patientDetailFragment, fragment)
                .commit()
        } else if (existingFragment !is PatientInfoFragment) {
            val fragment =
                PatientInfoFragment.newInstance(
                    intent.getStringExtra(DefinedParams.PatientId),
                    isTb = true
                )
            fragment.setDataCallback(this)
            fragmentManager.beginTransaction()
                .replace(R.id.patientDetailFragment, fragment)
                .commit()
        }
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
                finish()
            }
        }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.ivPrescription.id -> openPrescriptionActivity()
            binding.ivInvestigation.id -> openInvestigationActivity()
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

    private fun setButtonClickListener() {
        binding.btnSubmit.safeClickListener(this)
        binding.btnDone.safeClickListener(this)
        binding.btnRefer.safeClickListener(this)
        binding.ivPrescription.safeClickListener(this)
        binding.ivInvestigation.safeClickListener(this)
    }

    private fun replaceWithDiagnosisFragment() {
        supportFragmentManager.beginTransaction().replace(
            R.id.patientMedicalReviewDiagnosis,
            MedicalReviewPatientDiagnosisFragment.newInstance(
                isAnc = false,
                isPnc = false,
                isTB = true,
                patientId = intent.getStringExtra(DefinedParams.PatientId),
                memberID = viewModel.memberId,
                id = intent.getStringExtra(DefinedParams.ID)
            )
        ).commit()
        supportFragmentManager.beginTransaction().replace(
            R.id.patientHistoryContainer,
            PresumptiveTreatmentAndHistoryFragment.newInstance()
        ).commit()
        supportFragmentManager.beginTransaction().replace(
            R.id.patientSummaryContainer,
            TbSummaryFragment.newInstance()
        ).commit()
        supportFragmentManager.beginTransaction().replace(
            R.id.comorbiditiesContainer,
            ComorbiditiesFragment.newInstance()
        ).commit()
        initializeFragments()
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
                MedicalReviewTypeEnums.TB.name
            )
            putString(
                MedicalReviewTypeEnums.SystemicExaminations.name,
                MedicalReviewTypeEnums.TB.name
            )
        }
        replaceFragmentOrCreateNewFragment<PresentingComplaintsFragment>(
            binding.presentingComplaintsContainer.id,
            bundle = bundle,
            tag = PresentingComplaintsFragment.TAG
        )
        replaceFragmentOrCreateNewFragment<SystemicExaminationsFragment>(
            binding.systemicExaminationsContainer.id,
            bundle = bundle,
            tag = SystemicExaminationsFragment.TAG
        )
        replaceFragmentOrCreateNewFragment<ClinicalNotesFragment>(
            binding.clinicalNotesContainer.id,
            bundle = bundle,
            tag = ClinicalNotesFragment.TAG
        )
    }
}