package com.medtroniclabs.spice.ui.mypatients.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.FragmentNeonateBinding
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.TagListCustomView
import com.medtroniclabs.spice.ui.mypatients.viewmodel.MedicalReviewBaseViewModel

class NeonateFragment : BaseFragment() {

    private lateinit var binding: FragmentNeonateBinding
    private lateinit var cgNeonateOutcome: TagListCustomView
    private lateinit var cgSignSymptomsObserved: TagListCustomView
    private val viewModel: MedicalReviewBaseViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNeonateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeTagView()
        initializeGenderLabel()
        initializeStateOfBabyLabel()
    }

    private fun initializeStateOfBabyLabel() {
        getStateOfBabyFlowData().let {
            val view = SingleSelectionCustomView(binding.root.context)
            view.tag = TAG
            view.addViewElements(
                it,
                false,
                viewModel.stateOfBaby,
                Pair(DefinedParams.StateOfBaby,null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                stateOfBabySingleSelectionCallback
            )
            binding.stateOfBabyGroup.addView(view)
        }
    }

    companion object {
        const val TAG = "NeonateFragment"

        fun newInstance(): NeonateFragment {
            return NeonateFragment()
        }
    }

    private fun initializeGenderLabel() {
        getGenderFlowData().let {
            val view = SingleSelectionCustomView(binding.root.context)
            view.tag = TAG
            view.addViewElements(
                it,
                false,
                viewModel.genderFlow,
                Pair(DefinedParams.Gender,null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                genderSingleSelectionCallback
            )
            binding.genderLabelGroup.addView(view)
        }
    }

    private var genderSingleSelectionCallback: ((selectedID: Any?, elementId: Pair<String,String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.genderFlow[DefinedParams.Gender] = selectedID as String
        }

    private var stateOfBabySingleSelectionCallback: ((selectedID: Any?, elementId: Pair<String,String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.stateOfBaby[DefinedParams.StateOfBaby] = selectedID as String
        }

    private fun getGenderFlowData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(getOptionMap(getString(R.string.male)))
        flowList.add(getOptionMap(getString(R.string.female)))
        return flowList
    }

    private fun getStateOfBabyFlowData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(getOptionMap(getString(R.string.normal)))
        flowList.add(getOptionMap(getString(R.string.abnormal)))
        return flowList
    }

    private fun getOptionMap(value: String): Map<String, Any> {
        val map = HashMap<String, Any>()
        map[DefinedParams.ID] = value
        map[DefinedParams.NAME] = value
        return map
    }

    private fun initializeTagView() {
        cgNeonateOutcome = TagListCustomView(binding.root.context, binding.cgNeonateOutcome)
        cgSignSymptomsObserved =
            TagListCustomView(binding.root.context, binding.cgSignsSymptomsObserved)
        cgNeonateOutcome.addChipItemList(viewModel.getNeonateOutcome())
        cgSignSymptomsObserved.addChipItemList(viewModel.getSignSymptomsObserved())
    }

    fun validateInput(): Boolean {
        var isValid = false
        if (validateCgNeonateOutcome() && validateTextView(
                viewModel.genderFlow,
                binding.tvGenderError
            ) && validateTextView(viewModel.stateOfBaby, binding.tvStateOfBabyError)
        ) {
            isValid = true
        }
        return isValid
    }

    private fun validateTextView(
        flowData: HashMap<String, Any>,
        textView: AppCompatTextView
    ): Boolean {
        var isValidOrNot = true
        if (flowData.isEmpty()) {
            textView.isVisible = true
            isValidOrNot = false
        } else {
            textView.isVisible = false
        }
        return isValidOrNot
    }

    private fun validateCgNeonateOutcome(): Boolean {
        var isValidOrNot = true
        if (cgNeonateOutcome.getSelectedTags().isEmpty()) {
            binding.cgNeonateOutcomeError.isVisible = true
            isValidOrNot = false
        } else {
            binding.cgNeonateOutcomeError.isVisible = false
        }
        return isValidOrNot
    }
}