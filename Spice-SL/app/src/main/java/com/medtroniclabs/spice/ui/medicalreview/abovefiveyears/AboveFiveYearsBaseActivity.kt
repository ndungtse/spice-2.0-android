package com.medtroniclabs.spice.ui.medicalreview.abovefiveyears

import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
import com.medtroniclabs.spice.common.DateUtils.getCurrentDateAndTime
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryRequest
import com.medtroniclabs.spice.data.model.AboveFiveYearsSubmitRequest
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.databinding.ActivityAboveFiveYearsBaseBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.medicalreview.ClinicalNotesFragment
import com.medtroniclabs.spice.ui.medicalreview.PresentingComplaintsFragment
import com.medtroniclabs.spice.ui.medicalreview.SystemicExaminationsFragment
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.CLINICAL_NOTES
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.PC_ITEM
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.SE_ITEM
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.mypatients.fragment.MedicalReviewPatientDiagnosisFragment
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
            getString(R.string.patient_medical_review),
            callback = {
                backNavigation()
            }
        )
        initializeViews()
        initializeFragments()
        initializeListeners()
        attachObserver()
    }

    private fun backNavigation() {
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
        this@AboveFiveYearsBaseActivity.finish()
    }

    private fun initializeListeners() {
        binding.btnSubmit.safeClickListener(this)
        binding.refreshLayout.setOnRefreshListener {
            swipeRefresh()
        }
    }

    private fun swipeRefresh() {
        supportFragmentManager.findFragmentById(R.id.presentingComplaintsContainer)
            .let { currentFragment ->
                if (currentFragment is AboveFiveYearsTreatmentSummaryFragment) {
                    viewModel.aboveFiveYearsCreateResponse.value?.data?.let {
                        viewModel.getAboveFiveYearsSummaryDetails(AboveFiveYearsSummaryRequest(id = it.id))
                    }
                } else {
                    patientViewModel.patientsLiveData.value?.data?.let { details ->
                        details.patientId?.let { id ->
                            patientViewModel.getPatients(id)
                        }
                    }
                }
            }
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
                    resourceState.data?.let {
                        viewModel.getAboveFiveYearsSummaryDetails(AboveFiveYearsSummaryRequest(id = it.id))
                    }
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
                    if (binding.refreshLayout.isRefreshing) {
                        binding.refreshLayout.isRefreshing = false
                    }
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
        replaceFragmentInId<AboveFiveYearsTreatmentSummaryFragment>(
            binding.presentingComplaintsContainer.id,
            tag = AboveFiveYearsTreatmentSummaryFragment::class.simpleName
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
        patientViewModel.patientsLiveData.value?.data?.let { details ->
            details.patientId?.let {id ->
                //TODO: Location for Lat and Long should be implemented,for householdId and organizationId once given means we need to integrate
                val request = AboveFiveYearsSubmitRequest(
                    patientId = id,
                    latitude = 11.21,
                    longitude = 10.75,
                    householdId = 23,
                    assessmentType = MedicalReviewTypeEnums.AboveFiveYears.name,
                    presentingComplaints = viewModel.selectedPresentingComplaints.map { it.value },
                    presentingComplaintsNotes = viewModel.enteredComplaintNotes,
                    systemicExaminationsNotes = viewModel.enteredExaminationNotes,
                    provenance = ProvanceDto(
                        createdDateTime = getCurrentDateAndTime(
                            DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                        )
                    ),
                    systemicExaminations = viewModel.selectedSystemicExaminations.map { it.value },
                    clinicalNotes = viewModel.enteredClinicalNotes,
                    startTime = getCurrentDateAndTime(
                        DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                    ),
                    endTime = getCurrentDateAndTime(
                        DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                    )
                )
                viewModel.createAboveFiveYearsResult(request)
            }
        }
    }

    private fun enableSubmitBtn() {
        binding.btnSubmit.isEnabled =
            viewModel.selectedPresentingComplaints.isNotEmpty() || viewModel.selectedSystemicExaminations.isNotEmpty() || viewModel.enteredClinicalNotes.isNotBlank() || viewModel.enteredComplaintNotes.isNotBlank() || viewModel.enteredExaminationNotes.isNotBlank()
    }

}