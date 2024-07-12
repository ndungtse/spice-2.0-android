package com.medtroniclabs.spice.ui.medicalreview.undertwomonths.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils.getOptionMap
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.DefaultID
import com.medtroniclabs.spice.common.DefinedParams.DefaultIDLabel
import com.medtroniclabs.spice.common.DefinedParams.ID
import com.medtroniclabs.spice.common.DefinedParams.NAME
import com.medtroniclabs.spice.common.DefinedParams.Yes
import com.medtroniclabs.spice.common.DefinedParams.value
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.databinding.FragmentClinicalSummaryBinding
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil.isValidInput
import com.medtroniclabs.spice.ui.medicalreview.undertwomonths.viewmodel.ClinicalSummaryViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.BREAST_FEEDING_TAG
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.EXCLUSIVE_BREAST_FEED_TAG
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.MOTHER_VITAMIN_TAG

class ClinicalSummaryFragment : BaseFragment(), View.OnClickListener {
    private lateinit var binding: FragmentClinicalSummaryBinding
    val viewModel: ClinicalSummaryViewModel by activityViewModels()

    companion object {
        const val TAG = "ClinicalSummaryFragment"
    }

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
        super.onViewCreated(view, savedInstanceState)
        initView()
        initListeners()
        clickListener()
        attachObserver()
    }

    private fun attachObserver() {
        viewModel.summaryMetaListItems.observe(viewLifecycleOwner) {
            viewModel.summaryMetaListItems.observe(viewLifecycleOwner) { resourceState ->
                when (resourceState.state) {
                    ResourceState.LOADING -> {
                        showProgress()
                    }

                    ResourceState.SUCCESS -> {
                        hideProgress()
                        resourceState.data?.let { list ->
                            initializeDeliveryTypeItem(list)
                        }
                    }

                    ResourceState.ERROR -> {
                        hideProgress()
                    }
                }
            }
        }
    }

    private fun initListeners() {
        binding.apply {
            tvWAZLabel.markMandatory()
            tvWHZLabel.markMandatory()
            tvWeightLabel.markMandatory()
            tvHeightLabel.markMandatory()
        }
        binding.etWeight.doAfterTextChanged {
            viewModel.updateWeight(it.toString())
        }
        binding.etHeight.doAfterTextChanged {
            viewModel.updateHeight(it.toString())
        }
        binding.etTemperature.doAfterTextChanged {
            viewModel.updateTemperature(it.toString())
        }
        binding.etRespirationRate.doAfterTextChanged {
            val respiration = it?.trim().toString()
            viewModel.updateRespiratoryRate(
                respiration,
                binding.etRepeat.text?.trim().toString()
            )
            showHideRepeatField(respiration.toDoubleOrNull())
        }
        binding.etRepeat.doAfterTextChanged {
            viewModel.updateRespiratoryRate(
                binding.etRespirationRate.text?.trim().toString(),
                it?.trim().toString()
            )
        }
        binding.etWAZ.doAfterTextChanged {
            viewModel.updateWaz(it?.trim().toString())
        }
        binding.etWHZ.doAfterTextChanged {
            viewModel.updateWhz(it?.trim().toString())
        }
    }
    private fun showHideRepeatField(respiration: Double?) {
        if (respiration != null && respiration > 60.0) {
            binding.repeatGroup.visible()
        } else {
            binding.repeatGroup.invisible()
        }
    }
    private fun clickListener() {
        binding.etVitaminAForMother.safeClickListener(this)
        binding.llExclusiveBreastFeedingStatus.safeClickListener(this)
        binding.llBreastFeedingStatus.safeClickListener(this)
    }

    fun initView() {
        addCustomView(
            getBreastFeedingFlowData(),
            BREAST_FEEDING_TAG,
            viewModel.resultBreastFeedingHashMap,
            breastFeedingSelectionCallback,
            binding.llBreastFeedingStatus
        )

        addCustomView(
            getVitaminAMotherFlowData(),
            MOTHER_VITAMIN_TAG,
            viewModel.resultMotherVitaminHashMap,
            vitaminAMotherSelectionCallBack,
            binding.etVitaminAForMother
        )

        addCustomView(
            getExclusiveBreastFeedingFlowData(),
            EXCLUSIVE_BREAST_FEED_TAG,
            viewModel.resultExclusiveBreastFeedHashMap,
            exclusiveBreastFeedingSelectionCallBack,
            binding.llExclusiveBreastFeedingStatus
        )
        viewModel.getImmunisationStatusMetaItems()
    }

    private fun addCustomView(
        data: ArrayList<Map<String, Any>>,
        tag: String,
        hashMap: HashMap<String, Any>,
        callback: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)?,
        container: ViewGroup
    ) {
        SingleSelectionCustomView(binding.root.context).apply {
            this.tag = tag
            addViewElements(
                data,
                false,
                hashMap,
                Pair(tag, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                callback
            )
            container.addView(this)
        }
    }

    private val breastFeedingSelectionCallback: (selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit =
        { selectedID, _, _, _ ->
            viewModel.resultBreastFeedingHashMap[BREAST_FEEDING_TAG] = selectedID as String
            resetSelectionViews(EXCLUSIVE_BREAST_FEED_TAG)
            enableExclusiveBreastFeeding(selectedID)
            viewModel.updateBreastFeeding()
        }

    private val vitaminAMotherSelectionCallBack: (selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit =
        { selectedID, _, _, _ ->
            viewModel.resultMotherVitaminHashMap[MOTHER_VITAMIN_TAG] = selectedID as String
            viewModel.updateVitaminAForMother()
        }

    private val exclusiveBreastFeedingSelectionCallBack: (selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit =
        { selectedID, _, _, _ ->
            viewModel.resultExclusiveBreastFeedHashMap[EXCLUSIVE_BREAST_FEED_TAG] = selectedID as String
            viewModel.updateExclusiveBreastFeeding()
        }


    private fun enableExclusiveBreastFeeding(selectedID: Any?) {
        if (selectedID == Yes) {
            binding.exclusiveBreastFeedingGroup.visible()
        } else {
            binding.exclusiveBreastFeedingGroup.gone()
            viewModel.resultExclusiveBreastFeedHashMap.clear()
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

    private fun initializeDeliveryTypeItem(status: List<MedicalReviewMetaItems>) {
        val dropDownList = ArrayList<Map<String, Any>>()
        dropDownList.add(
            hashMapOf<String, Any>(
                NAME to DefaultIDLabel,
                ID to DefaultID
            )
        )
        for (item in status) {
            dropDownList.add(
                hashMapOf<String, Any>(
                    DefinedParams.NAME to item.name,
                    DefinedParams.id to item.id.toString(),
                    DefinedParams.value to (item.value ?: item.name)
                )
            )
        }
        setListenerToDeliveryStatus(dropDownList)
    }

    private fun setListenerToDeliveryStatus(list: ArrayList<Map<String, Any>>) {
        val adapter = CustomSpinnerAdapter(requireContext())
        adapter.setData(list)
        binding.etImmunisationStatus.adapter = adapter
        binding.etImmunisationStatus.setSelection(0, false)
        binding.etImmunisationStatus.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedItem = adapter.getData(position = position)
                    selectedItem?.let {
                        val selectedId = it[ID] as String?
                        val selectedImmunisationStatus = it[value] as String?
                        if (selectedId != DefaultID) {
                            selectedImmunisationStatus?.let {
                                viewModel.selectedImmunisationStatus = it
                                viewModel.updateImmunisationStatus()
                            }
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    /**
                     * this method is not used
                     */
                }
            }
    }

    override fun onClick(v: View?) {

    }

    fun isAnyEditTextFilled(): Boolean {
        return binding.etWeight.text?.isNotBlank() == true &&
                binding.etHeight.text?.isNotBlank() == true &&
                binding.etTemperature.text?.isNotBlank() == true &&
                binding.etRepeat.text?.isNotBlank() == true &&
                binding.etRespirationRate.text?.isNotBlank() == true &&
                viewModel.selectedImmunisationStatus != null &&
                viewModel.resultMotherVitaminHashMap[MOTHER_VITAMIN_TAG] != null &&
                viewModel.resultBreastFeedingHashMap[BREAST_FEEDING_TAG] != null ||
                viewModel.resultExclusiveBreastFeedHashMap[EXCLUSIVE_BREAST_FEED_TAG] != null
    }

    fun validateEditFields(): Boolean {
        val weight = weightValidate()
        val height = heightValidate()
        val temperature = temperatureValidate()
        val respirationRate = respirationRateValidate()
        val repeat = repeatValidate()
        val whz=whzValidate()
        val waz=wazValidate()
        return weight && height && temperature && respirationRate && repeat && whz && waz
    }
    private fun whzValidate(): Boolean {
        return if (binding.etWHZ.text?.isEmpty() == true) {
            binding.tvWHZError.visible()
            binding.tvWHZError.text = getString(R.string.error_label)
            false
        } else {
            binding.tvWHZError.gone() // Clear error message if needed
            true
        }
    }
    private fun wazValidate(): Boolean {
        return if (binding.etWAZ.text?.isEmpty() == true) {
            binding.tvWAZError.visible()
            binding.tvWAZError.text = getString(R.string.error_label)
            false
        } else {
            binding.tvWAZError.gone() // Clear error message if needed
            true
        }
    }


    private fun heightValidate(): Boolean {
        return isValidInput(
            binding.etHeight.text.toString(),
            binding.etHeight,
            binding.tvHeightError,
            0.0..300.0,
            R.string.height_error,
            true,
            requireContext()
        )
    }

    private fun respirationRateValidate(): Boolean {
        return isValidInput(
            binding.etRespirationRate.text.toString(),
            binding.etRespirationRate,
            binding.tvRespirationRateError,
            0.0..100.0,
            (R.string.respiratory_error),
            false,
            requireContext()
        )
    }

    private fun repeatValidate(): Boolean {
        return isValidInput(
            binding.etRepeat.text.toString(),
            binding.etRepeat,
            binding.tvRepeatError,
            0.0..100.0,
            (R.string.please_enter_repeat_between_0_to_100),
            false,
            requireContext()
        )
    }

    private fun temperatureValidate(): Boolean {
        return isValidInput(
            binding.etTemperature.text.toString(),
            binding.etTemperature,
            binding.tvTemperatureLabelError,
            10.0..300.0,
            (R.string.temperature),
            false,
            requireContext()
        )
    }


    private fun weightValidate(): Boolean {
        return isValidInput(
            binding.etWeight.text.toString(),
            binding.etWeight,
            binding.tvWeightError,
            0.0..400.0,
            R.string.weight_error,
            true,
            requireContext()
        )
    }

    private fun resetSelectionViews(vararg viewTags: String) {
        viewTags.forEach { tag ->
            val view = binding.root.findViewWithTag<SingleSelectionCustomView>(tag)
            view?.resetSingleSelectionChildViews()
        }
    }

}