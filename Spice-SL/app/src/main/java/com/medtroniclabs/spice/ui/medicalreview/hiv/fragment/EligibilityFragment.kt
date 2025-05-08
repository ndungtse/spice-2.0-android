package com.medtroniclabs.spice.ui.medicalreview.hiv.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils.getOptionMap
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.data.model.MultiSelectDropDownModel
import com.medtroniclabs.spice.databinding.FragmentEligibilityBinding
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.formgeneration.utility.MultiSelectSpinnerAdapter
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel.HivViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.HaveYouTakenHivTestBefore
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums


class EligibilityFragment : BaseFragment() {
    private lateinit var binding: FragmentEligibilityBinding
    private val hivViewModel: HivViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentEligibilityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        addObservers()
    }

    companion object {
        const val TAG = "EligibilityFragment"
    }

    private fun addObservers() {
        hivViewModel.hivMetaListItems.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let { list ->
                        initializeHivHistoryOptions(list.filter { it.category == MedicalReviewTypeEnums.hiv_history.name }
                            .sortedBy { it.displayOrder })
                        initializePopulationType(list.filter { it.category == MedicalReviewTypeEnums.population_type.name }
                            .sortedBy { it.displayOrder })
                        initializeHivTestDuration(list.filter { it.category == MedicalReviewTypeEnums.hiv_test_durations.name }
                            .sortedBy { it.displayOrder })
                    }
                }
            }
        }
    }

    private fun initViews() {
        binding.tvHaveYouTestedHIVBefore.markMandatory()
        hivViewModel.getHistoryListMetaItems()
        addCustomView(
            getData(),
            HaveYouTakenHivTestBefore,
            hivViewModel.resultHashMap,
            alreadyHIVTestedCallBack,
            binding.haveTestedHIVBeforeRoot
        )
    }


    private fun initializeHivTestDuration(costList: List<MedicalReviewMetaItems>) {
        val dropDownList = ArrayList<Map<String, Any>>()
        dropDownList.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to DefinedParams.DefaultIDLabel,
                DefinedParams.id to DefinedParams.DefaultID,
                DefinedParams.Value to DefinedParams.DefaultIDLabel
            )
        )
        for (item in costList) {
            dropDownList.add(
                hashMapOf<String, Any>(
                    DefinedParams.NAME to item.name,
                    DefinedParams.id to item.id.toString(),
                    DefinedParams.Value to (item.value ?: item.name)
                )
            )
        }
        val adapter = CustomSpinnerAdapter(requireContext())
        adapter.setData(dropDownList)
        binding.tvTestingDurationSpinner.adapter = adapter
        binding.tvTestingDurationSpinner.setSelection(0, false)
        binding.tvTestingDurationSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    itemId: Long
                ) {
                    val selectedItem = adapter.getData(position = pos)
                    selectedItem?.let {
                        handleHivTestDuration(it[DefinedParams.NAME] as String)
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /**
                     * this method is not used
                     */
                }
            }
    }

    fun handleHivTestDuration(testDuration: String) {
        val isHaveYouTakenHivTestBefore = hivViewModel.resultHashMap[HaveYouTakenHivTestBefore]
        if (testDuration != DefinedParams.DefaultIDLabel) {

            when (isHaveYouTakenHivTestBefore) {
                getString(R.string.yes) -> {
                    hivViewModel.selectedLastTestForHIV = testDuration
                    binding.tvTestingDurationSpinner.visible()
                }
                getString(R.string.no) -> {
                    hivViewModel.selectedLastTestForHIV = null
                    binding.tvTestingDurationSpinner.gone()
                }
                else -> {
                    hivViewModel.selectedLastTestForHIV = null
                    binding.tvTestingDurationSpinner.gone()
                }
            }
        } else {
            hivViewModel.selectedLastTestForHIV = null
        }
    }


    private fun initializeHivHistoryOptions(supplyList: List<MedicalReviewMetaItems>) {
        val dropDownList = ArrayList<MultiSelectDropDownModel>()
        for (item in supplyList) {
            dropDownList.add(
                MultiSelectDropDownModel(
                    id = item.id, name = item.name, value = item.value
                )
            )
        }
        val adapter = MultiSelectSpinnerAdapter(
            requireContext(), dropDownList, hivViewModel.selectedHistoryListItem
        )
        binding.tvHistorySpinner.adapter = adapter
        adapter.setOnItemSelectedListener(object :
            MultiSelectSpinnerAdapter.OnItemSelectedListener {
            override fun onItemSelected(
                selectedItems: List<MultiSelectDropDownModel>,
                pos: Int,
            ) {
                if (selectedItems.isNotEmpty()) {
                    hivViewModel.selectedHistoryListItem = ArrayList(selectedItems)
                }
            }
        })
    }

    private fun initializePopulationType(supplyList: List<MedicalReviewMetaItems>) {
        val dropDownList = ArrayList<MultiSelectDropDownModel>()
        for (item in supplyList) {
            dropDownList.add(
                MultiSelectDropDownModel(
                    id = item.id, name = item.name, value = item.value
                )
            )
        }
        val adapter = MultiSelectSpinnerAdapter(
            requireContext(), dropDownList, hivViewModel.selectedPopulationType
        )
        binding.tvPopulationTypeSpinner.adapter = adapter
        adapter.setOnItemSelectedListener(object :
            MultiSelectSpinnerAdapter.OnItemSelectedListener {
            override fun onItemSelected(
                selectedItems: List<MultiSelectDropDownModel>,
                pos: Int,
            ) {
                if (selectedItems.isNotEmpty()) {
                    hivViewModel.selectedPopulationType = ArrayList(selectedItems)
                }
            }
        })
    }

    private fun addCustomView(
        data: ArrayList<Map<String, Any>>,
        tag: String,
        hashMap: HashMap<String, Any>,
        callback: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)?,
        container: ViewGroup?
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
            container?.addView(this)
        }
    }

    private fun getData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(getOptionMap(getString(R.string.yes), getString(R.string.yes)))
        flowList.add(getOptionMap(getString(R.string.no), getString(R.string.no)))
        return flowList
    }

    private var alreadyHIVTestedCallBack: (selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit =
        { selectedID, _, _, _ ->
            hivViewModel.resultHashMap[HaveYouTakenHivTestBefore] = selectedID as String
            hivViewModel.resultHashMap[HaveYouTakenHivTestBefore]?.let {
                if (it == getString(R.string.yes)) {
                    binding.tvHaveTestedHIVBeforeError.gone()
                    binding.viewGroupHivTestDuration.visible()
                } else {
                    hivViewModel.selectedLastTestForHIV = null
                    binding.tvTestingDurationError.gone()
                    binding.viewGroupHivTestDuration.invisible()
                }
                resultMapChanged()
            }
        }

    private fun resultMapChanged() {
        setFragmentResult(
            MedicalReviewDefinedParams.HIV_ELIGIBILITY_ITEM, bundleOf(
                MedicalReviewDefinedParams.HIV_ELIGIBILITY_VALUES to true
            )
        )
    }

    fun validation(): Boolean {
        var isValid = true
        val isHaveYouTakenHivTestBefore = hivViewModel.resultHashMap[HaveYouTakenHivTestBefore] as? String
        val selectedLastTestForHIV = hivViewModel.selectedLastTestForHIV

        if (isHaveYouTakenHivTestBefore.isNullOrEmpty()) {
            binding.tvHaveTestedHIVBeforeError.visible()
            isValid = false
        } else binding.tvHaveTestedHIVBeforeError.gone()

        if (isHaveYouTakenHivTestBefore == getString(R.string.yes) && selectedLastTestForHIV.isNullOrEmpty()) {
            isValid = false
            binding.tvTestingDurationError.visible()
        } else binding.tvTestingDurationError.gone()
        return isValid
    }
}