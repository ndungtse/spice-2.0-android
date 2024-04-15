package com.medtroniclabs.spice.ui.medicalreview.undertwomonths

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.databinding.ActivityUnderTwoMonthsBaseBinding
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.medicalreview.ClinicalNotesFragment
import com.medtroniclabs.spice.ui.medicalreview.ExaminationCardFragment
import com.medtroniclabs.spice.ui.medicalreview.PresentingComplaintsFragment
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.mypatients.fragment.MedicalReviewPatientDiagnosisFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientInfoFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UnderTwoMonthsBaseActivity : BaseActivity() {

    private lateinit var binding: ActivityUnderTwoMonthsBaseBinding
    private val viewModel: UnderTwoMonthViewModel by viewModels()
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
}