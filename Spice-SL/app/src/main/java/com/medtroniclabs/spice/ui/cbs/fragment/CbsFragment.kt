package com.medtroniclabs.spice.ui.cbs.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.ANC_CBS
import com.medtroniclabs.spice.common.DefinedParams.AssessmentId
import com.medtroniclabs.spice.common.DefinedParams.CBS
import com.medtroniclabs.spice.common.DefinedParams.birth
import com.medtroniclabs.spice.common.DefinedParams.RmnchNotifiableCondition
import com.medtroniclabs.spice.common.DefinedParams.surveillanceDetails
import com.medtroniclabs.spice.data.model.RecommendedDosageListModel
import com.medtroniclabs.spice.databinding.FragmentAssessmentBinding
import com.medtroniclabs.spice.formgeneration.FormGenerator
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.listener.FormEventListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.FormResultComposer
import com.medtroniclabs.spice.formgeneration.utility.CheckBoxDialog
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.assessment.fragment.BioDataFragment
import com.medtroniclabs.spice.ui.assessment.referrallogic.ReferralResultGenerator
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.ANC
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.ChildHoodVisit
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.DeathOfMother
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.deathOfNewborn
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel

class CbsFragment : BaseFragment(), FormEventListener, View.OnClickListener {
    private lateinit var binding: FragmentAssessmentBinding
    private lateinit var formGenerator: FormGenerator
    private val viewModel: AssessmentViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAssessmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "CbsFragment"

