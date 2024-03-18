package com.medtroniclabs.spice.ui.assessment.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.databinding.FragmentAssessmentOtherSymptomsBinding
import com.medtroniclabs.spice.formgeneration.FormGenerator
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.listener.FormEventListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.FormResultComposer
import com.medtroniclabs.spice.formgeneration.utility.CheckBoxDialog
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.MenuConstants.OTHER_SYMPTOMS
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.OtherSymptoms
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AssessmentOtherSymptomsFragment : BaseFragment(), FormEventListener, View.OnClickListener {

    private lateinit var binding: FragmentAssessmentOtherSymptomsBinding
    private lateinit var formGenerator: FormGenerator
    private val viewModel: AssessmentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAssessmentOtherSymptomsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getMemberDetailsById()
        initViews()
        getFormDataForWorkflow()
        initializeFormGenerator()
        setListeners()
        attachObservers()
    }

    private fun initViews() {
        replaceFragmentInId<BioDataFragment>(
            binding.bioDataFragmentContainer.id,
            tag = BioDataFragment.TAG
        )
    }

    private fun getFormDataForWorkflow() {
        viewModel.getFormData(OTHER_SYMPTOMS)
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
                    showProgress()
                }
            }
        }
    }

    private fun setListeners() {
        binding.btnSubmit.safeClickListener(this)
    }

    private fun initializeFormGenerator() {
        formGenerator = FormGenerator(
            requireContext(), binding.llForm, null, this, binding.scrollView,
            translate = false
        )

        val objectList = Gson().fromJson(
            CommonUtils.getStringFromAssets(
                "other_symptoms.json",
                requireActivity().assets
            ),
            Array<FormLayout>::class.java
        ).asList()
        viewModel.formLayout = objectList
        formGenerator.populateViews(objectList)
    }


    override fun loadLocalCache(id: String, localDataCache: Any, selectedParent: Long?) {
        TODO("Not yet implemented")
    }

    override fun onPopulate(targetId: String) {
        TODO("Not yet implemented")
    }

    override fun onCheckBoxDialogueClicked(
        id: String,
        serverViewModel: FormLayout,
        resultMap: Any?
    ) {
        CheckBoxDialog.newInstance(OtherSymptoms.lowercase(), resultMap) { resultMap ->
            formGenerator.validateCheckboxDialogue(id, serverViewModel, resultMap)
        }.show(childFragmentManager, CheckBoxDialog.TAG)
    }

    override fun onInstructionClicked(
        id: String,
        title: String,
        informationList: ArrayList<String>?,
        description: String?
    ) {
        TODO("Not yet implemented")
    }

    override fun onFormSubmit(resultMap: HashMap<String, Any>?, serverData: List<FormLayout?>?) {
        resultMap?.let { details ->
            val result = serverData?.let {
                FormResultComposer().groupValues(
                    context = requireContext(),
                    serverData = it,
                    details,
                    OtherSymptoms.lowercase()
                )
            }
            result?.second?.let {
                StringConverter.convertGivenMapToString(it)?.let { resultData ->
                    viewModel.saveAssessment(resultData)
                }
            }
        }
    }

    override fun onRenderingComplete() {

    }

    override fun onUpdateInstruction(id: String, selectedId: Any?) {

    }

    override fun onInformationHandling(id: String, noOfDays: Int, enteredDays: Int) {

    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.btnSubmit.id -> {
                formGenerator.formSubmitAction(view)
            }
        }
    }
}