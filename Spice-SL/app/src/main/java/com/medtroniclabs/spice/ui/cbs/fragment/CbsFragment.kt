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
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.ANC_CBS
import com.medtroniclabs.spice.common.DefinedParams.AssessmentId
import com.medtroniclabs.spice.common.DefinedParams.CBS
import com.medtroniclabs.spice.common.DefinedParams.CbsNotifiableCondition
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
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.PNCNeonatal
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
        if (viewModel.motherID != null && viewModel.motherID != -1L && viewModel.workflowName.equals(
                PNCNeonatal,
                ignoreCase = true
            )
        ) {
            viewModel.getPregnancyDetailInformationForMother()
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
                    (map[RmnchNotifiableCondition] as? ArrayList<Map<String, String>>)
                        ?.firstOrNull { it[DefinedParams.Value].equals(deathOfNewborn, true) }
                        .let { filteredValue ->
                            val isStillBirth = (map[birth] as? String).equals(DefinedParams.still_birth, true)
                            if (filteredValue == null && isStillBirth) {
                                map.remove(birth)
                            }
                            singleSelectValueOption(DefinedParams.still_birth, birth, filteredValue != null)
                            binding.btnSubmit.text = getString(if (filteredValue != null) R.string.submit else R.string.next)
                        }
                }

                birth -> {
                    (map[birth] as? String)?.takeIf { it.equals(DefinedParams.still_birth, true) }?.let {
                        showInCheckBox((map[RmnchNotifiableCondition] as? ArrayList<*>) ?: arrayListOf<Any>())
                    } ?: run {
                        (map[RmnchNotifiableCondition] as? ArrayList<*>)?.let { removeInCheckBox(it) }
                    }
                }
            }
        }
    }

    private fun showInCheckBox(resultMap: ArrayList<*>) {
        viewModel.symptomTypeListResponse.value
            ?.firstOrNull { it.value.equals(deathOfNewborn,true) }
            ?.let { symptom ->
                val selectedItemMap = hashMapOf<String, Any>(
                    DefinedParams.ID to symptom._id,
                    DefinedParams.NAME to symptom.symptom
                ).apply {
                    symptom.displayValue?.let { put(DefinedParams.cultureValue, it) }
                    symptom.value?.let { put(DefinedParams.Value, it) }
                }

                val mapList = (resultMap as? ArrayList<HashMap<String, Any>>)?.apply {
                    if (none { (it[DefinedParams.Value] as? String).equals(deathOfNewborn,true) }) add(selectedItemMap)
                } ?: arrayListOf(selectedItemMap)

                viewModel.formLayoutsLiveData.value?.data?.formLayout
                    ?.firstOrNull { it.id == RmnchNotifiableCondition }
                    ?.let { formGenerator.validateCheckboxDialogue(RmnchNotifiableCondition, it, mapList,false) }
            }
    }


    private fun removeInCheckBox(resultMap: ArrayList<*>) {
        (resultMap as? ArrayList<HashMap<String, Any>>)?.let { mapList ->
            // Remove items where the value matches 'deathOfNewborn'
            mapList.removeAll {
                (it[DefinedParams.Value] as? String)?.equals(deathOfNewborn, true) == true
            }
            viewModel.formLayoutsLiveData.value?.data?.formLayout
                ?.firstOrNull { it.id == RmnchNotifiableCondition }
                ?.let { formLayout ->
                    formGenerator.validateCheckboxDialogue(
                        RmnchNotifiableCondition,
                        formLayout,
                        mapList
                    )
                }
        }
    }
    private fun singleSelectValueOption(value: String, key: String, isSelected: Boolean) {
        formGenerator.getViewByTag("${value}_${key}")
            ?.let { view ->
                if (view is TextView) {
                    view.isSelected = isSelected
                    if (isSelected) {
                        view.performClick()
                    }
                }
            }
    }

    private fun attachObservers() {
        viewModel.symptomTypeListResponse.observe(viewLifecycleOwner) { list ->
            if (requireArguments().getBoolean(DeathOfMother, false)) {
                list.firstOrNull { it.value == DeathOfMother }?.let { symptom ->
                    val selectedItemMap = hashMapOf<String, Any>(
                        com.medtroniclabs.spice.formgeneration.config.DefinedParams.ID to symptom._id,
                        com.medtroniclabs.spice.formgeneration.config.DefinedParams.NAME to symptom.symptom
                    ).apply {
                        symptom.displayValue?.let { put(com.medtroniclabs.spice.formgeneration.config.DefinedParams.cultureValue, it) }
                        symptom.value?.let { put(com.medtroniclabs.spice.formgeneration.config.DefinedParams.value, it) }
                    }
                    viewModel.formLayoutsLiveData.value?.data?.formLayout
                        ?.firstOrNull { it.id == RmnchNotifiableCondition }
                        ?.let { formGenerator.validateCheckboxDialogue(RmnchNotifiableCondition, it, arrayListOf(selectedItemMap)) }
                }
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
        val bioFragment = BioDataFragment.newInstance(true)

        childFragmentManager.beginTransaction().replace(
            binding.bioDataFragmentContainer.id, bioFragment
        ).commit()
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
            var memberIdOfMotherForResetPNCANC: Long? = null

            // Navigate after the assessment only if it contains the assessment ID, e.g., ICCM, RMNCH ANC, PNC, or Childhood Visit.
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
                                        val conditions = mutableListOf<String>()
                                        if (value.containsKey(CbsNotifiableCondition)) {
                                            conditions.addAll(viewModel.getFormatedNotifiableCondition(value, CbsNotifiableCondition))
                                        }

                                        if (value.containsKey(RmnchNotifiableCondition)) {
                                            conditions.addAll(viewModel.getFormatedNotifiableCondition(value, RmnchNotifiableCondition))
                                        }

                                        val cbs = values.toMutableMap()
                                        if (conditions.contains(DeathOfMother)) {
                                            cbs[DeathOfMother] = true
                                        }

                                        cbs.remove(CbsNotifiableCondition)
                                        cbs.remove(RmnchNotifiableCondition)
                                        cbs[DefinedParams.NotifiableConditions] = conditions

                                        assessmentDetailsMap[CBS.lowercase()] = cbs
                                    }
                                }
                            }
                        }
                        data.assessmentDetails = gson.toJson(assessmentDetailsMap)
                        val birth = ((resultValue[CBS.lowercase()] as? Map<String, Any>)
                            ?.get(surveillanceDetails) as? Map<String, Any>)
                            ?.get(birth) as? String
                        if (!birth.isNullOrBlank() && !birth.equals(DefinedParams.still_birth,true)) {
                            // If the birth value is "boy" or "girl," the data will not be saved in the database but only in the ViewModel.
                            // Once the member is saved in the CBS activity, all data will be stored in the database(In cbsActivity).
                            // If the birth value is "boy" or "girl," the PNC and ANC will be reset, as the pregnancy flow will conclude after childbirth.
                            viewModel.saveAssessmentCbs(data, resultValue, birth)
                            return
                        }
                        memberIdOfMotherForResetPNCANC = viewModel.motherID.takeIf {
                            it != null && it != -1L && viewModel.workflowName.equals(PNCNeonatal, ignoreCase = true)
                        } ?: data.householdMemberLocalId.takeIf {
                            memberIdOfMotherForResetPNCANC == null && !birth.isNullOrBlank() && birth.equals(DefinedParams.still_birth, ignoreCase = true)
                        }
                    }

                    // Convert HashMap back to JSON and save
                    // The memberIdOfMotherForResetPNCANC will have a value only if the birth value is "still birth"; otherwise, it will be null.
                    // It is used to reset PNC and ANC and update the pregnancy status in the member table.
                    result?.second?.let { resultValue ->
                        viewModel.saveCallResult(data, resultValue, memberIdOfMotherForResetPNCANC)
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
                    if (!birth.isNullOrBlank() && !birth.equals(DefinedParams.still_birth,true)) {
                        viewModel.setBirth(resultValue, referralResult, birth, isDelete,viewModel.memberDetailsLiveData.value?.data?.id)
                        return
                    }
                    if (!birth.isNullOrBlank() && birth.equals(DefinedParams.still_birth, true)) {
                        viewModel.memberDetailsLiveData.value?.data?.id?.let {
                            viewModel.savePatientClinicalInformation(viewModel.getUpdatedPregnancyDetail(it, viewModel.pregnancyDetail,true))
                        }
                    }
                    val rmnchText = rmnchList?.mapNotNull { it[DefinedParams.NAME] as? String }?.toMutableList() ?: mutableListOf()

                    val otherText = ((viewModel.assessmentMap[CBS.lowercase()] as? Map<String, Any>)
                        ?.get(surveillanceDetails) as? Map<String, Any>)
                        ?.get(DefinedParams.OtherNotifiableConditions) as? String

                    val index = rmnchText.indexOfFirst { it.equals(DefinedParams.Other, ignoreCase = true) }

                    if (index != -1 && !otherText.isNullOrBlank()) {
                        rmnchText[index] = "${DefinedParams.Other} ($otherText)"
                    }
                    val finalText = rmnchText.joinToString(", ")
                    if (isDelete) {
                        viewModel.updateMemberDeceasedStatus(
                            viewModel.memberDetailsLiveData.value?.data?.id ?: -1L,
                            false,
                            finalText
                        )
                    }
                    viewModel.saveAssessment(resultValue, referralResult, viewModel.menuId)
                }
            }
        }
    }

    override fun onRenderingComplete() {
        val memberData = viewModel.memberDetailsLiveData.value?.data
        if (requireArguments().getBoolean(DeathOfMother, false) ||
            (memberData?.gender.equals(DefinedParams.female, true) && memberData?.isPregnant == true)
        ) {
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
