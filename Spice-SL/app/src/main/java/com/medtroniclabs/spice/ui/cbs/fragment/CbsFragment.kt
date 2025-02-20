package com.medtroniclabs.spice.ui.cbs.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.AssessmentId
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
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import timber.log.Timber

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
        getFormDataForWorkflow()
        attachObservers()
        setListeners()
        if(requireArguments().getLong(AssessmentId) != 0L) {
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
        )
    }

    private fun attachObservers() {
        viewModel.formLayoutsLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let { data ->
                        formGenerator.populateViews(data.formLayout)
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
    }

    private fun getFormDataForWorkflow() {
        if (requireArguments().getString(MenuConstants.WorkFlowName) != null) {
            viewModel.getFormData(MenuConstants.CBS_MENU_ID)
        } else {
            viewModel.getFormData(MenuConstants.CBS_MENU_ID)
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
        CheckBoxDialog.newInstance(id, resultMap) { map ->
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
        Timber.d("$assessmentId")
        resultMap?.let { details ->
            val gson = Gson()
            val result = serverData?.let {
                FormResultComposer().groupValues(
                    context = requireContext(),
                    serverData = it,
                    details,
                    DefinedParams.CBS.lowercase()
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
                        (resultValue[DefinedParams.CBS.lowercase()] as? HashMap<String, Any>)?.let {
                            if (it.containsKey(DefinedParams.surveillanceDetails)) {
                                val value =
                                    it[DefinedParams.surveillanceDetails] as? HashMap<Any, Any>
                                if (!value.isNullOrEmpty()) {
                                    assessmentDetailsMap[DefinedParams.CBS.lowercase()] = value
                                }
                            }
                        }
                    }
                    // Convert HashMap back to JSON and save
                    data.assessmentDetails = gson.toJson(assessmentDetailsMap)
                    result?.second?.let { resultValue ->
                        viewModel.saveCallResult(data, resultValue)
                    }
                }
            } else {
                val referralResult = ReferralResultGenerator().calculateCBSReferralResult(details)
                result?.second?.let { resultValue ->
                    viewModel.saveAssessment(resultValue, referralResult, viewModel.menuId)
                }
            }
        }
    }

    override fun onRenderingComplete() {

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
