package com.medtroniclabs.spice.ui.medicalreview.abovefiveyears

import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.databinding.ActivityAboveFiveYearsBaseBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.medicalreview.ClinicalNotesFragment
import com.medtroniclabs.spice.ui.medicalreview.PresentingComplaintsFragment
import com.medtroniclabs.spice.ui.medicalreview.SystemicExaminationsFragment
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.CLINICAL_NOTES
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.PC_5YEARS
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.PC_ITEM
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.SE_ITEM
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.mypatients.fragment.MedicalReviewPatientDiagnosisFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.MedicalReviewTreatmentPlanSummary
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientInfoFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.ReferralTicketViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboveFiveYearsBaseActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityAboveFiveYearsBaseBinding
    private val viewModel: AboveFiveYearsViewModel by viewModels()
    private val patientViewModel: ReferralTicketViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboveFiveYearsBaseBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.patient_medical_review)
        )
        initializeViews()
        initializeFragments()
        initializeListeners()
        attachObserver()
    }

    private fun initializeListeners() {
        binding.btnSubmit.safeClickListener(this)
    }

    private fun initializeViews() {
        if (!(SecuredPreference.getBoolean(SecuredPreference.EnvironmentKey.IS_ABOVE_FIVE_YEARS_LOADED.name))) {
            viewModel.getStaticMetaData(MedicalReviewTypeEnums.AboveFiveYears.name)
            addPatientDetails()
        } else {
            addPatientDetails()
        }
        supportFragmentManager
            .setFragmentResultListener(PC_ITEM, this) { _, _ ->
                enableSubmitBtn()
            }
        supportFragmentManager
            .setFragmentResultListener(SE_ITEM, this) { _, _ ->
                enableSubmitBtn()
            }
        supportFragmentManager
            .setFragmentResultListener(CLINICAL_NOTES, this) { _, _ ->
                enableSubmitBtn()
            }
    }

    private fun addPatientDetails() {
        supportFragmentManager.beginTransaction()
            .add(
                R.id.patientDetailFragment,
                PatientInfoFragment.newInstance(intent.getStringExtra(DefinedParams.PatientId))
            ).commit()
    }

    private fun attachObserver() {
        viewModel.aboveFiveYearsMetaLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }

                ResourceState.SUCCESS -> {
                    initializeFragments()
                    hideLoading()
                }
            }
        }
        viewModel.aboveFiveYearsCreateResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    binding.nestedScrollViewID.fullScroll(ScrollView.FOCUS_UP)
                    initializeSummaryFragments()
                }
            }
        }
        patientViewModel.patientsLiveData.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }
    }

    private fun initializeSummaryFragments() {
        binding.apply {
            patientDiagnosisContainer.visibility = View.GONE
        }
        replaceFragmentInId<MedicalReviewTreatmentPlanSummary>(
            binding.presentingComplaintsContainer.id,
            tag = MedicalReviewTreatmentPlanSummary::class.simpleName
        )
        val complaintsFragment =
            supportFragmentManager.findFragmentById(R.id.systemicExaminationsContainer)
        val clinicalNotesFragment =
            supportFragmentManager.findFragmentById(R.id.clinicalNotesContainer)
        complaintsFragment?.let {
            supportFragmentManager.beginTransaction().remove(complaintsFragment).commit()
        }
        clinicalNotesFragment?.let {
            supportFragmentManager.beginTransaction().remove(clinicalNotesFragment).commit()
        }
    }

    private fun initializeFragments() {
        binding.patientDiagnosisContainer.visibility = View.VISIBLE
        val bundle = Bundle().apply {
            putString(MedicalReviewTypeEnums.PresentingComplaints.name, MedicalReviewTypeEnums.AboveFiveYears.name)
            putString(MedicalReviewTypeEnums.SystemicExaminations.name, MedicalReviewTypeEnums.AboveFiveYears.name)
        }
        replaceFragmentInId<MedicalReviewPatientDiagnosisFragment>(
            binding.patientDiagnosisContainer.id,
            tag = MedicalReviewPatientDiagnosisFragment::class.simpleName
        )
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

    override fun onClick(view: View) {
        when (view.id) {
            binding.btnSubmit.id -> {
                postResultInput()
            }
        }
    }

    private fun postResultInput() {

    }

    private fun enableSubmitBtn() {
        binding.btnSubmit.isEnabled =
            viewModel.selectedPresentingComplaints.isNotEmpty() || viewModel.selectedSystemicExaminations.isNotEmpty() || viewModel.enteredClinicalNotes.isNotBlank() || viewModel.enteredComplaintNotes.isNotBlank() || viewModel.enteredExaminationNotes.isNotBlank()
    }

}