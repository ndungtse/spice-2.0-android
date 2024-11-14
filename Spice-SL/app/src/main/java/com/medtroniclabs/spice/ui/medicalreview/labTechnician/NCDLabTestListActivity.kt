package com.medtroniclabs.spice.ui.medicalreview.labTechnician

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.databinding.ActivityLabTestListBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.model.medicalreview.InvestigationModel
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.medicalreview.investigation.InvestigationGenerator
import com.medtroniclabs.spice.ui.medicalreview.investigation.InvestigationListener
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel

class NCDLabTestListActivity : BaseActivity(), View.OnClickListener, InvestigationListener {

    private lateinit var binding: ActivityLabTestListBinding
    private val viewModel: NCDLabTestViewModel by viewModels()
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
                    resourceData.data?.let { data ->
                        viewModel.getLabTestList(data)
                    } ?: kotlin.run {
                        hideLoading()
                    }
                }
            }
        }

        viewModel.labTestListLiveData.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    resource.data?.let {
                        viewModel.addExistingLabTestListToUI(it)
                    }
                }
            }
        }

        viewModel.investigationListLiveData.observe(this) { investigationList ->
            showAdapterList(investigationList)
        }

        viewModel.createLabTestLiveData.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }

                ResourceState.SUCCESS -> {
                    resource.data?.let { map ->
                        hideLoading()
                        val intent = Intent()
                        if (map.containsKey(DefinedParams.EncounterId)) {
                            val value = map[DefinedParams.EncounterId]
                            if (value is String) {
                                intent.putExtra(DefinedParams.EncounterId, value)
                            }
                        }
                        setResult(RESULT_OK, intent)
                        finish()
                    } ?: kotlin.run {
                        hideLoading()
                    }
                }
            }
        }
    }

    private fun showAdapterList(investigationList: ArrayList<InvestigationModel>) {
        binding.llInvestigationHolder.removeAllViews()
        if (investigationList.isNotEmpty()) {
            val filteredList =
                investigationList.filter { it.labTestResultList.isNullOrEmpty() } as ArrayList<InvestigationModel>
            if (filteredList.isNotEmpty()) {
                binding.llInvestigationHolder.visible()
                binding.tvNoInvestigationDataFound.gone()
                investigationGenerator.populateViews(filteredList, true)
            } else {
                binding.llInvestigationHolder.gone()
                binding.tvNoInvestigationDataFound.visible()
            }
        } else {
            binding.llInvestigationHolder.gone()
            binding.tvNoInvestigationDataFound.visible()
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
                if (investigationGenerator.onValidateInput(true)) {
                    patientDetailViewModel.patientDetailsLiveData.value?.data?.let { data ->
                        viewModel.createLabTest(
                            geyPayloadForLabTest(investigationGenerator.getResultFromInvestigation()),
                            data
                        )
                    }
                } else {
                    investigationGenerator.getResultFromInvestigation()?.let {
                        binding.llInvestigationHolder.removeAllViews()
                        investigationGenerator.populateViews(ArrayList(it), true)
                    }
                }
            }
        }
    }

    private fun geyPayloadForLabTest(resultFromInvestigation: List<InvestigationModel>?): List<InvestigationModel>? {
        val list =
            resultFromInvestigation?.filter { it.id == null || (it.resultHashMap != null && it.resultHashMap!!.size > 0) }
        return list
    }

    override fun removeInvestigation(investigationGenerator: InvestigationModel) {
//     Method not to be used
    }

    override fun checkValidation() {
//     Method not to be used
    }
}

