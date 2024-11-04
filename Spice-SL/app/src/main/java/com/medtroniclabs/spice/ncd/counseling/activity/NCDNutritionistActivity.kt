package com.medtroniclabs.spice.ncd.counseling.activity

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.medtroniclabs.spice.appextensions.setVisible
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.ActivityNcdNutritionistBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ncd.counseling.adapter.NCDNutritionAdapter
import com.medtroniclabs.spice.ncd.counseling.model.ResultModel
import com.medtroniclabs.spice.ncd.counseling.model.NCDCounselingModel
import com.medtroniclabs.spice.ncd.counseling.model.AssessmentResultModel
import com.medtroniclabs.spice.ncd.counseling.utils.ValidationListener
import com.medtroniclabs.spice.ncd.counseling.viewmodel.CounselingViewModel
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDMedicalReviewViewModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity

class NCDNutritionistActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityNcdNutritionistBinding

    private val viewModel: CounselingViewModel by viewModels()
    private val mrViewModel: NCDMedicalReviewViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNcdNutritionistBinding.inflate(layoutInflater)
        setMainContentView(binding.root, isToolbarVisible = true)
        saveIntentValues()
        attachObservers()
        setClickListener()
        getLifestyleList()
    }

    private fun saveIntentValues() {
        intent?.extras?.let { bundle ->
            viewModel.apply {
                patientReference = bundle.getString(DefinedParams.PatientId)
                memberReference = bundle.getString(DefinedParams.FhirId)
                encounterReference = bundle.getString(NCDMRUtil.EncounterReference)
            }
        }
    }

    fun attachObservers() {
        mrViewModel.ncdMedicalReviewStaticLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    openDialog()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }
        viewModel.assessmentListLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    loadLifestyleList()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                    resourceState.message?.let { message ->
                        showErrorDialogue(message = message) {}
                    }
                }
            }
        }
    }

    private fun setClickListener() {
        binding.btnAdd.safeClickListener(this)
        binding.btnDone.safeClickListener(this)
    }

    private fun openDialog() {
        val ncdLifestyleDialog = NCDLifestyleDialog.newInstance { }
        ncdLifestyleDialog.show(supportFragmentManager, NCDLifestyleDialog.TAG)
    }

    private fun loadLifestyleList() {
        val lifestyleList = viewModel.assessmentListLiveData.value?.data?.entityList
        if (lifestyleList.isNullOrEmpty())
            lifestyles(false)
        else {
            val adapter = NCDNutritionAdapter(validation)
            binding.rvPatientLifestyleList.layoutManager =
                LinearLayoutManager(binding.rvPatientLifestyleList.context ?: this)
            binding.rvPatientLifestyleList.adapter = adapter
            adapter.submitData(lifestyleList)
            lifestyles(true)
        }
    }

    private val validation = object : ValidationListener {
        override fun validate() {
            binding.btnDone.isEnabled =
                viewModel.assessmentListLiveData.value?.data?.entityList?.firstOrNull { !it.lifestyleAssessment.isNullOrBlank() && !it.otherNote.isNullOrBlank() } != null
        }
    }

    private fun getLifestyleList() {
        val request = NCDCounselingModel(
            patientReference = viewModel.patientReference,
            memberReference = viewModel.memberReference,
            visitId = viewModel.encounterReference
        )
        viewModel.getAssessmentList(request, true)
    }

    private fun lifestyles(visible: Boolean) {
        binding.rvPatientLifestyleList.setVisible(visible)
        binding.tvNoRecord.setVisible(!visible)
    }

    private fun historyLifestyles(visible: Boolean) {
        binding.rvHistoryLifestyleList.setVisible(visible)
        binding.tvNoHistoryRecord.setVisible(!visible)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            binding.btnAdd.id -> {
                if (NCDMRUtil.isNCDMRMetaLoaded())
                    openDialog()
                else {
                    withNetworkAvailability(online = {
                        mrViewModel.getStaticMetaData()
                    })
                }
            }

            binding.btnDone.id -> updateLifestyles()
        }
    }

    private fun updateLifestyles() {
        val lifestyles =
            viewModel.assessmentListLiveData.value?.data?.entityList?.filter { !it.lifestyleAssessment.isNullOrBlank() && !it.otherNote.isNullOrBlank() }
        if (lifestyles.isNullOrEmpty())
            return
        else {
            val items = ArrayList<ResultModel>()
            lifestyles?.forEach {
                items.add(
                    ResultModel(
                        id = it.id,
                        lifestyleAssessment = it.lifestyleAssessment,
                        otherNote = it.otherNote
                    )
                )
            }
            val request = AssessmentResultModel(
                lifestyles = items, patientReference = viewModel.patientReference,
                memberReference = viewModel.memberReference,
                visitId = viewModel.encounterReference
            )
            viewModel.updateAssessment(request, true)
        }
    }
}