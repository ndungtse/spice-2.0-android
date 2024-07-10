package com.medtroniclabs.spice.ui.mypatients.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams.StateOfPerineum
import com.medtroniclabs.spice.common.DefinedParams.Tear
import com.medtroniclabs.spice.data.LabourDeliveryMetaEntity
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.databinding.FragmentMotherBinding
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.TagListCustomView
import com.medtroniclabs.spice.ui.medicalreview.labourDelivery.LabourDeliveryViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums

class MotherFragment : BaseFragment() {

    private lateinit var binding: FragmentMotherBinding
    private lateinit var cgGeneralConditionOfMother: TagListCustomView
    private lateinit var cgSignSymptomsObserved: TagListCustomView
    private lateinit var cgRiskFactors: TagListCustomView
    private lateinit var cgStatus: TagListCustomView
    private val viewModel: LabourDeliveryViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMotherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        initializeChipItem()
        attachObserver()
        initializeStateOfPerineumLabel()
        initializeTearLabel()
    }

    private fun initListeners() {
        binding.tvNumber.doAfterTextChanged {
            val ttNoOfDosage = it?.trim().toString()
            if(ttNoOfDosage.isNotEmpty()) {
                viewModel.motherTTDosageSoFar = ttNoOfDosage
                viewModel.validateSubmitButtonState()
            }else {
                viewModel.motherTTDosageSoFar = null
                viewModel.validateSubmitButtonState()
            }
        }
    }

    private fun attachObserver() {
        viewModel.labourDeliveryMetaList.observe(viewLifecycleOwner) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    resource.data?.let { listItems ->
                        initializeSignsSymptomsItems(listItems)
                        initializeMotherConditionItems(listItems)
                        initializeRiskFactorsItems(listItems)
                        initializeToMotherStatusItems(listItems)
                    }
                    hideProgress()
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }
    }

    private fun initializeSignsSymptomsItems(listItems: List<LabourDeliveryMetaEntity>) {
        val chipItemList = ArrayList<ChipViewItemModel>()
        listItems.filter { it.type == MedicalReviewTypeEnums.Mother.name }.forEach {
            chipItemList.add(
                ChipViewItemModel(
                    id = it.id,
                    name = it.name,
                    type = it.type
                )
            )
        }
        cgSignSymptomsObserved.addChipItemList(chipItemList)
    }

    private fun initializeToMotherStatusItems(listItems: List<LabourDeliveryMetaEntity>) {
        val chipItemList = ArrayList<ChipViewItemModel>()
        listItems.filter { it.category == MedicalReviewTypeEnums.MotherDeliveryStatus.name }.forEach {
            chipItemList.add(
                ChipViewItemModel(
                    id = it.id,
                    name = it.name,
                    type = it.type
                )
            )
        }
        cgStatus.addChipItemList(chipItemList)
    }

    private fun initializeMotherConditionItems(listItems: List<LabourDeliveryMetaEntity>) {
        val chipItemList = ArrayList<ChipViewItemModel>()
        listItems.filter { it.category == MedicalReviewTypeEnums.ConditionOfMother.name }.forEach {
            chipItemList.add(
                ChipViewItemModel(
                    id = it.id,
                    name = it.name,
                    type = it.type
                )
            )
        }
        cgGeneralConditionOfMother.addChipItemList(chipItemList)
    }

    private fun initializeRiskFactorsItems(listItems: List<LabourDeliveryMetaEntity>) {
        val chipItemList = ArrayList<ChipViewItemModel>()
        listItems.filter { it.category == MedicalReviewTypeEnums.RiskFactors.name }.forEach {
            chipItemList.add(
                ChipViewItemModel(
                    id = it.id,
                    name = it.name,
                    type = it.type
                )
            )
        }
        cgRiskFactors.addChipItemList(chipItemList)
    }


    private fun initializeChipItem() {
        cgGeneralConditionOfMother =
            TagListCustomView(binding.root.context, binding.cgGeneralConditionOfMother) { name, _, _ ->
                viewModel.motherGeneralCondition = name
                viewModel.validateSubmitButtonState()
            }
        cgRiskFactors = TagListCustomView(binding.root.context, binding.cgRiskFactors) { _, _, _ ->
            viewModel.motherRiskFactors = cgRiskFactors.getSelectedTags()
            viewModel.validateSubmitButtonState()
        }
        cgStatus =
            TagListCustomView(binding.root.context, binding.cgStatus) { _, _, _ ->
                viewModel.motherStatus = cgStatus.getSelectedTags()
                viewModel.validateSubmitButtonState()
            }
        cgSignSymptomsObserved =
            TagListCustomView(binding.root.context, binding.cgSignsSymptomsObserved) { _, _, _ ->
                viewModel.motherSignsAndSymptoms = cgSignSymptomsObserved.getSelectedTags()
                viewModel.validateSubmitButtonState()
            }
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
                Pair(StateOfPerineum, null),
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
                Pair(Tear, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                singleSelectionCallback
            )
            binding.tearlayout.addView(view)
        }
    }

    private var singleSelectionCallback: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            saveSelectedOptionValue(selectedID)
            viewModel.validateSubmitButtonState()
        }

    private fun saveSelectedOptionValue(selectedID: Any?) {
        viewModel.perineumStateMap[StateOfPerineum] = selectedID as String
        if (selectedID.toString() == getString(R.string.tear)) {
            binding.groupTear.isVisible = true
        } else if (selectedID.toString() == getString(R.string.episotomy)) {
            binding.groupTear.isVisible = false
        } else {
            viewModel.perineumStateMap[Tear] = selectedID
        }
    }

    private fun getTearFlowData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(CommonUtils.getOptionMap(getString(R.string.deg_1), getString(R.string.deg_1)))
        flowList.add(CommonUtils.getOptionMap(getString(R.string.deg_2), getString(R.string.deg_2)))
        flowList.add(CommonUtils.getOptionMap(getString(R.string.deg_3), getString(R.string.deg_3)))
        flowList.add(CommonUtils.getOptionMap(getString(R.string.deg_4), getString(R.string.deg_4)))
        return flowList
    }

    private fun getMotherFlowData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(CommonUtils.getOptionMap(getString(R.string.episotomy), getString(R.string.episotomy)))
        flowList.add(CommonUtils.getOptionMap(getString(R.string.tear), getString(R.string.tear)))
        return flowList
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