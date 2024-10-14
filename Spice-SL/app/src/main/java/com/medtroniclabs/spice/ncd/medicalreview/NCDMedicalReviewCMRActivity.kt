package com.medtroniclabs.spice.ncd.medicalreview

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.ActivityNcdmedicalReviewCmractivityBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.EncounterReference
import com.medtroniclabs.spice.ncd.medicalreview.dialog.NCDTreatmentPlanDialog
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.home.AssessmentToolsActivity
import com.medtroniclabs.spice.ui.medicalreview.investigation.InvestigationActivity
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.AncVisitCallBack
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientInfoFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NCDMedicalReviewCMRActivity : BaseActivity(), View.OnClickListener, AncVisitCallBack {
    private lateinit var binding: ActivityNcdmedicalReviewCmractivityBinding
    private val patientDetailViewModel: PatientDetailViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        binding = ActivityNcdmedicalReviewCmractivityBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.patient_medical_review),
            homeAndBackVisibility = Pair(true, true),
        )
        initView()
    }

    private fun initView() {
        binding.btnLayout.clBtn.gone()
        binding.btnMedicalReview.safeClickListener(this)
        binding.btnLayout.ivTreatmentPlan.safeClickListener(this)
        binding.btnLayout.ivInvestigation.safeClickListener(this)
        initializePatientDetails()
    }

    private fun initializePatientDetails() {
        patientDetailViewModel.origin = intent.extras?.getString(DefinedParams.ORIGIN)
        val fragment = PatientInfoFragment.newInstanceForNCD(getPatientId(), getOrigin() ?: "")
        fragment.setDataCallback(this)
        addOrReuseFragment(
            R.id.patientDetailFragment,
            PatientInfoFragment.TAG,
            fragment
        )
        hideLoading()
    }

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressPopStack()
            }
        }

    private fun onBackPressPopStack() {
        this@NCDMedicalReviewCMRActivity.finish()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnMedicalReview.id -> {
                val intent = Intent(this, AssessmentToolsActivity::class.java)
                intent.putExtra(DefinedParams.FhirId, getFhirId())
                intent.putExtra(DefinedParams.PatientId, getPatientId())
                intent.putExtra(DefinedParams.ORIGIN, getOrigin())
                intent.putExtra(DefinedParams.Gender, getGender())
                intent.putExtra(EncounterReference, getEncounterReference())
                startActivity(intent)
            }
            binding.btnLayout.ivTreatmentPlan.id -> {
                val patientId = getPatientId()
                val fhirId = getFhirId()
                if (patientId.isNullOrBlank() || fhirId.isNullOrBlank())
                    return
                else {
                    val dialog =
                        NCDTreatmentPlanDialog.newInstance(
                            patientId,
                            fhirId
                        ) { isPositiveResult, message ->
                            if (isPositiveResult)
                                showSuccessDialogue(
                                    title = getString(R.string.treatment_plan),
                                    message = message
                                ) {}
                            else
                                showErrorDialogue(
                                    title = getString(R.string.error),
                                    message = message,
                                    positiveButtonName = getString(R.string.ok),
                                ) {}
                        }
                    dialog.show(supportFragmentManager, NCDTreatmentPlanDialog.TAG)
                }
            }
            binding.btnLayout.ivInvestigation.id  -> {
                patientDetailViewModel.patientDetailsLiveData.value?.data?.let { data ->
                    val intent = Intent(this, InvestigationActivity::class.java)
                    intent.putExtra(DefinedParams.PatientId, data.id)
                    // TODO need to get investigation in summary(After confirm with backend)
                    intent.putExtra(DefinedParams.EncounterId, patientDetailViewModel.encounterId)
                    intent.putExtra(EncounterReference, getEncounterReference())
                    intent.putExtra(DefinedParams.MemberID, data.id)
                    intent.putExtra(DefinedParams.ORIGIN, getMenuOrigin())
                    intent.putExtra(NCDMRUtil.NCD, NCDMRUtil.NCD)
                    getResult.launch(intent)
                }
            }
        }
    }

    private fun getMenuOrigin(): String? {
        return intent.getStringExtra(DefinedParams.ORIGIN)
    }

    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                val value = it.data?.getStringExtra(DefinedParams.EncounterId)
                value?.let { valueString ->
                    patientDetailViewModel.encounterId = valueString
                }
            }
        }

    private fun getFhirId(): String? {
        return intent.getStringExtra(DefinedParams.FhirId)
    }

    private fun getPatientId(): String? {
        return intent.getStringExtra(DefinedParams.PatientId)
    }

    private fun getOrigin(): String? {
        return intent.getStringExtra(DefinedParams.ORIGIN)
    }

    private fun getGender(): String? {
        return intent.getStringExtra(DefinedParams.Gender)
    }

    private fun getEncounterReference(): String? {
        return intent.getStringExtra(EncounterReference)
    }

    override fun onDataLoaded(details: PatientListRespModel) {
        // after patient details loaded
    }
}