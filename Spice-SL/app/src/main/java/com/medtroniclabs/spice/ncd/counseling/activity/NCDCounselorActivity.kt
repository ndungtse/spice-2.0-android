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
import com.medtroniclabs.spice.databinding.ActivityNcdCounselorBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.ncd.counseling.adapter.NCDCounselorAdapter
import com.medtroniclabs.spice.ncd.counseling.adapter.NCDCounselorHistoryAdapter
import com.medtroniclabs.spice.ncd.counseling.model.AssessmentResultModel
import com.medtroniclabs.spice.ncd.counseling.model.NCDCounselingModel
import com.medtroniclabs.spice.ncd.counseling.model.ResultModel
import com.medtroniclabs.spice.ncd.counseling.utils.ValidationListener
import com.medtroniclabs.spice.ncd.counseling.viewmodel.CounselingViewModel
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel

class NCDCounselorActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityNcdCounselorBinding

    private val viewModel: CounselingViewModel by viewModels()
    private val patientViewModel: PatientDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        binding = ActivityNcdCounselorBinding.inflate(layoutInflater)
        setMainContentView(binding.root,
            isToolbarVisible = true,
            homeAndBackVisibility = Pair(true, true),
            callback = {
                backHandelFlow()
            },
            callbackHome = {
                backHandelFlow()
            })
        saveIntentValues()
        attachObservers()
        setClickListener()

        getPatientDetails()
        getCounselingList()
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
            patientViewModel.getPatients(id = memberId, origin = MenuConstants.PSYCHOLOGICAL)
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
        viewModel.updateAssessmentLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState?.data?.message?.let { message ->
                        showSuccessDialogue(
                            title = getString(R.string.psychological_assessment),
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
        viewModel.assessmentListLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    loadCounselingList()
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
            btnAdd.safeClickListener(this@NCDCounselorActivity)
            btnDone.safeClickListener(this@NCDCounselorActivity)
            tvViewHistory.safeClickListener(this@NCDCounselorActivity)
        }
    }

    private fun openDialog() {
        val ncdCounselingDialog = NCDCounselingDialog.newInstance { isPositiveResult ->
            if (isPositiveResult)
                getCounselingList()
        }
        ncdCounselingDialog.show(supportFragmentManager, NCDCounselingDialog.TAG)
    }

    private fun loadCounselingList() {
        val counselingList = viewModel.assessmentListLiveData.value?.data?.entityList
        if (counselingList.isNullOrEmpty()) {
            counseling(false)
            historyCounseling(false)
        } else {
            val (list, historyList) = counselingList.partition { it.assessedBy.isNullOrBlank() }
            loadList(ArrayList(list))
            loadHistoryList(ArrayList(historyList))
        }
    }

    private fun loadHistoryList(historyList: ArrayList<NCDCounselingModel>) {
        if (historyList.isNotEmpty()) {
            val historyAdapter = NCDCounselorHistoryAdapter()
            binding.rvHistoryPsychological.layoutManager =
                LinearLayoutManager(binding.rvHistoryPsychological.context ?: this)
            binding.rvHistoryPsychological.adapter = historyAdapter
            historyAdapter.submitData(historyList)
            historyCounseling(true)
        } else
            historyCounseling(false)
    }

    private fun loadList(list: ArrayList<NCDCounselingModel>) {
        if (list.isNotEmpty()) {
            val adapter = NCDCounselorAdapter(validation)
            binding.rvCounselorAssessmentList.layoutManager =
                LinearLayoutManager(binding.rvCounselorAssessmentList.context ?: this)
            binding.rvCounselorAssessmentList.adapter = adapter
            adapter.submitData(list)
            counseling(true)
        } else
            counseling(false)
    }

    private val validation = object : ValidationListener {
        override fun validate() {
            binding.btnDone.isEnabled =
                viewModel.assessmentListLiveData.value?.data?.entityList?.firstOrNull { !it.otherNote.isNullOrBlank() } != null
        }
    }

    private fun getCounselingList() {
        val request = NCDCounselingModel(
            patientReference = viewModel.patientReference,
            memberReference = viewModel.memberReference,
            visitId = viewModel.encounterReference
        )
        viewModel.getAssessmentList(request, false)
    }

    private fun counseling(visible: Boolean) {
        binding.rvCounselorAssessmentList.setVisible(visible)
        binding.tvNoRecord.setVisible(!visible)
    }

    private fun historyCounseling(visible: Boolean) {
        binding.rvHistoryPsychological.setVisible(visible)
        binding.tvNoHistoryRecord.setVisible(!visible)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            binding.btnAdd.id -> openDialog()

            binding.btnDone.id -> updateCounseling()

            binding.tvViewHistory.id -> binding.clPsychologicalHistory.setVisible(binding.clPsychologicalHistory.visibility == View.GONE)
        }
    }

    private fun updateCounseling() {
        val counselingList =
            viewModel.assessmentListLiveData.value?.data?.entityList?.filter { it.assessedBy.isNullOrBlank() && !it.counselorAssessment.isNullOrBlank() }
        if (counselingList.isNullOrEmpty())
            return
        else {
            val items = ArrayList<ResultModel>()
            counselingList.forEach {
                items.add(
                    ResultModel(
                        id = it.id,
                        counselorAssessment = it.counselorAssessment
                    )
                )
            }
            val request = AssessmentResultModel(
                counselorAssessments = items,
                patientReference = viewModel.patientReference,
                memberReference = viewModel.memberReference,
                visitId = viewModel.encounterReference,
                patientVisitId = viewModel.encounterReference,
                assessedBy = NCDMRUtil.currentUserId(),
                assessedByDisplay = NCDMRUtil.getUserName(),
                assessedDate = DateUtils.getTodayDateDDMMYYYY()
            )
            viewModel.updateAssessment(request, false)
        }
    }
}