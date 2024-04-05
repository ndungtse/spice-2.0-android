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
import com.medtroniclabs.spice.common.DefinedParams.StateOfPerineum
import com.medtroniclabs.spice.common.DefinedParams.Tear
import com.medtroniclabs.spice.databinding.FragmentMotherBinding
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.TagListCustomView
import com.medtroniclabs.spice.ui.mypatients.viewmodel.MedicalReviewBaseViewModel

class MotherFragment : BaseFragment() {

    private lateinit var binding: FragmentMotherBinding
    private lateinit var cgGeneralConditionOfMother: TagListCustomView
    private lateinit var cgSignSymptomsObserved: TagListCustomView
    private lateinit var cgRiskFactors: TagListCustomView
    private lateinit var cgStatus: TagListCustomView
    private val viewModel: MedicalReviewBaseViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMotherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeChipItem()
        initializeStateOfPerineumLabel()
        initializeTearLabel()
    }

    private fun initializeChipItem() {
        cgGeneralConditionOfMother =
            TagListCustomView(binding.root.context, binding.cgGeneralConditionOfMother)
        cgGeneralConditionOfMother.addChipItemList(viewModel.getGeneralConditionOfMother())
        cgSignSymptomsObserved =
            TagListCustomView(binding.root.context, binding.cgSignsSymptomsObserved)
        cgSignSymptomsObserved.addChipItemList(viewModel.getSignSymptomsObservedMother())

        cgRiskFactors = TagListCustomView(binding.root.context, binding.cgRiskFactors)
        cgRiskFactors.addChipItemList(viewModel.getRiskFactor())
        cgStatus =
            TagListCustomView(binding.root.context, binding.cgStatus)
        cgStatus.addChipItemList(viewModel.getStatusMother())
    }

    companion object {
        const val TAG = "MotherFragment"

        fun newInstance(): MotherFragment {
            return MotherFragment()
        }
    }
    
    private fun initializeStateOfPerineumLabel() {
        getMotherFlowData().let {
            val view = SingleSelectionCustomView(binding.root.context)
            view.tag = TAG
            view.addViewElements(
                it,
                false,
                viewModel.perineumStateMap,
                Pair(StateOfPerineum,null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                singleSelectionCallback
            )
            binding.selectionGroup.addView(view)
        }
    }

    private fun initializeTearLabel() {
        getTearFlowData().let {
            val view = SingleSelectionCustomView(binding.root.context)
            view.tag = TAG
            view.addViewElements(
                it,
                false,
                viewModel.perineumStateMap,
                Pair(Tear,null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                singleSelectionCallback
            )
            binding.tearlayout.addView(view)
        }
    }

    private var singleSelectionCallback: ((selectedID: Any?, elementId: Pair<String,String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            saveSelectedOptionValue(selectedID)
        }

    private fun saveSelectedOptionValue(selectedID: Any?) {
        viewModel.perineumStateMap[StateOfPerineum] = selectedID as String
        if (selectedID.toString() == getString(R.string.tear)) {
            binding.groupTear.isVisible = true
        } else if (selectedID.toString() == getString(R.string.episotomy)) {
            binding.groupTear.isVisible = false
        }
        else {
            viewModel.perineumStateMap[Tear] = selectedID as String
        }
    }

    private fun getTearFlowData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String,Any>>()
        flowList.add(getOptionMap(getString(R.string.deg_1)))
        flowList.add(getOptionMap(getString(R.string.deg_2)))
        flowList.add(getOptionMap(getString(R.string.deg_3)))
        flowList.add(getOptionMap(getString(R.string.deg_4)))
        return flowList
    }

    private fun getMotherFlowData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String,Any>>()
        flowList.add(getOptionMap(getString(R.string.episotomy)))
        flowList.add(getOptionMap(getString(R.string.tear)))
        return flowList
    }
    private fun getOptionMap(value: String): HashMap<String, Any> {
        val map = HashMap<String,Any>()
        map[DefinedParams.ID] = value
        map[DefinedParams.NAME] = value
        return map
    }

    fun validateInput(): Boolean {
        var isValid = false
        if (validateTagView(
                cgGeneralConditionOfMother,
                binding.cgGeneralConditionOfMotherError
            ) && validateTagView(cgStatus, binding.cgStatusError)
        ) {
            isValid = true
        }
        return isValid
    }

    private fun validateTagView(
        tagView: TagListCustomView,
        textView: AppCompatTextView
    ): Boolean {
        var isValidOrNot = true
        if (tagView.getSelectedTags().isEmpty()) {
            textView.isVisible = true
            isValidOrNot = false
        } else {
            textView.isVisible = false
        }
        return isValidOrNot
    }
}