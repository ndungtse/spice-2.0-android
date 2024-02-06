package com.medtroniclabs.spice.ui.assessment.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.safeClickListener
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.CommonUtils.getDuration
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.databinding.FragmentAssessmentBinding
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.formgeneration.FormGenerator
import com.medtroniclabs.spice.formgeneration.extension.capitalizeFirstChar
import com.medtroniclabs.spice.formgeneration.listener.FormEventListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.FormResultComposer
import com.medtroniclabs.spice.formgeneration.utility.CheckBoxDialog
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AssessmentICCMFragment : Fragment(), FormEventListener, View.OnClickListener {

    private lateinit var binding: FragmentAssessmentBinding
    private lateinit var formGenerator: FormGenerator
    private val viewModel: AssessmentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAssessmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getMemberDetailsById()
        initializeFormGenerator()
        setListeners()
        viewModel.insertSignsAndSymptoms()
        attachObservers()
    }

    private fun setListeners() {
        binding.btnSubmit.safeClickListener(this)
    }

    private fun attachObservers() {
        viewModel.memberDetailsLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    (activity as? BaseActivity)?.showLoading()
                }

                ResourceState.SUCCESS -> {
                    (activity as? BaseActivity)?.hideLoading()
                    showPatientBioData(resourceState.data)
                }

                ResourceState.ERROR -> {
                    (activity as? BaseActivity)?.hideLoading()
                }
            }
        }
    }

    private fun initializeFormGenerator() {
        formGenerator = FormGenerator(
            requireContext(), binding.llForm, null, this, binding.scrollView,
            translate = false
        )

        val objectList = Gson().fromJson(
            CommonUtils.getStringFromAssets(
                "iccm.json",
                requireActivity().assets
            ),
            Array<FormLayout>::class.java
        ).asList()
        viewModel.formLayout = objectList
        formGenerator.populateViews(objectList)
    }

    companion object {
        val TAG = "AssessmentICCMFragment"
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
        CheckBoxDialog.newInstance(id, resultMap) { resultMap ->
            formGenerator.validateCheckboxDialogue(id, serverViewModel, resultMap)
        }.show(childFragmentManager, CheckBoxDialog.TAG)
    }

    override fun onInstructionClicked(
        id: String,
        title: String,
        informationList: ArrayList<String>?,
        description: String?
    ) {
    }

    private fun showPatientBioData(data: HouseholdMemberEntity?) {
        data?.let {
            binding.nationalId.tvKey.text = getString(R.string.national_id)
            binding.nationalId.tvValue.text = data.nationalId
            binding.patientName.tvKey.text = getString(R.string.name)
            binding.patientName.tvValue.text = data.name.capitalizeFirstChar()
            binding.gender.tvKey.text = getString(R.string.gender)
            binding.gender.tvValue.text = data.gender.capitalizeFirstChar()
            binding.dobAge.tvKey.text = getString(R.string.age)
            binding.dobAge.tvValue.text = getDuration(data.age, requireContext())
        }
    }

    override fun onFormSubmit(resultMap: HashMap<String, Any>?, serverData: List<FormLayout?>?) {
        resultMap?.let { details ->
            val result = serverData?.let {
                FormResultComposer().groupValues(
                    context = requireContext(),
                    serverData = it,
                    details
                )
            }
            result?.second?.let {
                StringConverter.convertGivenMapToString(it)?.let { resultData ->
                    viewModel.saveAssessment(resultData)
                }
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.btnSubmit.id -> {
                formGenerator.formSubmitAction(view)
            }
        }
    }
}