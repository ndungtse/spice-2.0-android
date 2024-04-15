package com.medtroniclabs.spice.ui.medicalreview.undertwomonths

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils.getOptionMap
import com.medtroniclabs.spice.common.DefinedParams.DefaultIDLabel
import com.medtroniclabs.spice.common.DefinedParams.DefaultSelectID
import com.medtroniclabs.spice.common.DefinedParams.ID
import com.medtroniclabs.spice.common.DefinedParams.NAME
import com.medtroniclabs.spice.common.DefinedParams.Yes
import com.medtroniclabs.spice.databinding.FragmentClinicalSummaryBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.BREAST_FEEDING_TAG
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.EXCLUSIVE_BREAST_FEED_TAG
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.MOTHER_VITAMIN_TAG

class ClinicalSummaryFragment : BaseFragment(), View.OnClickListener {
    private lateinit var binding: FragmentClinicalSummaryBinding
    val viewModel: UnderTwoMonthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            FragmentClinicalSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (super.onViewCreated(view, savedInstanceState))
        initView()
        spinnerForDeliveryType()
        clickListener()
    }

    private fun clickListener() {
        binding.etVitaminAForMother.safeClickListener(this)
        binding.llExclusiveBreastFeedingStatus.safeClickListener(this)
        binding.llBreastFeedingStatus.safeClickListener(this)
    }

    fun initView() {

        getBreastFeedingFlowData().let {
            val view = SingleSelectionCustomView(binding.root.context)
            view.tag = BREAST_FEEDING_TAG
            view.addViewElements(
                it,
                false,
                viewModel.resultBreastFeedingHashMap,
                Pair(BREAST_FEEDING_TAG, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                singleSelectionCallback
            )
            binding.llBreastFeedingStatus.addView(view)
        }

        getVitaminAMotherFlowData().let {
            val view = SingleSelectionCustomView(binding.root.context)
            view.tag = MOTHER_VITAMIN_TAG
            view.addViewElements(
                it,
                false,
                viewModel.resultMotherVitaminHashMap,
                Pair(MOTHER_VITAMIN_TAG, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                vitaminAMotherSelectionCallBack
            )
            binding.etVitaminAForMother.addView(view)
        }

        getExclusiveBreastFeedingFlowData().let {
            val view = SingleSelectionCustomView(binding.root.context)
            view.tag = EXCLUSIVE_BREAST_FEED_TAG
            view.addViewElements(
                it,
                false,
                viewModel.exclusiveBreastFeedHashMap,
                Pair(EXCLUSIVE_BREAST_FEED_TAG, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                exclusiveBreastFeedingSelectionCallBack
            )
            binding.llExclusiveBreastFeedingStatus.addView(view)
        }

    }

    private var singleSelectionCallback: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.resultBreastFeedingHashMap[BREAST_FEEDING_TAG] = selectedID as String
            enableExclusiveBreastFeeding(selectedID)
        }

    private var vitaminAMotherSelectionCallBack: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.resultMotherVitaminHashMap[MOTHER_VITAMIN_TAG] = selectedID as String
        }

    private var exclusiveBreastFeedingSelectionCallBack: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.exclusiveBreastFeedHashMap[EXCLUSIVE_BREAST_FEED_TAG] = selectedID as String
        }

    private fun enableExclusiveBreastFeeding(selectedID: Any?) {
        if (selectedID == Yes) {
            binding.exclusiveBreastFeedingGroup.visible()
        } else {
            binding.exclusiveBreastFeedingGroup.gone()
        }
    }

    private fun getBreastFeedingFlowData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(getOptionMap(getString(R.string.yes), getString(R.string.yes)))
        flowList.add(getOptionMap(getString(R.string.no), getString(R.string.no)))
        return flowList
    }

    private fun getVitaminAMotherFlowData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(getOptionMap(getString(R.string.yes), getString(R.string.yes)))
        flowList.add(getOptionMap(getString(R.string.no), getString(R.string.no)))
        return flowList
    }

    private fun getExclusiveBreastFeedingFlowData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(getOptionMap(getString(R.string.yes), getString(R.string.yes)))
        flowList.add(getOptionMap(getString(R.string.no), getString(R.string.no)))
        return flowList
    }

    private fun spinnerForDeliveryType() {
        val list = arrayListOf<Map<String, Any>>()
        list.add(
            hashMapOf<String, Any>(
                NAME to DefaultIDLabel,
                ID to DefaultSelectID
            )
        )
        list.add(
            hashMapOf<String, Any>(
                NAME to " Status 1",
                ID to "1L"
            )
        )
        list.add(
            hashMapOf<String, Any>(
                NAME to "Status 2",
                ID to "2L"
            )
        )
        setListenerToDeliveryStatus(list)
    }

    private fun setListenerToDeliveryStatus(list: ArrayList<Map<String, Any>>) {
        val adapter = CustomSpinnerAdapter(requireContext())
        adapter.setData(list)
        binding.etImmunisationStatus.adapter = adapter
    }

    override fun onClick(v: View?) {

    }

}