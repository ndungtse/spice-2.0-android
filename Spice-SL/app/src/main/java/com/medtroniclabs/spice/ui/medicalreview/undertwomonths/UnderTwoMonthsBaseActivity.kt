package com.medtroniclabs.spice.ui.medicalreview.undertwomonths

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.databinding.ActivityUnderTwoMonthsBaseBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.medicalreview.ClinicalNotesFragment
import com.medtroniclabs.spice.ui.medicalreview.ExaminationCardFragment
import com.medtroniclabs.spice.ui.medicalreview.PresentingComplaintsFragment
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.ExaminationsComplaintsViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.mypatients.fragment.MedicalReviewPatientDiagnosisFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.UnderTwoMonthsTreatmentSummaryFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientInfoFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UnderTwoMonthsBaseActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityUnderTwoMonthsBaseBinding
    private val viewModel: UnderTwoMonthViewModel by viewModels()
    private val clinicalNotesViewModel : ExaminationsComplaintsViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnderTwoMonthsBaseBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.patient_medical_review)
        )
        initializeViews()
        attachObserver()
        initializeListeners()
    }

    private fun attachObserver() {
        viewModel.underTwoMonthsMetaLiveData.observe(this){resourceState ->
            when(resourceState.state){
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
        if (!(SecuredPreference.getBoolean(SecuredPreference.EnvironmentKey.IS_UNDER_TWO_MONTHS_LOADED.name))){
            viewModel.getStaticMetaData()
        } else {
            initializeFragments()
        }
        supportFragmentManager
            .setFragmentResultListener(MedicalReviewDefinedParams.PC_ITEM, this) { _, _ ->
                updateNextButtonState()
            }
        supportFragmentManager
            .setFragmentResultListener(MedicalReviewDefinedParams.CLINICAL_NOTES, this) { _, _ ->
                updateNextButtonState()
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
                MedicalReviewDefinedParams.PC_5YEARS
            )
            putString(
                MedicalReviewTypeEnums.SystemicExaminations.name,
                MedicalReviewDefinedParams.PC_5YEARS
            )
        }
        replaceFragmentInId<MedicalReviewPatientDiagnosisFragment>(
            binding.patientDiagnosisContainer.id,
            tag = MedicalReviewPatientDiagnosisFragment::class.simpleName
        )
        replaceFragmentInId<BirthHistoryFragment>(
            binding.birthHistoryContainer.id,
            tag = BirthHistoryFragment::class.simpleName
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
            bundle = bundle,
            tag = PresentingComplaintsFragment::class.simpleName
        )
        replaceFragmentInId<ClinicalNotesFragment>(
            binding.clinicalNotesContainer.id,
            bundle = bundle,
            tag = ClinicalNotesFragment::class.simpleName
        )
    }
    private fun initializeListeners() {
        binding.btnSubmit.safeClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnSubmit -> {
//                if (validateInputs() == true) {
                    replaceFragmentInId<UnderTwoMonthsTreatmentSummaryFragment>(
                        binding.birthHistoryContainer.id,
                        tag = UnderTwoMonthsTreatmentSummaryFragment::class.simpleName
                    )
                removeFragment(R.id.clinicalSummaryContainer)
                removeFragment(R.id.examinationsContainer)
                removeFragment(R.id.presentingComplaintsContainer)
                removeFragment(R.id.clinicalNotesContainer)
                removeFragment(R.id.patientDiagnosisContainer)
//                }
            }
        }
    }


    private fun removeFragment(clinicalSummaryContainer: Int) {
        supportFragmentManager.findFragmentById(clinicalSummaryContainer)?.let {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    fun updateNextButtonState() {
        binding.btnSubmit.isEnabled = isAnyEditTextFilled()
    }

    private fun isAnyEditTextFilled(): Boolean {
        val clinicalSummaryFragment =
            supportFragmentManager.findFragmentById(R.id.clinicalSummaryContainer) as? ClinicalSummaryFragment
        val clinicalSummaryFilled = clinicalSummaryFragment?.isAnyEditTextFilled() ?: false
        return clinicalSummaryFilled && clinicalNotesViewModel.enteredClinicalNotes.isNotBlank() && clinicalNotesViewModel.enteredComplaintNotes.isNotBlank()
    }

    private fun validateInputs(): Boolean? {
        val clinicalSummaryFragment =
            supportFragmentManager.findFragmentById(R.id.clinicalSummaryContainer) as? ClinicalSummaryFragment
        return clinicalSummaryFragment?.validateEditFields()
    }
}