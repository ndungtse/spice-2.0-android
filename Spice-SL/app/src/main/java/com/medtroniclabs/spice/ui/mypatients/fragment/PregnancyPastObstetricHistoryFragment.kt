package com.medtroniclabs.spice.ui.mypatients.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.databinding.FragmentPregnancyPastObstetricHistoryBinding
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.mappingkey.HouseHoldRegistration.yes
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.TagListCustomView
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.mypatients.activity.MotherNeonateANCActivity
import com.medtroniclabs.spice.ui.mypatients.viewmodel.MotherNeonateANCViewModel

class PregnancyPastObstetricHistoryFragment : BaseFragment() {

    private lateinit var binding: FragmentPregnancyPastObstetricHistoryBinding
    private lateinit var complaintsTagView: TagListCustomView
    private val viewModel: MotherNeonateANCViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPregnancyPastObstetricHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "PregnancyPastObstetricHistoryFragment"
        fun newInstance() =
            PregnancyPastObstetricHistoryFragment()
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // Not needed for your use case
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // Not needed for your use case
        }

        override fun afterTextChanged(s: Editable?) {
            // Call the method to check if any EditText field is filled
            (requireActivity() as? MotherNeonateANCActivity)?.updateNextButtonState()
        }
    }

    fun getPregnancyHistoryDetails() {
        val flowValue = viewModel.resultFlowHashMap[TAG] as? String
        viewModel.pregnancyDetailsModel.apply {
            pregnancyHistory = complaintsTagView.getSelectedTags().map { it.name }
            doesMotherHaveDeliveryKit = flowValue?.equals(yes, ignoreCase = true) ?: false
        }
    }

    fun isAnyEditTextFilled(): Boolean {
        return complaintsTagView.getSelectedTags()
            .isNotEmpty() || viewModel.resultFlowHashMap[TAG] != null || binding.etClinicalNotes.text?.isNotBlank() == true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setAncReqToGetMetaForPregnancyHistory(MedicalReviewTypeEnums.PregnancyHistories.name)
        attachObservers()
    }

    private fun initView(
        complaintList: ArrayList<ChipViewItemModel>,
        updateNextButtonStateCallback: (() -> Unit)? = null
    ) {
        complaintsTagView =
            TagListCustomView(binding.root.context, binding.tagViewPresentingComplaints,
                callBack = { _, _, _ ->
                    // Callback invoked whenever chip state changes
                    updateNextButtonStateCallback?.invoke() // Call the provided callback
                })
        complaintsTagView.addChipItemList(complaintList)
        getRMNCHFlowData().let {
            val view = SingleSelectionCustomView(binding.root.context)
            view.tag = TAG
            view.addViewElements(
                it,
                false,
                viewModel.resultFlowHashMap,
                Pair(TAG, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                singleSelectionCallback
            )
            binding.btnLayout.addView(view)
        }
        binding.etClinicalNotes.addTextChangedListener(textWatcher)
    }

    private fun attachObservers() {
        viewModel.ancMetaLiveDataForPregnancyHistory.observe(viewLifecycleOwner) {
            val complaintList = it.map { item ->
                ChipViewItemModel(
                    id = item.id,
                    name = item.name,
                    type = item.type
                )
            } as ArrayList<ChipViewItemModel>
            initView(complaintList) {
                (requireActivity() as? MotherNeonateANCActivity)?.updateNextButtonState()
            }
        }
    }

    private var singleSelectionCallback: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.resultFlowHashMap[TAG] = selectedID as String
            (requireActivity() as? MotherNeonateANCActivity)?.updateNextButtonState()
        }


    private fun getRMNCHFlowData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(CommonUtils.getOptionMap(getString(R.string.yes), getString(R.string.yes)))
        flowList.add(CommonUtils.getOptionMap(getString(R.string.no), getString(R.string.no)))
        return flowList
    }
}