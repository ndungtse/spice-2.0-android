package com.medtroniclabs.spice.ncd.counseling.activity

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.setVisible
import com.medtroniclabs.spice.appextensions.textOrHyphen
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.databinding.ActivityNcdNutritionistBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.ncd.counseling.adapter.NCDNutritionAdapter
import com.medtroniclabs.spice.ncd.counseling.adapter.NCDNutritionHistoryAdapter
import com.medtroniclabs.spice.ncd.counseling.model.AssessmentResultModel
import com.medtroniclabs.spice.ncd.counseling.model.NCDCounselingModel
import com.medtroniclabs.spice.ncd.counseling.model.ResultModel
import com.medtroniclabs.spice.ncd.counseling.utils.ValidationListener
import com.medtroniclabs.spice.ncd.counseling.viewmodel.CounselingViewModel
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDMedicalReviewViewModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel

class NCDNutritionistActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityNcdNutritionistBinding

    private val viewModel: CounselingViewModel by viewModels()
    private val patientViewModel: PatientDetailViewModel by viewModels()
    private val mrViewModel: NCDMedicalReviewViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        binding = ActivityNcdNutritionistBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            isToolbarVisible = true,
            homeAndBackVisibility = Pair(true, true),
            callback = {
                backHandelFlow()
            },
            callbackHome = {
                backHandelFlow()
            }
        )
        saveIntentValues()
        attachObservers()
        setClickListener()

        getPatientDetails()
        getLifestyleList()
    }

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backHandelFlow()
            }
        }

    private fun backHandelFlow() {
        if (binding.btnDone.isEnabled) {
            showErrorDialogue(
                getString(R.string.alert),
                getString(R.string.exit_reason_message),
                isNegativeButtonNeed = true
            ) {
                if (it) finish()
            }
        } else
            finish()
    }

    private fun getPatientDetails() {
        viewModel.memberReference?.let { memberId ->
            patientViewModel.getPatients(id = memberId, origin = MenuConstants.LIFESTYLE)
        }
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
        viewModel.updateAssessmentLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState?.data?.message?.let { message ->
                        showSuccessDialogue(
                            title = getString(R.string.lifestyle_assessment),
                            message = message
                        ) { finish() }
                    } ?: run { finish() }
                }

                ResourceState.ERROR -> {
                    hideLoading()
                    resourceState.message?.let { message ->
                        showErrorDialogue(message = message) {}
                    }
                }
            }
        }
        patientViewModel.patientDetailsLiveData.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    resource.data?.let {
                        loadPatientInfo(it)
                    }
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }
    }

    private fun loadPatientInfo(data: PatientListRespModel) {
        binding.apply {
            tvProgramId.text = data.patientId.textOrHyphen()
            tvNationalId.text = data.identityValue.textOrHyphen()
        }
        data.firstName?.let {
            val text = StringConverter.appendTexts(firstText = it, data.lastName)
            setTitle(
                StringConverter.appendTexts(
                    firstText = text,
                    data.age.toString(),
                    data.gender,
                    separator = "-"
                )
            )
        }
    }

    private fun setClickListener() {
        binding.apply {
            btnAdd.safeClickListener(this@NCDNutritionistActivity)
            btnDone.safeClickListener(this@NCDNutritionistActivity)
            tvViewHistory.safeClickListener(this@NCDNutritionistActivity)
        }
    }

    private fun openDialog() {
        val ncdLifestyleDialog = NCDLifestyleDialog.newInstance {
            getLifestyleList()
        }
        ncdLifestyleDialog.show(supportFragmentManager, NCDLifestyleDialog.TAG)
    }

    private fun loadLifestyleList() {
        val lifestyleList = viewModel.assessmentListLiveData.value?.data?.entityList
        if (lifestyleList.isNullOrEmpty()) {
            lifestyles(false)
            historyLifestyles(false)
        } else {
            val (list, historyList) = lifestyleList.partition { it.assessedBy.isNullOrBlank() }
            loadList(ArrayList(list))
            loadHistoryList(ArrayList(historyList))
        }
    }

    private fun loadHistoryList(lifestyleList: java.util.ArrayList<NCDCounselingModel>) {
        if (lifestyleList.isNotEmpty()) {
            val adapter = NCDNutritionHistoryAdapter()
            binding.rvHistoryLifestyleList.layoutManager =
                LinearLayoutManager(binding.rvHistoryLifestyleList.context ?: this)
            binding.rvHistoryLifestyleList.adapter = adapter
            adapter.submitData(lifestyleList)
            historyLifestyles(true)
        } else
            historyLifestyles(false)
    }

    private fun loadList(lifestyleHistoryList: java.util.ArrayList<NCDCounselingModel>) {
        if (lifestyleHistoryList.isNotEmpty()) {
            val adapter = NCDNutritionAdapter(validation)
            binding.rvPatientLifestyleList.layoutManager =
                LinearLayoutManager(binding.rvPatientLifestyleList.context ?: this)
            binding.rvPatientLifestyleList.adapter = adapter
            adapter.submitData(lifestyleHistoryList)
            lifestyles(true)
        } else
            lifestyles(false)
    }

    private val validation = object : ValidationListener {
        override fun validate() {
            binding.btnDone.isEnabled =
                viewModel.assessmentListLiveData.value?.data?.entityList?.firstOrNull { !it.lifestyleAssessment.isNullOrBlank() } != null
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

            binding.tvViewHistory.id -> binding.clLifeStyleHistory.setVisible(binding.clLifeStyleHistory.visibility == View.GONE)
        }
    }

    private fun updateLifestyles() {
        val lifestyles =
            viewModel.assessmentListLiveData.value?.data?.entityList?.filter { it.assessedBy.isNullOrBlank() && !it.lifestyleAssessment.isNullOrBlank() }
        if (lifestyles.isNullOrEmpty())
            return
        else {
            val items = ArrayList<ResultModel>()
            lifestyles.forEach {
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
                visitId = viewModel.encounterReference,
                patientVisitId = viewModel.encounterReference,
                assessedBy = NCDMRUtil.currentUserId(),
                assessedByDisplay = NCDMRUtil.getUserName(),
                assessedDate = DateUtils.getTodayDateDDMMYYYY()
            )
            viewModel.updateAssessment(request, true)
        }
    }
}