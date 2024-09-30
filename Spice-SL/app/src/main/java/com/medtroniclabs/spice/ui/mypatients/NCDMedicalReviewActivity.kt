package com.medtroniclabs.spice.ui.mypatients

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.databinding.ActivityNcdMrBaseBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.ncd.medicalreview.fragment.NCDMedicalReviewDiagnosisCardFragment
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.AncVisitCallBack
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientInfoFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.NCDMedicalReviewViewModel
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NCDMedicalReviewActivity : BaseActivity(), View.OnClickListener, AncVisitCallBack {

    private val viewModel: NCDMedicalReviewViewModel by viewModels()
    private val patientDetailViewModel: PatientDetailViewModel by viewModels()
    private lateinit var binding: ActivityNcdMrBaseBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNcdMrBaseBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.patient_medical_review)
        )
        initializeStaticDataSave()
        setListeners()
        attachObservers()
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

    private fun onBackPressPopStack() {
        this@NCDMedicalReviewActivity.finish()
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

    fun initView() {
        initializePatientDetails()
    }

    private fun setListeners() {
        binding.btnLayout.btnNext.safeClickListener(this)
    }

    private fun initializePatientDetails() {
        patientDetailViewModel.origin = intent.extras?.getString(DefinedParams.ORIGIN)
        val patientId = intent.getStringExtra(DefinedParams.FhirId)
        val fragment = PatientInfoFragment.newInstanceForNCD(patientId, "medicalreview")
        fragment.setDataCallback(this)
        supportFragmentManager.beginTransaction()
            .add(
                R.id.patientDetailFragment,
                fragment
            ).commit()
    }

    private fun initializeFragments() {
        supportFragmentManager.beginTransaction()
            .add(
                R.id.medicalDiagnosisContainer,
                NCDMedicalReviewDiagnosisCardFragment.newInstance()
            ).commit()
        hideLoading()
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.btnLayout.btnNext.id -> {
            }
        }
    }

    override fun onDataLoaded(details: PatientListRespModel) {
        initializeFragments()
    }
}