        fun newInstance() = CbsFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initFormView()
        attachObservers()
        setListeners()
        if (requireArguments().getLong(AssessmentId) != 0L) {
            viewModel.getAssessmentDetailsById(requireArguments().getLong(AssessmentId))
        }
    }

    private fun setListeners() {
        binding.btnSubmit.safeClickListener(this)
    }

    private fun initFormView() {
        formGenerator = FormGenerator(
            requireContext(), binding.llForm, null, this, binding.scrollView,
            translate = false
        ) { map, id ->
            when (id) {
                RmnchNotifiableCondition -> {
                    if (map.containsKey(RmnchNotifiableCondition)) {
                        val value = map[RmnchNotifiableCondition] as? ArrayList<Map<String, String>>
                        val filteredValues = value?.firstOrNull {
                            it[DefinedParams.Value].equals(
                                deathOfNewborn,
                                true
                            )
                        }
                        if (filteredValues.isNullOrEmpty()) {
                            formGenerator.getViewByTag(birth + formGenerator.rootSuffix)
                                ?.visible()
                            binding.btnSubmit.text = getString(R.string.next)
                        } else {
                            binding.btnSubmit.text = getString(R.string.submit)
                            formGenerator.getViewByTag(birth + formGenerator.rootSuffix)?.gone()
                            viewModel.formLayoutsLiveData.value?.data?.formLayout
                                ?.firstOrNull { it.id.equals(birth, true) }
                                ?.apply {
                                    val removeValue = map[birth] as? String
                                    optionsList?.firstOrNull {
                                        (it[DefinedParams.id] as? String).equals(
                                            removeValue,
                                            true
                                        )
                                    }
                                        ?.let {
                                            map.remove(birth)
                                            singleSelectValueOption(
                                                it[DefinedParams.id] as? String ?: "", birth
                                            )
                                        }
                                }
                        }
                    }
                }
            }
        }
    }

    private fun singleSelectValueOption(value: String, key: String) {
        formGenerator.getViewByTag("${value}_${key}")
            ?.let { view ->
                if (view is TextView) {
                    view.isSelected = false
                }
            }
    }

    private fun attachObservers() {
        viewModel.symptomTypeListResponse.observe(viewLifecycleOwner) { list ->
            list.firstOrNull { it.value == DeathOfMother }?.let { symptom ->
                val selectedItemMap = hashMapOf<String, Any>(
                    com.medtroniclabs.spice.formgeneration.config.DefinedParams.ID to symptom._id,
                    com.medtroniclabs.spice.formgeneration.config.DefinedParams.NAME to symptom.symptom
                ).apply {
                    symptom.displayValue?.let { put(com.medtroniclabs.spice.formgeneration.config.DefinedParams.cultureValue, it) }
                    symptom.value?.let { put(com.medtroniclabs.spice.formgeneration.config.DefinedParams.value, it) }
                }

                viewModel.formLayoutsLiveData?.value?.data?.formLayout
                    ?.firstOrNull { it.id == RmnchNotifiableCondition }
                    ?.let { formGenerator.validateCheckboxDialogue(RmnchNotifiableCondition, it, arrayListOf(selectedItemMap)) }
            }
        }
        viewModel.formLayoutsLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let { data ->
                        if (arguments?.getBoolean(deathOfNewborn) == true) {
                            formGenerator.populateViews(data.formLayout.filter { it.id != birth })
                        } else {
                            formGenerator.populateViews(data.formLayout)
                        }
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }
        viewModel.getAssessmentDetails.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }
        viewModel.triggerGetForm.observe(viewLifecycleOwner) {
            getFormDataForWorkflow()
        }
    }

    private fun getFormDataForWorkflow() {
        val workflowName = requireArguments().getString(MenuConstants.WorkFlowName)
        val memberData = viewModel.memberDetailsLiveData.value?.data
        when {
            workflowName.equals(ChildHoodVisit,true) -> viewModel.getFormData(
                MenuConstants.CBS_MENU_ID
            )
            workflowName.equals(RMNCH.PNCNeonatal,true) -> viewModel.getFormData(
                MenuConstants.CBS_MENU_ID
            )
            workflowName.equals(ANC, true) -> viewModel.getFormData(ANC_CBS)
            memberData?.gender.equals(DefinedParams.male, true) -> viewModel.getFormData(
                MenuConstants.CBS_MENU_ID
            )

            memberData?.gender.equals(
                DefinedParams.female,
                true
            ) && memberData?.isPregnant == true -> {
                viewModel.getFormData(ANC_CBS)
            }

            memberData?.gender.equals(DefinedParams.female, true) -> {
                viewModel.getFormData(MenuConstants.CBS_MENU_ID)
            }
        }
    }

    private fun initViews() {
        replaceFragmentInId<BioDataFragment>(
            binding.bioDataFragmentContainer.id,
            tag = BioDataFragment.TAG
        )
    }

    override fun loadLocalCache(id: String, localDataCache: Any, selectedParent: Long?) {

    }

    override fun onPopulate(targetId: String) {

    }

    override fun onCheckBoxDialogueClicked(
        id: String,
        serverViewModel: FormLayout,
        resultMap: Any?
    ) {
        val value = if (requireArguments().getBoolean(DeathOfMother, false)) {
            listOf(Pair(DeathOfMother,false))
        } else {
            listOf()
        }
        CheckBoxDialog.newInstance(id, resultMap, autoPopulate = value, title = getString(R.string.notifiable_conditions)) { map ->
            formGenerator.validateCheckboxDialogue(id, serverViewModel, map)
        }.show(childFragmentManager, CheckBoxDialog.TAG)
    }

    override fun onInstructionClicked(
        id: String,
        title: String,
        informationList: ArrayList<String>?,
        description: String?,
        dosageListModel: ArrayList<RecommendedDosageListModel>?
    ) {

    }

    override fun onFormSubmit(resultMap: HashMap<String, Any>?, serverData: List<FormLayout?>?) {
        val assessmentId = requireArguments().getLong(AssessmentId)
        resultMap?.let { details ->
            val gson = Gson()
            val result = serverData?.let {
                FormResultComposer().groupValues(
                    context = requireContext(),
                    serverData = it,
                    details,
                    CBS.lowercase()
                )
            }
            if (assessmentId != 0L) {
                viewModel.getAssessmentDetails.value?.data?.let { data ->
                    // Convert JSON String to HashMap
                    val assessmentDetailsJson = data.assessmentDetails
                    val type = object : TypeToken<HashMap<String, Any>>() {}.type
                    val assessmentDetailsMap: HashMap<String, Any> =
                        gson.fromJson(assessmentDetailsJson, type) ?: hashMapOf()
                    // Add result to HashMap if not null
                    result?.second?.let { resultValue ->
                        (resultValue[CBS.lowercase()] as? HashMap<String, Any>)?.let {
                            if (it.containsKey(surveillanceDetails)) {
                                val value =
                                    it[surveillanceDetails] as? HashMap<Any, Any>
                                if (!value.isNullOrEmpty()) {
                                    value.takeIf { value -> value.isNotEmpty() }?.let { values ->
                                        val notifiableCondition =
                                            (values[DefinedParams.CbsNotifiableCondition] as? ArrayList<Map<String, Any>>)
                                                ?: (values[RmnchNotifiableCondition] as? ArrayList<Map<String, Any>>)
                                                ?: arrayListOf()
                                        assessmentDetailsMap[CBS.lowercase()] =
                                            values.toMutableMap().apply {
                                                remove(DefinedParams.CbsNotifiableCondition)
                                                remove(RmnchNotifiableCondition)
                                                put(
                                                    DefinedParams.NotifiableConditions,
                                                    notifiableCondition
                                                )
                                            }
                                    }
                                }
                            }
                        }
                        data.assessmentDetails = gson.toJson(assessmentDetailsMap)
                        val birth = ((resultValue[CBS.lowercase()] as? Map<String, Any>)
                            ?.get(surveillanceDetails) as? Map<String, Any>)
                            ?.get(birth) as? String
                        if (!birth.isNullOrBlank()) {
                            viewModel.saveAssessmentCbs(data, resultValue, birth)
                            return
                        }
                    }
                    // Convert HashMap back to JSON and save

                    result?.second?.let { resultValue ->
                        viewModel.saveCallResult(data, resultValue)
                    }
                }
            } else {
                val referralResult = ReferralResultGenerator().calculateCBSReferralResult(details)
                result?.second?.let { resultValue ->
                    val rmnchList = ((resultValue[CBS.lowercase()] as? Map<String, Any>)
                        ?.get(surveillanceDetails) as? Map<String, Any>)
                        ?.get(RmnchNotifiableCondition) as? List<Map<String, Any>>
                    val isDelete = rmnchList?.any {
                        it[DefinedParams.Value]?.toString().equals(DeathOfMother, true)
                    } == true

                    val birth = ((resultValue[CBS.lowercase()] as? Map<String, Any>)
                        ?.get(surveillanceDetails) as? Map<String, Any>)
                        ?.get(birth) as? String
                    if (!birth.isNullOrBlank()) {
                        viewModel.setBirth(resultValue, referralResult, birth, isDelete)
                        return
                    }
                    if (isDelete) {
                        viewModel.updateMemberDeceasedStatus(
                            viewModel.memberDetailsLiveData.value?.data?.id ?: -1L, false
                        )
                    }
                    viewModel.saveAssessment(resultValue, referralResult, viewModel.menuId)
                }
            }
        }
    }

    override fun onRenderingComplete() {
        if (requireArguments().getBoolean(DeathOfMother, false)) {
            viewModel.getSymptomListByType(RmnchNotifiableCondition)
        }
    }

    override fun onUpdateInstruction(id: String, selectedId: Any?) {

    }

    override fun onInformationHandling(
        id: String,
        noOfDays: Int,
        enteredDays: Int?,
        resultMap: HashMap<String, Any>?
    ) {

    }

    override fun onAgeCheckForPregnancy() {

    }

    override fun handleMandatoryCondition(serverData: FormLayout?) {

    }


    override fun onClick(view: View) {
        when (view.id) {
            binding.btnSubmit.id -> {
                withLocationCheck({
                    viewModel.fetchCurrentLocation(requireContext())
                    formGenerator.formSubmitAction(view)
                })
            }
        }
    }

    fun getCurrentAnsweredStatus(): Boolean {
        return formGenerator.getResultMap().isNotEmpty()
    }

    override fun onAgeUpdateListener(
        age: Int,
        serverData: List<FormLayout?>?,
        resultHashMap: HashMap<String, Any>
    ) {
    }
}
