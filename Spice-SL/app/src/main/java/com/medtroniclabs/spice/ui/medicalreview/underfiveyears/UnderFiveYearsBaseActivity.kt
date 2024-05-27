package com.medtroniclabs.spice.ui.medicalreview.underfiveyears

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.databinding.ActivityUnderFiveYearsBaseBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.medicalreview.ClinicalNotesFragment
import com.medtroniclabs.spice.ui.medicalreview.PresentingComplaintsFragment
import com.medtroniclabs.spice.ui.medicalreview.SystemicExaminationsFragment
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.ClinicalNotesViewModel
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.PresentingComplaintsViewModel
import com.medtroniclabs.spice.ui.medicalreview.examinations.ExaminationCardFragment
import com.medtroniclabs.spice.ui.medicalreview.examinations.ExaminationCardViewModel
import com.medtroniclabs.spice.ui.medicalreview.undertwomonths.ClinicalSummaryFragment
import com.medtroniclabs.spice.ui.medicalreview.undertwomonths.ClinicalSummaryViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.mypatients.fragment.MedicalReviewPatientDiagnosisFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientInfoFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UnderFiveYearsBaseActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityUnderFiveYearsBaseBinding
    private val viewModel: UnderFiveYearsViewModel by viewModels()
    private val clinicalNotesViewModel: ClinicalNotesViewModel by viewModels()
    private val presentingComplaintsViewModel: PresentingComplaintsViewModel by viewModels()
    private val clinicalSummaryViewModel: ClinicalSummaryViewModel by viewModels()
    private val examinationCardViewModel: ExaminationCardViewModel by viewModels()
    private val patientDetailViewModel: PatientDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnderFiveYearsBaseBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.patient_medical_review)
        )
        initializeViews()
        attachObserver()
        initializeListeners()
        viewModel.patientId = intent.getStringExtra(DefinedParams.PatientId)
    }

    private fun initializeListeners() {
        binding.btnSubmit.safeClickListener(this)
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
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    initializeFragments()
                }
            }
        }
    }

    private fun initializeViews() {
        examinationCardViewModel.workFlowType = MedicalReviewTypeEnums.UnderFiveYears.name
        if (!(SecuredPreference.getBoolean(SecuredPreference.EnvironmentKey.IS_UNDER_FIVE_YEARS_LOADED.name))) {
            viewModel.getStaticMetaData()
        } else {
            initializeFragments()
        }
        supportFragmentManager
            .setFragmentResultListener(MedicalReviewDefinedParams.SE_ITEM, this) { _, _ ->

            }
    }

    private fun initializeFragments() {
        supportFragmentManager.beginTransaction()
            .add(
                R.id.patientDetailFragment,
                PatientInfoFragment.newInstance(intent.getStringExtra(DefinedParams.PatientId))
            ).commit()
        binding.patientDiagnosisContainer.visibility = View.VISIBLE
        val bundle = Bundle().apply {
            putString(
                MedicalReviewTypeEnums.PresentingComplaints.name,
                MedicalReviewTypeEnums.UnderFiveYears.name
            )
            putString(
                MedicalReviewTypeEnums.SystemicExaminations.name,
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
            binding.clinicalNotesContainer.id,
            tag = ClinicalNotesFragment::class.simpleName
        )
    }

    override fun onClick(view: View) {

    }

    private fun removeFragment(clinicalSummaryContainer: Int) {
        supportFragmentManager.findFragmentById(clinicalSummaryContainer)?.let {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }
    }

}