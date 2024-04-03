package com.medtroniclabs.spice.ui.assessment.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.setSuccess
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.EntityMapper
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.databinding.FragmentAssessmentRmnchBinding
import com.medtroniclabs.spice.db.entity.MemberClinicalEntity
import com.medtroniclabs.spice.formgeneration.FormGenerator
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.listener.FormEventListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.model.FormResponse
import com.medtroniclabs.spice.formgeneration.ui.FormResultComposer
import com.medtroniclabs.spice.formgeneration.utility.CheckBoxDialog
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.PlaceOfDelivery
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.visitCount
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AssessmentRMNCHFragment : BaseFragment(), View.OnClickListener,
    FormEventListener {

    private lateinit var binding: FragmentAssessmentRmnchBinding
    private val viewModel: AssessmentViewModel by activityViewModels()
    private lateinit var formGenerator: FormGenerator

    companion object {
        const val TAG = "AssessmentRMNCHChildhoodVisitFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAssessmentRmnchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        getFormDataForWorkflow()
        attachObservers()
        setListener()
    }

    private fun setListener() {
        binding.btnSubmit.safeClickListener(this)
    }

    private fun attachObservers() {
        viewModel.formLayoutsLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let { data ->
                        formGenerator.populateViews(data.formLayout)
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }

                ResourceState.LOADING -> {
                    showProgress()
                }
            }
        }
        viewModel.facilitySpinnerLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let { data ->
                        formGenerator.spinnerDataInjection(
                            data,
                            EntityMapper.getResultSpinnerMapList(data)
                        )
                    }
                }

                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }

        viewModel.memberClinicalLiveData.observe(viewLifecycleOwner){data ->
            data?.clinicalDate?.let {date ->
                if (date.isNotEmpty()) {
                    formGenerator.getViewByTag(RMNCH.lastMenstrualPeriod + formGenerator.rootSuffix)?.let {
                        it.gone()
                    }
                }
            }
        }
    }

    private fun getFormDataForWorkflow() {
        viewModel.getFormData(RMNCH.RMNCHChildHoodVisit)
    }

    private fun initView() {
        replaceFragmentInId<BioDataFragment>(
            binding.bioDataFragmentContainer.id,
            tag = BioDataFragment.TAG
        )
        formGenerator = FormGenerator(
            requireContext(), binding.llForm, null, this, binding.scrollView,
            translate = false
        )
        showRespectiveWorkflow()
    }

    private fun showRespectiveWorkflow() {
        var resultJsonFileName: String? = null
        when (viewModel.workflowName) {
            RMNCH.ANC -> {
                resultJsonFileName = "rmnch_anc_visit.json"
            }

            RMNCH.ChildHoodVisit -> {
                resultJsonFileName = "rmnch_childhood_visit.json"
            }

            RMNCH.PNC -> {
                resultJsonFileName = "rmnch_pnc_phu_delivery_child.json"
            }
        }
        resultJsonFileName?.let { name ->
            loadJson(name)
        }
    }

    private fun loadJson(resultJsonFileName: String) {
        val objectList = Gson().fromJson(
            CommonUtils.getStringFromAssets(
                resultJsonFileName,
                requireActivity().assets
            ),
            Array<FormLayout>::class.java
        ).asList()

        viewModel.formLayoutsLiveData.setSuccess(FormResponse(objectList, time = 123231231))
    }


    override fun onClick(v: View) {
        when (v.id) {
            binding.btnSubmit.id -> {
                formGenerator.formSubmitAction(v)
            }
        }
    }

    override fun loadLocalCache(id: String, localDataCache: Any, selectedParent: Long?) {
        when (id) {
            PlaceOfDelivery -> {
                if (localDataCache is String) {
                    viewModel.loadDataCacheByType(id, localDataCache)
                }
            }
        }
    }

    override fun onPopulate(targetId: String) {
    }

    override fun onCheckBoxDialogueClicked(
        id: String,
        serverViewModel: FormLayout,
        resultMap: Any?
    ) {
        CheckBoxDialog.newInstance(id, resultMap) { map ->
            formGenerator.validateCheckboxDialogue(id, serverViewModel, map)
        }.show(childFragmentManager, CheckBoxDialog.TAG)
    }

    override fun onInstructionClicked(
        id: String,
        title: String,
        informationList: ArrayList<String>?,
        description: String?
    ) {
    }

    override fun onFormSubmit(resultMap: HashMap<String, Any>?, serverData: List<FormLayout?>?) {
        resultMap?.let { details ->
            val result = serverData?.let {
                FormResultComposer().groupValues(
                    context = requireContext(),
                    serverData = it,
                    details,
                    AssessmentDefinedParams.RMNCH.lowercase()
                )
            }
            result?.second?.let { second ->
                viewModel.workflowName?.let {
                    handlePregnancy(second, workflowName = it)
                }
                viewModel.saveAssessment(second, null)
            }
        }
    }

    override fun onRenderingComplete() {
        viewModel.memberClinicalLiveData.value?.clinicalDate?.let {
            formGenerator.getViewByTag(RMNCH.lastMenstrualPeriod + formGenerator.rootSuffix)?.let {
                it.gone()
            }
        }
    }

    override fun onUpdateInstruction(id: String, selectedId: Any?) {
    }

    override fun onInformationHandling(
        id: String,
        noOfDays: Int,
        enteredDays: Int,
        resultMap: HashMap<String, Any>?
    ) {
    }

    private fun handlePregnancy(details: HashMap<String, Any>, workflowName: String) {
        viewModel.memberDetailsLiveData.value?.data?.apply {
            val pregnancyMap =
                details[AssessmentDefinedParams.RMNCH.lowercase()] as HashMap<String, Any>
            if (pregnancyMap.containsKey(workflowName.lowercase()) && pregnancyMap[workflowName.lowercase()] is Map<*, *>) {
                val map = pregnancyMap[workflowName.lowercase()] as HashMap<String, Any>
                viewModel.memberClinicalLiveData.value?.let {
                    map[visitCount] = it.visitCount + 1
                    map[RMNCH.lastMenstrualPeriod] = it.clinicalDate
                    savePatientClinicalInformation(patientId,workflowName,map,it.id)
                } ?: kotlin.run {
                    map[visitCount] = 1L
                    savePatientClinicalInformation(patientId, workflowName, map)
                }
            }
        }
    }

    private fun savePatientClinicalInformation(
        patientId: String?,
        workflowName: String,
        map: HashMap<String, Any>,
        rowId: Long = 0
    ) {
        patientId?.let { id ->
            getClinicalDateAndVisitCount(map, workflowName)?.let {
                val clinicalEntity = MemberClinicalEntity(
                    id = rowId,
                    patientId = id,
                    type = workflowName,
                    visitCount = it.first,
                    clinicalDate = it.second
                )
                viewModel.savePatientVisitCountByType(clinicalEntity)
            }
        }
    }

    override fun onAgeCheckForPregnancy() {
        TODO("Not yet implemented")
    }

    private fun getClinicalDateAndVisitCount(
        details: HashMap<String, Any>,
        workflowName: String
    ): Pair<Long, String>? {
        return when (workflowName) {
            RMNCH.ANC -> {
                Pair(details[visitCount] as Long, details[RMNCH.lastMenstrualPeriod] as String)
            }

            else -> {
                null
            }
        }
    }
}
