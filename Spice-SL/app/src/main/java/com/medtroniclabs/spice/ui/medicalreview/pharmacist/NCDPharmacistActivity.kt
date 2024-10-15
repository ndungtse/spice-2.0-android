package com.medtroniclabs.spice.ui.medicalreview.pharmacist

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.databinding.ActivityNcdPharmacistBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NCDPharmacistActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityNcdPharmacistBinding
    private val patientDetailViewModel: PatientDetailViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNcdPharmacistBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.pharmacist_title),
            homeAndBackVisibility = Pair(false, true),
        )
        initView()
        getPatientDetails()
        attachObserver()
    }

    private fun attachObserver() {
        patientDetailViewModel.patientDetailsLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> showLoading()
                ResourceState.ERROR -> hideLoading()
                ResourceState.SUCCESS -> {
                    hideLoading()
                    loadPatientInfo(resourceState.data)
                }
            }
        }
    }

    private fun loadPatientInfo(data: PatientListRespModel?) {
        data?.let {patientInfo ->
            patientInfo.programId?.let { programId ->
                binding.tvProgramId.text = programId
            }
            patientInfo.identityValue?.let { nationalId ->
                binding.tvProgramId.text = nationalId
            }
            data.firstName?.let {
                val text = StringConverter.appendTexts(firstText = it, data.lastName)
                setTitle(
                    StringConverter.appendTexts(
                        firstText = text,
                        data.age?.toInt().toString(),
                        data.gender,
                        separator = getString(R.string.separator_hyphen)
                    )
                )
            }
        }
    }

    private fun initView() {
        binding.bottomView.btnDone.safeClickListener(this)
        binding.bottomView.btnCancel.safeClickListener(this)
        replaceFragmentInId<NCDPharmacistFragment>(
            binding.prescriptionRefillFragment.id,
            tag = NCDPharmacistFragment.TAG
        )
    }

    private fun getPatientDetails() {
        intent?.let {
            patientDetailViewModel.origin = it.getStringExtra(DefinedParams.ORIGIN)
            it.getStringExtra(DefinedParams.FhirId)?.let { id ->
                patientDetailViewModel.getPatients(id, origin = patientDetailViewModel.origin?.lowercase())
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.bottomView.btnDone.id -> {
                val existingDialog = supportFragmentManager.findFragmentByTag(NCDQuantityDifferenceDialogueFragment.TAG)
                if (existingDialog == null) {
                    NCDQuantityDifferenceDialogueFragment.newInstance()
                        .show(supportFragmentManager, NCDQuantityDifferenceDialogueFragment.TAG)
                }
            }

            binding.bottomView.btnCancel.id -> {
                finish()
            }
        }
    }
}