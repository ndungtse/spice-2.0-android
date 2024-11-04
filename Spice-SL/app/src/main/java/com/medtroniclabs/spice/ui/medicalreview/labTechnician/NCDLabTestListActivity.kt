package com.medtroniclabs.spice.ui.medicalreview.labTechnician

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.databinding.ActivityLabTestListBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.model.medicalreview.InvestigationModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.medicalreview.investigation.InvestigationGenerator
import com.medtroniclabs.spice.ui.medicalreview.investigation.InvestigationListener
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel

class NCDLabTestListActivity : BaseActivity(), View.OnClickListener, InvestigationListener {

    private lateinit var binding: ActivityLabTestListBinding
    private val patientDetailViewModel: PatientDetailViewModel by viewModels()
    private lateinit var investigationGenerator: InvestigationGenerator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLabTestListBinding.inflate(layoutInflater)
        setMainContentView(binding.root, true, homeAndBackVisibility = Pair(true, true))
        initView()
        attachObserver()
    }

    private fun attachObserver() {
        patientDetailViewModel.patientDetailsLiveData.observe(this) { resourceData ->
            when (resourceData.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    autoPopulateDetails(resourceData.data)
                }
            }
        }
    }

    private fun initView() {
        binding.bottomView.safeClickListener(this)
        intent?.let {
            patientDetailViewModel.origin = it.getStringExtra(DefinedParams.ORIGIN)
            it.getStringExtra(DefinedParams.FhirId)?.let { id ->
                patientDetailViewModel.getPatients(
                    id,
                    origin = patientDetailViewModel.origin?.lowercase()
                )
            }
        }
        investigationGenerator = InvestigationGenerator(
            this@NCDLabTestListActivity,
            binding.llInvestigationHolder,
            binding.nestedScrollView,
            false,
            this
        )
    }

    private fun autoPopulateDetails(data: PatientListRespModel?) {
        data?.let { patientDetails ->
            binding.tvProgramId.text =
                patientDetails.programId?.toString() ?: getString(R.string.separator_hyphen)
            binding.tvNationalId.text =
                patientDetails.identityValue ?: getString(R.string.separator_hyphen)
            var diagnosisText = ""
            if (patientDetails.isConfirmDiagnosis) {
                diagnosisText =
                    patientDetails.diagnosis?.joinToString(separator = getString(R.string.comma_symbol))
                        .toString()
            }
            binding.tvDiagnoses.text = diagnosisText

            patientDetails.cvdRiskScore?.let { score ->
                binding.tvCVD.text = StringConverter.appendTexts(
                    "${score}%",
                    patientDetails.cvdRiskLevel, separator = getString(R.string.separator_hyphen)
                )
                val textColor = CommonUtils.cvdRiskColorCode(score, this)
                binding.tvCVD.setTextColor(textColor)
            }
            data.firstName?.let { firstName ->
                val text = StringConverter.appendTexts(firstText = firstName, data.lastName)
                setTitle(
                    StringConverter.appendTexts(
                        firstText = text,
                        data.age?.toString(),
                        data.gender,
                        separator = getString(R.string.separator_hyphen)
                    )
                )
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.bottomView.id -> {
                finish()
            }
        }
    }

    override fun removeInvestigation(investigationGenerator: InvestigationModel) {
//     Method not to be used
    }

    override fun checkValidation() {
//     Method not to be used
    }
}