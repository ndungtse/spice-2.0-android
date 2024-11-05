package com.medtroniclabs.spice.ncd.medicalreview

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.setVisible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.ORIGIN
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.databinding.ActivityNcdmedicalReviewCmractivityBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.EncounterReference
import com.medtroniclabs.spice.ncd.medicalreview.dialog.NCDTreatmentPlanDialog
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDMedicalReviewViewModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.home.AssessmentToolsActivity
import com.medtroniclabs.spice.ui.medicalreview.investigation.InvestigationActivity
import com.medtroniclabs.spice.ncd.counseling.activity.NCDCounselingActivity
import com.medtroniclabs.spice.ncd.counseling.activity.NCDLifestyleActivity
import com.medtroniclabs.spice.ncd.medicalreview.prescription.activity.NCDPrescriptionActivity
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.AncVisitCallBack
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientInfoFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NCDMedicalReviewCMRActivity : BaseActivity(), View.OnClickListener, AncVisitCallBack {
    private lateinit var binding: ActivityNcdmedicalReviewCmractivityBinding
    private val viewModel: NCDMedicalReviewViewModel by viewModels()
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
        initializeStaticDataSave()
        initView()
        attachObservers()
    }

    private fun initializeStaticDataSave() {
        if (!(SecuredPreference.getBoolean(SecuredPreference.EnvironmentKey.IS_NCD_MEDICAL_REVIEW_LOADED.name))) {
            withNetworkAvailability(online = {
                viewModel.getStaticMetaData()
            }, offline = {
                onBackPressPopStack()
            })
        } else {
            initView()
        }
    }

    fun attachObservers() {
        viewModel.ncdMedicalReviewStaticLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    initView()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                    showErrorDialog()
                }
            }
        }
        patientDetailViewModel.patientDetailsLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
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
                    if (binding.refreshLayout.isRefreshing) {
                        binding.refreshLayout.isRefreshing = false
                    }
                    showError()
                }
            }
        }
    }

    private fun showErrorDialog() {
        showErrorDialogue(
            getString(R.string.alert),
            getString(R.string.exit_reason),
            isNegativeButtonNeed = true
        ) { isPositive ->
            if (isPositive) {
                onBackPressPopStack()
            }
        }
        hideLoading()
    }

    private fun showError(isActivityClosed: Boolean = false) {
        showErrorDialogue(
            title = getString(R.string.alert),
            message = getString(R.string.something_went_wrong_try_later),
            positiveButtonName = getString(R.string.ok),
        ) { isPositiveResult ->
            if (isPositiveResult && isActivityClosed) {
                onBackPressPopStack()
            }
        }
    }

    private fun initView() {
        binding.btnLayout.clPsycMenu.setVisible(CommonUtils.isPsychologicalFlowEnabled())
        binding.btnLayout.clBtn.gone()
        binding.btnMedicalReview.safeClickListener(this)
        binding.btnLayout.ivTreatmentPlan.safeClickListener(this)
        binding.btnLayout.ivInvestigation.safeClickListener(this)
        binding.btnLayout.ivLifestyle.safeClickListener(this)
        binding.btnLayout.clPsycMenu.safeClickListener(this)
        binding.btnLayout.ivPrescriptionImgView.safeClickListener(this)
        withNetworkAvailability(online = {
            initializePatientDetails()
        })
        binding.refreshLayout.setOnRefreshListener {
            withNetworkAvailability(online = {
                swipeRefresh()
            }, offline = {
                if (binding.refreshLayout.isRefreshing) {
                    binding.refreshLayout.isRefreshing = false
                }
            })
        }
    }

    private fun swipeRefresh() {
        patientDetailViewModel.patientDetailsLiveData.value?.data?.let { details ->
            details.id?.let { id ->
                patientDetailViewModel.getPatients(id, origin = patientDetailViewModel.origin)
            }
        }
    }

    private fun initializePatientDetails() {
        patientDetailViewModel.origin = intent.extras?.getString(DefinedParams.ORIGIN)
        val fragment = PatientInfoFragment.newInstanceForNCD(getFhirId(), getOrigin() ?: "")
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

            binding.btnLayout.ivInvestigation.id -> {
                patientDetailViewModel.patientDetailsLiveData.value?.data?.let { data ->
                    val intent = Intent(this, InvestigationActivity::class.java)
                    intent.putExtra(DefinedParams.PatientId, data.id)
                    intent.putExtra(EncounterReference, getEncounterReference())
                    intent.putExtra(DefinedParams.MemberID, data.id)
                    intent.putExtra(DefinedParams.ORIGIN, getMenuOrigin())
                    intent.putExtra(NCDMRUtil.NCD, NCDMRUtil.NCD)
                    getResult.launch(intent)
                }
            }
            binding.btnLayout.ivLifestyle.id -> navigateUser(
                Intent(
                    this,
                    NCDLifestyleActivity::class.java
                )
            )

            binding.btnLayout.clPsycMenu.id -> navigateUser(
                Intent(
                    this,
                    NCDCounselingActivity::class.java
                )
            )

            binding.btnLayout.ivPrescriptionImgView.id -> {
                if (!patientDetailViewModel.getPatientId().isNullOrEmpty() && !getEncounterReference().isNullOrEmpty()){
                    withNetworkAvailability(online ={
                        val intent = Intent(this, NCDPrescriptionActivity::class.java)
                        intent.putExtra(ORIGIN, DefinedParams.MedicalReview)
                        intent.putExtra(DefinedParams.PatientId, patientDetailViewModel.getPatientId()  )
                        intent.putExtra(DefinedParams.id,  patientDetailViewModel.getPatientFHIRId())
                        intent.putExtra(DefinedParams.PatientVisitId, getEncounterReference())
                        startActivity(intent)
                    } )
                }
            }
        }
    }

    private fun navigateUser(intent: Intent) {
        val bundle = Bundle()
        bundle.putString(NCDMRUtil.PATIENT_REFERENCE, patientDetailViewModel.getPatientId())
        bundle.putString(NCDMRUtil.MEMBER_REFERENCE, patientDetailViewModel.getPatientFHIRId())
        bundle.putString(NCDMRUtil.VISIT_ID, getEncounterReference())
        intent.putExtras(bundle)
        startActivity(intent)
    }

    private fun getMenuOrigin(): String? {
        return intent.getStringExtra(DefinedParams.ORIGIN)
    }

    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                swipeRefresh()
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