package com.medtroniclabs.spice.ui.medicalreview.hiv.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.databinding.FragmentHivStatusBinding
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel.HIVStatusViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HIVStatusFragment : BaseFragment() {

    val adapter: CustomSpinnerAdapter by lazy { CustomSpinnerAdapter(requireContext()) }
    private val viewModel: HIVStatusViewModel by activityViewModels()
    private lateinit var binding: FragmentHivStatusBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHivStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "HIVStatusFragment"
        fun newInstance() =
            HIVStatusFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObserver()
    }

    fun attachObserver() {
        viewModel.getHivStatusMetaList.observe(viewLifecycleOwner) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    resource.data?.let { listItems ->
                        fun filterAndMap(category: String): ArrayList<Map<String, Any>> {
                            return listItems
                                .filter { it.category == category }
                                .map { item ->
                                    CommonUtils.getOptionMap(
                                        value = item.value.orEmpty(),
                                        name = item.name
                                    )
                                }
                                .toCollection(ArrayList())
                        }
                        initPregnancyStatus(filterAndMap(MedicalReviewTypeEnums.hivPreganancyBreastFeedingStatus.name))
                        initAHD(filterAndMap(MedicalReviewTypeEnums.ahdStatus.name))
                        initDSD(filterAndMap(MedicalReviewTypeEnums.dsdStatus.name))
                        val dropDownList = ArrayList<Map<String, Any>>()
                        dropDownList.add(
                            mapOf(
                                DefinedParams.NAME to "",
                                DefinedParams.Value to DefinedParams.DefaultID
                            )
                        )
                        dropDownList.addAll(listItems
                            .filter { it.category == MedicalReviewTypeEnums.nonEstablishedModels.name }
                            .map { item ->
                                mapOf(
                                    DefinedParams.NAME to item.name,
                                    DefinedParams.Value to (item.value ?: item.name)
                                )
                            })
                        if (dropDownList.isNotEmpty()) {
                            setSpinner(ArrayList(dropDownList))
                        }
                    }
                    hideProgress()
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }
    }

    private fun initAHD(data: ArrayList<Map<String, Any>>?) {
        if (data.isNullOrEmpty()) return  // Exit if null or empty

        val view = SingleSelectionCustomView(requireContext()).apply {
            tag = MedicalReviewTypeEnums.ahdStatus.name
            addViewElements(
                data,
                SecuredPreference.getIsTranslationEnabled(),
                viewModel.resultAHD,
                Pair(MedicalReviewTypeEnums.ahdStatus.name, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                singleSelectionAHDCallback
            )
        }

        binding.llAHD.addView(view)
    }

    private fun initDSD(data: ArrayList<Map<String, Any>>?) {
        if (data.isNullOrEmpty()) return  // Exit if null or empty

        val view = SingleSelectionCustomView(requireContext()).apply {
            tag = MedicalReviewTypeEnums.dsdStatus.name
            addViewElements(
                data,
                SecuredPreference.getIsTranslationEnabled(),
                viewModel.resultDSD,
                Pair(MedicalReviewTypeEnums.dsdStatus.name, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                singleSelectionDSDCallback
            )
        }

        binding.llDSD.addView(view)
    }

    fun initView() {
        viewModel.getHivStatusMeta(MedicalReviewTypeEnums.HIV.name)
    }

    private fun initPregnancyStatus(data: ArrayList<Map<String, Any>>?) {
        if (data.isNullOrEmpty()) return  // Exit if null or empty

        val view = SingleSelectionCustomView(requireContext()).apply {
            tag = MedicalReviewTypeEnums.hivPreganancyBreastFeedingStatus.name
            addViewElements(
                data,
                SecuredPreference.getIsTranslationEnabled(),
                viewModel.resultPregnantStatus,
                Pair(MedicalReviewTypeEnums.hivPreganancyBreastFeedingStatus.name, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                singleSelectionCallback
            )
        }

        binding.llPregnancyAndBreastFeedingStatus.addView(view)
    }

    private var singleSelectionCallback: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.resultPregnantStatus[MedicalReviewTypeEnums.hivPreganancyBreastFeedingStatus.name] =
                selectedID as? String ?: ""
        }

    private var singleSelectionAHDCallback: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.resultAHD[MedicalReviewTypeEnums.ahdStatus.name] =
                selectedID as? String ?: ""
        }

    private var singleSelectionDSDCallback: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.resultDSD[MedicalReviewTypeEnums.dsdStatus.name] =
                selectedID as? String ?: ""
        }

    private fun setSpinner(statusList: ArrayList<Map<String, Any>>) {
        adapter.setData(statusList)
        binding.etSelectModel.adapter = adapter
        val defaultPosition = 0
        binding.etSelectModel.post {
            binding.etSelectModel.setSelection(defaultPosition, false)
        }
        binding.etSelectModel.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    itemId: Long
                ) {
                    val selectedItem = adapter.getData(position = pos)
                    selectedItem?.let {
                        val selectedModel = it[DefinedParams.Value] as String?
                        selectedModel?.let {
                            viewModel.selectModel = selectedModel
                        } ?: kotlin.run {
                            viewModel.selectModel = null
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /**
                     * this method is not used
                     */
                }
            }
    }
}